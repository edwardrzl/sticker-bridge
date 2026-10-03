package com.edrl.stickerbridge

import android.app.Application
import com.edrl.stickerbridge.di.AppContainer

class StickerBridgeApp : Application() {
    /** Lazy because WhatsApp's content provider may be created before [onCreate] runs. */
    val container: AppContainer by lazy { AppContainer(this) }
}
