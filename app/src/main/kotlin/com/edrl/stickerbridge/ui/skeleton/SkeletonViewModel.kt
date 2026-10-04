package com.edrl.stickerbridge.ui.skeleton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.extraction.ExtractionSession
import com.edrl.stickerbridge.core.link.PostLinkParser
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StorageException
import com.edrl.stickerbridge.core.save.PackAction
import com.edrl.stickerbridge.core.save.SaveResult
import com.edrl.stickerbridge.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

data class SkeletonState(
    val link: String = "",
    val running: Boolean = false,
    val lines: List<String> = emptyList(),
    val canLoadMore: Boolean = false,
    /** A pack waiting for WhatsApp's add or update confirmation, launched by the screen. */
    val pendingAdd: StickerPack? = null,
)

/**
 * Provisional test screen: link → comment images ordered by likes → stickers saved with the save
 * use case → one WhatsApp confirmation per pack touched. Replaced by the real screens in U5.
 */
class SkeletonViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SkeletonState())
    val state: StateFlow<SkeletonState> = mutableState.asStateFlow()

    /** Kept open after a search so "load more" can continue it. */
    private var session: ExtractionSession? = null

    /** Packs still waiting for their WhatsApp confirmation, shown one after another. */
    private val confirmations = ArrayDeque<StickerPack>()

    fun onLinkChange(link: String) = mutableState.update { it.copy(link = link) }

    fun run() =
        launchStep {
            session?.close()
            session = null
            mutableState.update { it.copy(lines = emptyList(), canLoadMore = false) }
            val link = PostLinkParser.parse(state.value.link)
            if (link == null) {
                report("No es un enlace de video de TikTok")
                return@launchStep
            }
            val mark = TimeSource.Monotonic.markNow()
            val outcome =
                container.extractor
                    .open(link)
                    .also { session = it }
                    .loadInitial()
            describe(outcome, mark.elapsedNow().inWholeMilliseconds).forEach(::report)
            if (outcome is ExtractionOutcome.Loaded) {
                mutableState.update { it.copy(canLoadMore = outcome.page.hasMore) }
                val chosen =
                    outcome.page.images
                        .take(MAX_IMAGES_PER_RUN)
                        .map { ImageRef.Remote(it.url) }
                val saveMark = TimeSource.Monotonic.markNow()
                val result = container.saveStickers.save(chosen)
                describe(result, saveMark.elapsedNow().inWholeMilliseconds).forEach(::report)
                confirmations += result.actions.filterIsInstance<PackAction.OpenWhatsApp>().map { it.pack }
                showNextConfirmation()
            }
        }

    /** Loads one more batch of comments and reports the reordered list (FR2.4, FR2.8). */
    fun loadMore() =
        launchStep {
            val current = session ?: return@launchStep
            val mark = TimeSource.Monotonic.markNow()
            val outcome = current.loadMore()
            describe(outcome, mark.elapsedNow().inWholeMilliseconds).forEach(::report)
            mutableState.update { it.copy(canLoadMore = outcome is ExtractionOutcome.Loaded && outcome.page.hasMore) }
        }

    fun onAddLaunched() = mutableState.update { it.copy(pendingAdd = null) }

    fun onAddResult(accepted: Boolean) {
        report(if (accepted) "WhatsApp confirmó el paquete" else "WhatsApp no confirmó el paquete")
        showNextConfirmation()
    }

    override fun onCleared() {
        session?.close()
    }

    private fun showNextConfirmation() {
        val next = confirmations.removeFirstOrNull() ?: return
        // WhatsApp only reloads an added pack when its screen is opened again (FR5.6).
        container.publisher.notifyChanged(next)
        mutableState.update { it.copy(pendingAdd = next) }
    }

    private fun launchStep(block: suspend () -> Unit) {
        if (state.value.running) return
        viewModelScope.launch {
            mutableState.update { it.copy(running = true) }
            try {
                block()
            } catch (e: StorageException) {
                report("No se pudieron guardar los stickers: ${e.message}")
            } finally {
                mutableState.update { it.copy(running = false) }
            }
        }
    }

    private fun report(line: String) {
        container.diagnostics.event("skeleton", line)
        mutableState.update { it.copy(lines = it.lines + line) }
    }

    private companion object {
        /** The test screen saves at most this many of the images found, with no selection screen. */
        const val MAX_IMAGES_PER_RUN = 5
    }
}

private fun describe(
    outcome: ExtractionOutcome,
    elapsedMs: Long,
): List<String> =
    when (outcome) {
        is ExtractionOutcome.Loaded ->
            listOf(
                "Imágenes encontradas: ${outcome.page.images.size} ($elapsedMs ms)" +
                    if (outcome.page.hasMore) ", hay más comentarios" else "",
                "Likes, en orden: ${outcome.page.images.joinToString { it.likes.toString() }}",
            )
        is ExtractionOutcome.Failed -> listOf("La extracción falló: ${outcome.error} ($elapsedMs ms)")
    }

private fun describe(
    result: SaveResult,
    elapsedMs: Long,
): List<String> =
    buildList {
        add("Guardados en $elapsedMs ms: ${result.saved.entries.joinToString { "${it.value} en ${it.key}" }}")
        if (result.withoutAnimation > 0) add("${result.withoutAnimation} se guardaron sin animación")
        if (result.failed > 0) add("${result.failed} no se pudieron convertir")
        if (!result.whatsAppInstalled) add("WhatsApp no está instalado")
        result.actions.filterIsInstance<PackAction.Waiting>().forEach {
            add("${it.pack.name}: faltan ${it.missing} para poder agregarlo a WhatsApp")
        }
    }
