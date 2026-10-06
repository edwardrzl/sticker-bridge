package com.edrl.stickerbridge.net

import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import com.edrl.stickerbridge.core.extraction.HostAllowlist
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AllowlistInterceptorTest {
    private val refused = mutableListOf<String>()
    private val log = DiagnosticLog { _, message, _ -> refused += message }
    private val client = OkHttpClient.Builder().addInterceptor(AllowlistInterceptor(HostAllowlist.TIKTOK, log)).build()

    private fun get(url: String) = client.newCall(Request.Builder().url(url).build()).execute()

    @Test
    fun `an address on another domain is refused before anything is sent`() {
        assertFailsWith<IOException> { get("https://example.invalid/photo.jpeg") }

        assertEquals(listOf("request to https://example.invalid refused"), refused)
    }

    @Test
    fun `an address without encryption is refused even on a tiktok domain`() {
        assertFailsWith<IOException> { get("http://p16-common-sign.tiktokcdn.com/photo.jpeg") }

        assertEquals(listOf("request to http://p16-common-sign.tiktokcdn.com refused"), refused)
    }
}
