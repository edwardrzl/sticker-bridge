package com.edrl.stickerbridge.core.extraction

/** A comment that has replies, which may carry images of their own (FR2.10). */
data class CommentThread(
    /** TikTok's comment id: digits only. */
    val commentId: String,
    val likes: Long,
    val replies: Int,
)

/**
 * Decides whose replies to read next. Replies are requested per comment and a popular post has
 * thousands, so they are read on demand: the most liked comments first, a few per request, and
 * each comment only once (FR2.10).
 */
class ReplyQueue(
    private val batchSize: Int = THREADS_PER_REQUEST,
) {
    private val known = LinkedHashMap<String, CommentThread>()
    private val asked = mutableSetOf<String>()

    val hasPending: Boolean
        get() = known.keys.any { it !in asked }

    fun add(threads: List<CommentThread>) = threads.forEach { known.putIfAbsent(it.commentId, it) }

    /** The next comments to read replies from; they will not be given again. */
    fun next(): List<CommentThread> {
        val batch =
            known.values
                .filter { it.commentId !in asked }
                .sortedByDescending { it.likes }
                .take(batchSize)
        asked += batch.map { it.commentId }
        return batch
    }

    companion object {
        const val THREADS_PER_REQUEST = 3
    }
}
