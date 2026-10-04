package com.edrl.stickerbridge.core.extraction

import kotlin.test.Test
import kotlin.test.assertEquals

class CommentImageOrderTest {
    private fun image(
        name: String,
        likes: Long,
    ) = CommentImage(
        url = "https://p16.tiktokcdn.com/$name",
        thumbnailUrl = "https://p16.tiktokcdn.com/$name",
        likes = likes,
    )

    @Test
    fun `most liked comments come first`() {
        val ordered = CommentImageOrder.byLikes(listOf(image("b", 19), image("c", 3), image("a", 53)))

        assertEquals(listOf(53L, 19L, 3L), ordered.map { it.likes })
    }

    @Test
    fun `ties keep TikTok's order`() {
        val ordered = CommentImageOrder.byLikes(listOf(image("first", 7), image("top", 9), image("second", 7)))

        assertEquals(listOf("top", "first", "second"), ordered.map { it.url.substringAfterLast('/') })
    }

    @Test
    fun `a repeated image appears once with its highest like count`() {
        val ordered = CommentImageOrder.byLikes(listOf(image("same", 2), image("other", 5), image("same", 40)))

        assertEquals(listOf("same" to 40L, "other" to 5L), ordered.map { it.url.substringAfterLast('/') to it.likes })
    }

    @Test
    fun `an empty list stays empty`() {
        assertEquals(emptyList(), CommentImageOrder.byLikes(emptyList()))
    }
}
