package com.edrl.stickerbridge.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edrl.stickerbridge.R
import com.edrl.stickerbridge.core.extraction.ExtractionError
import com.edrl.stickerbridge.core.save.PackAction
import com.edrl.stickerbridge.core.save.SaveResult
import com.edrl.stickerbridge.ui.components.TitledScreen
import com.edrl.stickerbridge.ui.thumbnails.ThumbnailLoader

/** P2 — the comment images of a video, ordered by likes, to choose and save (FR3, FR5.9). */
@Composable
fun SearchScreen(
    state: SearchState,
    thumbnails: ThumbnailLoader,
    actions: SearchActions,
    modifier: Modifier = Modifier,
) {
    TitledScreen(stringResource(R.string.search_title), actions.onBack, backTag = "search-back", modifier) {
        when (val phase = state.phase) {
            SearchPhase.Idle, SearchPhase.InvalidLink -> Unit
            SearchPhase.Searching -> Searching(actions.onCancel)
            is SearchPhase.Failed -> Failure(errorText(phase.error), actions.onRetry, actions.onImport)
            SearchPhase.Loaded ->
                if (state.images.isEmpty()) Empty(state, actions) else SearchGrid(state, thumbnails, actions)
            is SearchPhase.Saving -> Centered(stringResource(R.string.search_saving, phase.done, phase.total))
            is SearchPhase.Saved -> Summary(phase.result, canChooseMore = state.images.isNotEmpty(), actions)
            SearchPhase.StorageFailed -> Failure(stringResource(R.string.error_storage), actions.onShowImages, null)
        }
    }
}

@Composable
private fun Searching(onCancel: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.search_loading), textAlign = TextAlign.Center)
        OutlinedButton(onClick = onCancel, modifier = Modifier.testTag("search-cancel")) {
            Text(stringResource(R.string.search_cancel))
        }
    }
}

@Composable
private fun Empty(
    state: SearchState,
    actions: SearchActions,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.search_empty), textAlign = TextAlign.Center)
        if (state.hasMore) MoreButton(MoreSource.Comments, state, actions)
        if (state.hasMoreReplies) MoreButton(MoreSource.Replies, state, actions)
        OutlinedButton(onClick = actions.onImport) { Text(stringResource(R.string.entry_import)) }
    }
}

@Composable
private fun Failure(
    message: String,
    onRetry: () -> Unit,
    onImport: (() -> Unit)?,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Button(onClick = onRetry, modifier = Modifier.testTag("search-retry")) { Text(stringResource(R.string.retry)) }
        if (onImport != null) OutlinedButton(onClick = onImport) { Text(stringResource(R.string.entry_import)) }
    }
}

@Composable
private fun Summary(
    result: SaveResult,
    canChooseMore: Boolean,
    actions: SearchActions,
) {
    val notes = MaterialTheme.typography.bodyMedium
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            painterResource(R.drawable.ic_check_circle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Text(stringResource(R.string.search_saved_title), style = MaterialTheme.typography.headlineMedium)
        result.saved.forEach { (pack, count) ->
            Text(stringResource(R.string.search_saved_in, count, pack), style = MaterialTheme.typography.titleMedium)
        }
        if (result.withoutAnimation > 0) {
            Text(stringResource(R.string.search_without_animation, result.withoutAnimation), style = notes)
        }
        if (result.failed > 0) Text(stringResource(R.string.search_failed, result.failed), style = notes)
        if (!result.whatsAppInstalled) {
            Text(stringResource(R.string.whatsapp_not_installed), style = notes)
        } else if (result.actions.any { it is PackAction.OpenWhatsApp }) {
            Text(stringResource(R.string.search_whatsapp_hint), style = notes)
        }
        if (canChooseMore) {
            Button(onClick = actions.onShowImages, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.search_back_to_grid))
            }
        }
        OutlinedButton(onClick = actions.onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.search_new_video))
        }
    }
}

@Composable
private fun Centered(text: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator()
        Text(text)
    }
}

@Composable
private fun errorText(error: ExtractionError): String =
    stringResource(
        when (error) {
            ExtractionError.NoConnection -> R.string.error_no_connection
            ExtractionError.PostUnavailable -> R.string.error_post_unavailable
            ExtractionError.CommentsBlocked -> R.string.error_comments_blocked
            ExtractionError.Timeout -> R.string.error_timeout
            is ExtractionError.UnexpectedFormat -> R.string.error_unexpected_format
        },
    )
