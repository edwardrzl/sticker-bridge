package com.edrl.stickerbridge.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.edrl.stickerbridge.R
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.ui.components.LikesBadge
import com.edrl.stickerbridge.ui.thumbnails.Thumbnail
import com.edrl.stickerbridge.ui.thumbnails.ThumbnailLoader

private const val PLACEHOLDER_ALPHA = 0.25f

/** The images of a video ordered by likes, to choose, with the ways to find more below them. */
@Composable
internal fun SearchGrid(
    state: SearchState,
    thumbnails: ThumbnailLoader,
    actions: SearchActions,
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.search_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 104.dp),
            modifier = Modifier.weight(1f).testTag("search-grid"),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.images, key = { it.url }) { image ->
                GridItem(image, selected = image.url in state.selected, thumbnails) { actions.onToggle(image.url) }
            }
            if (state.hasMore) {
                item(span = { GridItemSpan(maxLineSpan) }) { MoreButton(MoreSource.Comments, state, actions) }
            }
            if (state.hasMoreReplies) {
                item(span = { GridItemSpan(maxLineSpan) }) { MoreButton(MoreSource.Replies, state, actions) }
            }
        }
        SelectionBar(state.selected.size, actions.onSave)
    }
}

/** Asks for more of [source]: further comments, or replies, read a few comments per tap (FR2.10). */
@Composable
internal fun MoreButton(
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
    FilledTonalButton(
        onClick = if (replies) actions.onLoadReplies else actions.onLoadMore,
        enabled = !state.loading,
        modifier = Modifier.fillMaxWidth().testTag(if (replies) "search-load-replies" else "search-load-more"),
    ) {
        if (replies) Icon(painterResource(R.drawable.ic_forum), contentDescription = null, Modifier.size(18.dp))
        Text(stringResource(label), Modifier.padding(start = if (replies) 8.dp else 0.dp))
    }
}

@Composable
private fun SelectionBar(
    selected: Int,
    onSave: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.search_selected, selected), style = MaterialTheme.typography.titleMedium)
            Button(onClick = onSave, enabled = selected > 0, modifier = Modifier.testTag("search-save")) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, Modifier.size(18.dp))
                Text(stringResource(R.string.search_save), Modifier.padding(start = 8.dp))
            }
        }
    }
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
    val shape = RoundedCornerShape(16.dp)
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else colors.surfaceContainerHigh)
            .border(if (selected) 3.dp else 0.dp, if (selected) colors.primary else colors.surfaceContainerHigh, shape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description }
            .testTag("search-item"),
    ) {
        Thumbnail(
            key = image.thumbnailUrl,
            load = { thumbnails.remote(image.thumbnailUrl) },
            contentDescription = description,
            modifier = Modifier.fillMaxSize().padding(8.dp),
            placeholder = { StickerPlaceholder() },
        )
        LikesBadge(image.likes, Modifier.align(Alignment.BottomStart).padding(6.dp))
        if (selected) SelectedMark(Modifier.align(Alignment.TopEnd).padding(6.dp))
    }
}

/** Shown while a thumbnail loads, and when it cannot be loaded. */
@Composable
internal fun StickerPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            painterResource(R.drawable.ic_sticker),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PLACEHOLDER_ALPHA),
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun SelectedMark(modifier: Modifier = Modifier) {
    Box(
        modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp),
        )
    }
}
