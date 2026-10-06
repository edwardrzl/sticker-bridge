package com.edrl.stickerbridge.ui.search

/** Callbacks of the video screen. */
data class SearchActions(
    val onToggle: (String) -> Unit,
    val onSave: () -> Unit,
    val onLoadMore: () -> Unit,
    val onCancel: () -> Unit,
    val onRetry: () -> Unit,
    val onImport: () -> Unit,
    val onShowImages: () -> Unit,
    val onBack: () -> Unit,
)
