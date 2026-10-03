package com.edrl.stickerbridge.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edrl.stickerbridge.StickerBridgeApp
import com.edrl.stickerbridge.ui.skeleton.SkeletonScreen
import com.edrl.stickerbridge.ui.skeleton.SkeletonViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as StickerBridgeApp).container
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                SkeletonScreen(viewModel { SkeletonViewModel(container) })
            }
        }
    }
}
