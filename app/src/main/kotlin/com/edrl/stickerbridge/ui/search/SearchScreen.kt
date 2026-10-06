package com.edrl.stickerbridge.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.edrl.stickerbridge.R
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.core.extraction.ExtractionError
import com.edrl.stickerbridge.core.save.PackAction
import com.edrl.stickerbridge.core.save.SaveResult
import com.edrl.stickerbridge.ui.thumbnails.Thumbnail
import com.edrl.stickerbridge.ui.thumbnails.ThumbnailLoader

/** P2 — the comment images of a video, ordered by likes, to choose and save (FR3, FR5.9). */
@Composable
fun SearchScreen(
    state: SearchState,
    thumbnails: ThumbnailLoader,
    actions: SearchActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = actions.onBack, modifier = Modifier.testTag("search-back")) {
                Text(stringResource(R.string.back))
            }
            Text(stringResource(R.string.search_title), style = MaterialTheme.typography.titleLarge)
        }
        when (val phase = state.phase) {
            SearchPhase.Idle, SearchPhase.InvalidLink -> Unit
            SearchPhase.Searching -> Searching(actions.onCancel)
            is SearchPhase.Failed -> Failure(errorText(phase.error), actions.onRetry, actions.onImport)
            SearchPhase.Loaded ->
                if (state.images.isEmpty()) Empty(state, actions) else Grid(state, thumbnails, actions)
            is SearchPhase.Saving -> Centered(stringResource(R.string.search_saving, phase.done, phase.total))
            is SearchPhase.Saved -> Summary(phase.result, canChooseMore = state.images.isNotEmpty(), actions)
            SearchPhase.StorageFailed -> Failure(stringResource(R.string.error_storage), actions.onShowImages, null)
        }
    }
}

@Composable
private fun Grid(
    state: SearchState,
    thumbnails: ThumbnailLoader,
    actions: SearchActions,
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.search_hint),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 104.dp),
            modifier = Modifier.weight(1f).testTag("search-grid"),
            contentPadding =
                androidx.compose.foundation.layout
                    .PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.images, key = { it.url }) { image ->
                GridItem(
                    image,
                    selected = image.url in state.selected,
                    thumbnails,
                    onClick = { actions.onToggle(image.url) },
                )
            }
            if (state.hasMore) {
                item(span = { GridItemSpan(maxLineSpan) }) { MoreButton(MoreSource.Comments, state, actions) }
            }
            if (state.hasMoreReplies) {
                item(span = { GridItemSpan(maxLineSpan) }) { MoreButton(MoreSource.Replies, state, actions) }
            }
        }
        Surface(tonalElevation = 3.dp) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.search_selected, state.selected.size))
                Button(
                    onClick = actions.onSave,
                    enabled = state.selected.isNotEmpty(),
                    modifier = Modifier.testTag("search-save"),
                ) { Text(stringResource(R.string.search_save)) }
            }
        }
    }
}

/** Asks for more of [source]: further comments, or replies, read a few comments per tap (FR2.10). */
@Composable
private fun MoreButton(
    source: MoreSource,
    state: SearchState,
    actions: SearchActions,
) {
    val replies = source == MoreSource.Replies
    val label =
        when {
            replies && state.loadingReplies -> R.string.search_loading_replies
            replies -> R.string.search_load_replies
            state.loadingMore -> R.string.search_loading_more
            else -> R.string.search_load_more
        }
    OutlinedButton(
        onClick = if (replies) actions.onLoadReplies else actions.onLoadMore,
        enabled = !state.loading,
        modifier = Modifier.fillMaxWidth().testTag(if (replies) "search-load-replies" else "search-load-more"),
    ) { Text(stringResource(label)) }
}

@Composable
private fun GridItem(
    image: CommentImage,
    selected: Boolean,
    thumbnails: ThumbnailLoader,
    onClick: () -> Unit,
) {
    val description =
        stringResource(
            if (selected) R.string.search_item_description_selected else R.string.search_item_description,
            image.likes.toInt(),
        )
    val shape = RoundedCornerShape(12.dp)
    val border =
        if (selected) {
            BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(shape)
            .border(border, shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description }
            .testTag("search-item"),
    ) {
        Thumbnail(
            key = image.thumbnailUrl,
            load = { thumbnails.remote(image.thumbnailUrl) },
            contentDescription = description,
            modifier = Modifier.fillMaxSize().padding(6.dp),
        )
        Text(
            stringResource(R.string.search_likes, image.likes.toInt()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
        )
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
        Text(stringResource(R.string.search_loading))
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
        Text(stringResource(R.string.search_empty))
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
        Text(message, style = MaterialTheme.typography.titleMedium)
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
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.search_saved_title), style = MaterialTheme.typography.headlineSmall)
        result.saved.forEach { (pack, count) -> Text(stringResource(R.string.search_saved_in, count, pack)) }
        if (result.withoutAnimation >
            0
        ) {
            Text(stringResource(R.string.search_without_animation, result.withoutAnimation))
        }
        if (result.failed > 0) Text(stringResource(R.string.search_failed, result.failed))
        if (!result.whatsAppInstalled) {
            Text(stringResource(R.string.whatsapp_not_installed))
        } else if (result.actions.any { it is PackAction.OpenWhatsApp }) {
            Text(stringResource(R.string.search_whatsapp_hint), style = MaterialTheme.typography.bodySmall)
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
