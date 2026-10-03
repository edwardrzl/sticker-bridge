package com.edrl.stickerbridge.core.extraction

import com.edrl.stickerbridge.core.link.PostLink

/** An image attached to a comment. Its identity is the URL of the original file. */
data class CommentImage(
    val url: String,
    val thumbnailUrl: String,
)

/** Images found in one or more batches of comments, and whether TikTok has more comments to load. */
data class ExtractionPage(
    val images: List<CommentImage>,
    val hasMore: Boolean,
)

/** Why the comment images of a post could not be obtained. */
sealed interface ExtractionError {
    data object NoConnection : ExtractionError

    data object PostUnavailable : ExtractionError

    /** TikTok asked to log in, to solve a verification, or to open its app. */
    data object CommentsBlocked : ExtractionError

    data object Timeout : ExtractionError

    /** TikTok answered with something the app does not understand, probably after a change on their side. */
    data class UnexpectedFormat(
        val detail: String,
    ) : ExtractionError
}

sealed interface ExtractionOutcome {
    data class Loaded(
        val page: ExtractionPage,
    ) : ExtractionOutcome

    data class Failed(
        val error: ExtractionError,
    ) : ExtractionOutcome
}

/**
 * Port: obtains the images attached to the comments of a TikTok post.
 *
 * This is the only boundary the rest of the app knows about extraction, so the way of extracting
 * can be replaced without touching selection, conversion or packs.
 */
fun interface CommentImageExtractor {
    fun open(link: PostLink): ExtractionSession
}

/** One extraction of one post. Closing it releases the browser and deletes its cookies and storage. */
interface ExtractionSession : AutoCloseable {
    suspend fun loadInitial(): ExtractionOutcome

    suspend fun loadMore(): ExtractionOutcome
}
