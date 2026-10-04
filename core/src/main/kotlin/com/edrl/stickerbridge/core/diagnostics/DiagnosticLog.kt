package com.edrl.stickerbridge.core.diagnostics

/**
 * Port: technical log for the maintainer (NFR12). Messages never include personal data such as
 * comment texts or user names.
 */
fun interface DiagnosticLog {
    fun event(
        tag: String,
        message: String,
        cause: Throwable?,
    )

    fun event(
        tag: String,
        message: String,
    ) = event(tag, message, null)
}
