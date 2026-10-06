package com.edrl.stickerbridge.core.extraction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CommentResponseParserTest {
    private val parser = CommentResponseParser()

    private fun resource(name: String): String =
        checkNotNull(javaClass.getResource("/comment-list/$name")) { "missing fixture $name" }.readText()

    @Test
    fun `extracts image urls from comments that have images`() {
        val result = parser.parse(resource("with-images.json"))

        val parsed = assertIs<ParsedComments.Parsed>(result)
        assertEquals(
            listOf(
                "https://p16-sign.tiktokcdn.com/sticker-a.webp",
                "https://p16-sign.tiktokcdn.com/photo-b.jpeg",
            ),
            parsed.images.map { it.url },
        )
        assertTrue(parsed.hasMore)
    }

    @Test
    fun `uses the crop url as thumbnail and falls back to the original`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-images.json")))

        assertEquals("https://p16-sign.tiktokcdn.com/sticker-a-crop.webp", parsed.images[0].thumbnailUrl)
        assertEquals(parsed.images[1].url, parsed.images[1].thumbnailUrl)
    }

    @Test
    fun `returns each image url only once`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("duplicates.json")))

        assertEquals(listOf("https://p16-sign.tiktokcdn.com/same.webp"), parsed.images.map { it.url })
    }

    @Test
    fun `extracts stickers preferring the animated high resolution file`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-stickers.json")))

        assertEquals(
            listOf("$STICKER_FOLDER/a8851f58~tplv-dhq7zx4c1p-full.awebp", "$STICKER_HOST/static-only~full.webp"),
            parsed.images.map { it.url },
        )
    }

    @Test
    fun `uses the low resolution static sticker as thumbnail`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-stickers.json")))

        assertEquals("$STICKER_FOLDER/a8851f58~tplv-dhq7zx4c1p-low.webp", parsed.images[0].thumbnailUrl)
        assertEquals(parsed.images[1].url, parsed.images[1].thumbnailUrl)
    }

    @Test
    fun `each image carries the likes of its comment`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-images.json")))

        assertEquals(listOf(19L, 53L), parsed.images.map { it.likes })
    }

    @Test
    fun `comments without a like count count as zero likes`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-stickers.json")))

        assertEquals(listOf(0L, 0L), parsed.images.map { it.likes })
    }

    @Test
    fun `lists the comments that have replies, with their likes`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-replies.json")))

        assertEquals(
            listOf(
                CommentThread("7674322013931029262", likes = 168_227, replies = 1_017),
                CommentThread("7674380166761022228", likes = 419_910, replies = 893),
            ),
            parsed.threads,
        )
    }

    @Test
    fun `a comment id that is not a number is never a thread`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-replies.json")))

        assertTrue(parsed.threads.all { thread -> thread.commentId.all(Char::isDigit) })
    }

    @Test
    fun `comments without replies give no threads`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse(resource("with-images.json")))

        assertTrue(parsed.threads.isEmpty())
    }

    @Test
    fun `null comments with a success status means an empty page`() {
        val parsed = assertIs<ParsedComments.Parsed>(parser.parse("""{"status_code":0,"comments":null,"has_more":0}"""))

        assertTrue(parsed.images.isEmpty())
        assertEquals(false, parsed.hasMore)
    }

    @Test
    fun `missing comments field is an unexpected format`() {
        val result = parser.parse("""{"status_code":0,"something_else":[]}""")

        assertIs<ParsedComments.Malformed>(result)
    }

    @Test
    fun `invalid json is an unexpected format`() {
        assertIs<ParsedComments.Malformed>(parser.parse("<html>login</html>"))
    }

    private companion object {
        const val STICKER_HOST = "https://p16-tiktok-dm-sticker-sign-sg.ibyteimg.com"
        const val STICKER_FOLDER = "$STICKER_HOST/tos-alisg-i-dhq7zx4c1p-sg"
    }
}
