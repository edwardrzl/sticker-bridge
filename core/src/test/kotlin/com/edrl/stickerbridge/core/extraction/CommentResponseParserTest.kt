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
}
