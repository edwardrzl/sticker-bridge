package com.edrl.stickerbridge.di

import android.util.Log
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog

/** Writes diagnostics to the device log under one tag, so `adb logcat -s StickerBridge` shows them. */
class AndroidDiagnosticLog : DiagnosticLog {
    override fun event(
        tag: String,
        message: String,
        cause: Throwable?,
    ) {
        Log.i(TAG, "[$tag] $message", cause)
    }

    private companion object {
        const val TAG = "StickerBridge"
    }
}
