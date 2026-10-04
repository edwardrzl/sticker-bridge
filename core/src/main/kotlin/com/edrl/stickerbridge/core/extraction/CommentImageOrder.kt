package com.edrl.stickerbridge.core.extraction

/**
 * Orders comment images by the likes of their comment, most liked first (FR2.8). TikTok's web
 * API returns comments in its own order, so the app sorts everything loaded so far.
 */
object CommentImageOrder {
    /** Stable: ties keep TikTok's order. A repeated image keeps its highest like count (BR-02). */
    fun byLikes(images: List<CommentImage>): List<CommentImage> {
        val unique = LinkedHashMap<String, CommentImage>()
        images.forEach { image ->
            val existing = unique[image.url]
            if (existing == null || image.likes > existing.likes) {
                unique[image.url] = existing?.copy(likes = image.likes) ?: image
            }
        }
        return unique.values.sortedByDescending { it.likes }
    }
}
