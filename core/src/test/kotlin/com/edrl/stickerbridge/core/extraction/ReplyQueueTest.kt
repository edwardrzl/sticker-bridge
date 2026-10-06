package com.edrl.stickerbridge.core.extraction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReplyQueueTest {
    private fun thread(
        id: String,
        likes: Long,
    ) = CommentThread(id, likes, replies = 10)

    @Test
    fun `an empty queue has nothing pending`() {
        val queue = ReplyQueue()

        assertFalse(queue.hasPending)
        assertTrue(queue.next().isEmpty())
    }

    @Test
    fun `gives the most liked comments first, three at a time`() {
        val queue = ReplyQueue()
        queue.add(listOf(thread("1", 5), thread("2", 900), thread("3", 40), thread("4", 7), thread("5", 300)))

        assertEquals(listOf("2", "5", "3"), queue.next().map { it.commentId })
        assertTrue(queue.hasPending)
        assertEquals(listOf("4", "1"), queue.next().map { it.commentId })
        assertFalse(queue.hasPending)
    }

    @Test
    fun `never gives the same comment twice, even if it is added again`() {
        val queue = ReplyQueue()
        queue.add(listOf(thread("1", 5)))
        queue.next()

        queue.add(listOf(thread("1", 5)))

        assertFalse(queue.hasPending)
        assertTrue(queue.next().isEmpty())
    }

    @Test
    fun `comments loaded later join the queue`() {
        val queue = ReplyQueue()
        queue.add(listOf(thread("1", 5)))
        queue.next()

        queue.add(listOf(thread("2", 1)))

        assertTrue(queue.hasPending)
        assertEquals(listOf("2"), queue.next().map { it.commentId })
    }

    @Test
    fun `ties keep the order in which TikTok gave the comments`() {
        val queue = ReplyQueue()
        queue.add(listOf(thread("1", 5), thread("2", 5), thread("3", 5)))

        assertEquals(listOf("1", "2", "3"), queue.next().map { it.commentId })
    }
}
