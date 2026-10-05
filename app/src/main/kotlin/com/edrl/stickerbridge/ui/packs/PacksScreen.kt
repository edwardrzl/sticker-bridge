package com.edrl.stickerbridge.ui.packs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edrl.stickerbridge.R
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackStatus
import com.edrl.stickerbridge.core.pack.Sticker
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.ui.components.TitledScreen
import com.edrl.stickerbridge.ui.search.StickerPlaceholder
import com.edrl.stickerbridge.ui.thumbnails.Thumbnail
import com.edrl.stickerbridge.ui.thumbnails.ThumbnailLoader

/** P3 — the packs with their WhatsApp status; tapping a sticker offers to remove it (FR5.8, FR5.11). */
@Composable
fun PacksScreen(
    state: PacksState,
    thumbnails: ThumbnailLoader,
    actions: PacksActions,
    modifier: Modifier = Modifier,
) {
    var toRemove by remember { mutableStateOf<Pair<StickerPack, Sticker>?>(null) }
    TitledScreen(stringResource(R.string.packs_title), actions.onBack, backTag = "packs-back", modifier) {
        when {
            state.loading -> Notice { CircularProgressIndicator() }
            state.packs.isEmpty() -> Notice { Text(stringResource(R.string.packs_empty), textAlign = TextAlign.Center) }
            else ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.packs_remove_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(state.packs, key = { it.pack.identifier }) { item ->
                        PackCard(item, thumbnails, actions, onSelect = { toRemove = item.pack to it })
                    }
                }
        }
    }
    toRemove?.let { (pack, sticker) ->
        RemoveDialog(onConfirm = { actions.onRemove(pack, sticker) }, onClose = { toRemove = null })
    }
}

@Composable
private fun Notice(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun RemoveDialog(
    onConfirm: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.packs_remove_title)) },
        text = { Text(stringResource(R.string.packs_remove_message)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onClose()
            }) { Text(stringResource(R.string.packs_remove_confirm)) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.search_cancel)) } },
    )
}

@Composable
private fun PackCard(
    item: PackItem,
    thumbnails: ThumbnailLoader,
    actions: PacksActions,
    onSelect: (Sticker) -> Unit,
) {
    val pack = item.pack
    val added = item.status == PackStatus.Added
    val kind =
        stringResource(
            if (pack.series ==
                PackSeries.ANIMATED
            ) {
                R.string.packs_animated
            } else {
                R.string.packs_static
            },
        )
    Card(Modifier.fillMaxWidth().testTag("packs-card")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(pack.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.packs_count, pack.stickers.size, kind),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusLabel(added)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pack.stickers, key = { it.id }) { sticker ->
                    val file = actions.stickerFile(pack, sticker)
                    Thumbnail(
                        key = file?.path ?: sticker.id,
                        load = { file?.let { thumbnails.local(it) } },
                        contentDescription = stringResource(R.string.packs_sticker_description),
                        modifier = Modifier.stickerTile().clickable { onSelect(sticker) }.testTag("packs-sticker"),
                        placeholder = { StickerPlaceholder() },
                    )
                }
            }
            val whatsApp = Modifier.fillMaxWidth().testTag("packs-whatsapp")
            if (added) {
                FilledTonalButton(onClick = { actions.onOpenInWhatsApp(pack) }, modifier = whatsApp) {
                    Text(stringResource(R.string.packs_update))
                }
            } else {
                Button(onClick = { actions.onOpenInWhatsApp(pack) }, modifier = whatsApp) {
                    Text(stringResource(R.string.packs_add))
                }
            }
        }
    }
}

@Composable
private fun Modifier.stickerTile(): Modifier =
    size(72.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerLowest)

/** Whether the pack is already in WhatsApp, as a small coloured label. */
@Composable
private fun StatusLabel(added: Boolean) {
    val colors = MaterialTheme.colorScheme
    Text(
        stringResource(if (added) R.string.packs_status_added else R.string.packs_status_ready),
        style = MaterialTheme.typography.labelMedium,
        color = if (added) colors.onPrimaryContainer else colors.onSecondaryContainer,
        modifier =
            Modifier
                .background(if (added) colors.primaryContainer else colors.secondaryContainer, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
