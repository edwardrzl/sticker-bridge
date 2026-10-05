package com.edrl.stickerbridge.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.core.extraction.CommentImageExtractor
import com.edrl.stickerbridge.core.extraction.ExtractionError
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.extraction.ExtractionSession
import com.edrl.stickerbridge.core.link.PostLink
import com.edrl.stickerbridge.core.link.PostLinkParser
import com.edrl.stickerbridge.core.pack.StorageException
import com.edrl.stickerbridge.core.save.PackAction
import com.edrl.stickerbridge.core.save.SaveResult
import com.edrl.stickerbridge.core.save.SaveStickersUseCase
import com.edrl.stickerbridge.ui.WhatsAppConfirmations
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchPhase {
    data object Idle : SearchPhase

    data object InvalidLink : SearchPhase

    data object Searching : SearchPhase

    /** Images are shown (possibly none) and can be chosen. */
    data object Loaded : SearchPhase

    data class Failed(
        val error: ExtractionError,
    ) : SearchPhase

    data class Saving(
        val done: Int,
        val total: Int,
    ) : SearchPhase

    data class Saved(
        val result: SaveResult,
    ) : SearchPhase

    data object StorageFailed : SearchPhase
}

/** Where "load more" looks: further comments, or the replies of the comments already loaded. */
enum class MoreSource { Comments, Replies }

data class SearchState(
    val phase: SearchPhase = SearchPhase.Idle,
    /** Ordered by likes, most liked first (FR2.8). */
    val images: List<CommentImage> = emptyList(),
    /** URLs of the chosen images. */
    val selected: Set<String> = emptySet(),
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    /** Some loaded comment has replies that were not read yet (FR2.10). */
    val hasMoreReplies: Boolean = false,
    val loadingReplies: Boolean = false,
) {
    val loading: Boolean get() = loadingMore || loadingReplies
}

/** The state after asking for more: the images found so far, or no more of that source if it failed. */
private fun SearchState.after(
    outcome: ExtractionOutcome,
    source: MoreSource,
): SearchState {
    val idle = copy(loadingMore = false, loadingReplies = false)
    return when (outcome) {
        is ExtractionOutcome.Loaded ->
            idle.copy(
                images = outcome.page.images,
                hasMore = outcome.page.hasMore,
                hasMoreReplies = outcome.page.hasMoreReplies,
            )
        is ExtractionOutcome.Failed ->
            if (source == MoreSource.Replies) idle.copy(hasMoreReplies = false) else idle.copy(hasMore = false)
    }
}

/**
 * The video screen: search a post's comment images, choose some, save them (FR3, FR5.5), and
 * import gallery images through the same save path (FR7.2).
 */
class SearchViewModel(
    private val extractor: CommentImageExtractor,
    private val saveStickers: SaveStickersUseCase,
    val confirmations: WhatsAppConfirmations,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = mutableState.asStateFlow()

    private var session: ExtractionSession? = null
    private var job: Job? = null

    /** Starts a search from a pasted or shared text (FR1.1, FR1.2, FR1.4). */
    fun search(text: String) {
        val link = PostLinkParser.parse(text)
        if (link == null) {
            mutableState.value = SearchState(phase = SearchPhase.InvalidLink)
            return
        }
        start(link)
    }

    /** Stops the search in progress (FR2.3). */
    fun cancel() {
        job?.cancel()
        session?.close()
        session = null
        mutableState.value = SearchState()
    }

    fun toggle(url: String) =
        mutableState.update { state ->
            state.copy(selected = if (url in state.selected) state.selected - url else state.selected + url)
        }

    /**
     * One more batch of comments, or the replies of the next few comments; the choice made so far
     * is kept (FR2.4, FR2.10). One request at a time.
     */
    fun loadMore(source: MoreSource = MoreSource.Comments) {
        val current = session
        val state = mutableState.value
        val available = if (source == MoreSource.Replies) state.hasMoreReplies else state.hasMore
        if (current == null || state.loading || !available) return
        mutableState.update {
            it.copy(loadingMore = source == MoreSource.Comments, loadingReplies = source == MoreSource.Replies)
        }
        viewModelScope.launch {
            val outcome = if (source == MoreSource.Replies) current.loadReplies() else current.loadMore()
            mutableState.update { it.after(outcome, source) }
        }
    }

    /** Saves the chosen images, most liked first (FR3.3). */
    fun saveSelected() {
        val state = mutableState.value
        val chosen = state.images.filter { it.url in state.selected }.map { ImageRef.Remote(it.url) }
        if (chosen.isNotEmpty()) save(chosen)
    }

    /** Saves images picked from the gallery (FR7). */
    fun saveImported(uris: List<String>) {
        if (uris.isNotEmpty()) save(uris.map(ImageRef::Local))
    }

    /** Back to the grid after saving, to choose more from the same video. */
    fun showImages() {
        if (mutableState.value.images.isNotEmpty()) mutableState.update { it.copy(phase = SearchPhase.Loaded) }
    }

    override fun onCleared() {
        session?.close()
    }

    private fun start(link: PostLink) {
        job?.cancel()
        session?.close()
        session = null
        mutableState.value = SearchState(phase = SearchPhase.Searching)
        job =
            viewModelScope.launch {
                val opened = extractor.open(link).also { session = it }
                mutableState.value =
                    when (val outcome = opened.loadInitial()) {
                        is ExtractionOutcome.Loaded ->
                            SearchState(
                                SearchPhase.Loaded,
                                outcome.page.images,
                                hasMore = outcome.page.hasMore,
                                hasMoreReplies = outcome.page.hasMoreReplies,
                            )
                        is ExtractionOutcome.Failed -> SearchState(SearchPhase.Failed(outcome.error))
                    }
            }
    }

    private fun save(sources: List<ImageRef>) {
        if (mutableState.value.phase is SearchPhase.Saving) return
        mutableState.update { it.copy(phase = SearchPhase.Saving(0, sources.size)) }
        viewModelScope.launch {
            val phase =
                try {
                    val result =
                        saveStickers.save(sources) { done, total ->
                            mutableState.update { it.copy(phase = SearchPhase.Saving(done, total)) }
                        }
                    confirmations.enqueue(result.actions.filterIsInstance<PackAction.OpenWhatsApp>().map { it.pack })
                    SearchPhase.Saved(result)
                } catch (_: StorageException) {
                    SearchPhase.StorageFailed
                }
            mutableState.update { it.copy(phase = phase, selected = emptySet()) }
        }
    }
}
