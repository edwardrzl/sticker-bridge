package com.edrl.stickerbridge.core.link

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PostLinkParserTest {
    @Test
    fun `long video links`() {
        assertEquals(
            PostLink("https://www.tiktok.com/@jc.enduro/video/7691548371403902239", PostKind.VIDEO),
            PostLinkParser.parse("https://www.tiktok.com/@jc.enduro/video/7691548371403902239"),
        )
        assertEquals(PostKind.VIDEO, PostLinkParser.parse("https://tiktok.com/@a_b/video/123")?.kind)
    }

    @Test
    fun `photo posts`() {
        assertEquals(
            PostLink("https://www.tiktok.com/@juanfe.jfc/photo/7692552918939061512", PostKind.PHOTO),
            PostLinkParser.parse("https://www.tiktok.com/@juanfe.jfc/photo/7692552918939061512"),
        )
    }

    @Test
    fun `mobile links`() {
        assertEquals(
            PostLink("https://m.tiktok.com/v/7691548371403902239", PostKind.VIDEO),
            PostLinkParser.parse("https://m.tiktok.com/v/7691548371403902239.html"),
        )
    }

    @Test
    fun `short links`() {
        assertEquals(
            PostLink("https://vt.tiktok.com/ZSb9e5Ndq/", PostKind.SHORT),
            PostLinkParser.parse("https://vt.tiktok.com/ZSb9e5Ndq/"),
        )
        assertEquals(PostKind.SHORT, PostLinkParser.parse("https://vm.tiktok.com/ZMabc123/")?.kind)
        assertEquals(PostKind.SHORT, PostLinkParser.parse("https://www.tiktok.com/t/ZTabc123/")?.kind)
    }

    @Test
    fun `finds the link inside a longer shared text`() {
        val shared = "Mira este video de @juanfe.jfc en TikTok https://vt.tiktok.com/ZSb9e5Ndq/ ¡te va a gustar!"

        assertEquals(PostLink("https://vt.tiktok.com/ZSb9e5Ndq/", PostKind.SHORT), PostLinkParser.parse(shared))
    }

    @Test
    fun `drops query parameters and forces https`() {
        assertEquals(
            "https://www.tiktok.com/@jc.enduro/video/7691548371403902239",
            PostLinkParser
                .parse(
                    "http://www.tiktok.com/@jc.enduro/video/7691548371403902239?is_from_webapp=1&sender_device=pc",
                )?.url,
        )
    }

    @Test
    fun `adds the scheme when it is missing`() {
        assertEquals(PostKind.SHORT, PostLinkParser.parse("vt.tiktok.com/ZSb9e5Ndq/")?.kind)
    }

    @Test
    fun `rejects other sites, profiles and plain text`() {
        assertNull(PostLinkParser.parse("https://www.youtube.com/watch?v=abc"))
        assertNull(PostLinkParser.parse("https://www.tiktok.com/@jc.enduro"))
        assertNull(PostLinkParser.parse("hola, no hay enlace aquí"))
        assertNull(PostLinkParser.parse("https://eviltiktok.com/@a/video/123"))
    }
}
