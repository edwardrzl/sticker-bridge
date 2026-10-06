package com.edrl.stickerbridge.ui.packs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.edrl.stickerbridge.R
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackStatus
import com.edrl.stickerbridge.core.pack.Sticker
import com.edrl.stickerbridge.core.pack.StickerPack
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
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = actions.onBack,
                modifier = Modifier.testTag("packs-back"),
            ) { Text(stringResource(R.string.back)) }
            Text(stringResource(R.string.packs_title), style = MaterialTheme.typography.titleLarge)
        }
        when {
            state.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
            state.packs.isEmpty() -> Text(stringResource(R.string.packs_empty), Modifier.padding(24.dp))
            else ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding =
                        androidx.compose.foundation.layout
                            .PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.packs_remove_hint),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    items(state.packs, key = { it.pack.identifier }) { item ->
                        PackCard(item, thumbnails, actions, onSelect = {
                            toRemove =
                                item.pack to it
                        })
                    }
                }
        }
    }
    toRemove?.let { (pack, sticker) ->
        AlertDialog(
            onDismissRequest = { toRemove = null },
            title = { Text(stringResource(R.string.packs_remove_title)) },
            text = { Text(stringResource(R.string.packs_remove_message)) },
            confirmButton = {
                TextButton(onClick = {
                    actions.onRemove(pack, sticker)
                    toRemove = null
                }) { Text(stringResource(R.string.packs_remove_confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { toRemove = null },
                ) { Text(stringResource(R.string.search_cancel)) }
            },
        )
    }
}

@Composable
private fun PackCard(
    item: PackItem,
    thumbnails: ThumbnailLoader,
    actions: PacksActions,
    onSelect: (Sticker) -> Unit,
) {
    val pack = item.pack
    Card(Modifier.fillMaxWidth().testTag("packs-card")) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(pack.name, style = MaterialTheme.typography.titleMedium)
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
            Text(
                stringResource(R.string.packs_count, pack.stickers.size, kind),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(statusText(item.status), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(pack.stickers, key = { it.id }) { sticker ->
                    val file = actions.stickerFile(pack, sticker)
                    Thumbnail(
                        key = file?.path ?: sticker.id,
                        load = { file?.let { thumbnails.local(it) } },
                        contentDescription = stringResource(R.string.packs_sticker_description),
                        modifier =
                            Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onSelect(sticker) }
                                .testTag("packs-sticker"),
                    )
                }
            }
            Button(onClick = { actions.onOpenInWhatsApp(pack) }, modifier = Modifier.testTag("packs-whatsapp")) {
                Text(
                    stringResource(
                        if (item.status == PackStatus.Added) R.string.packs_update else R.string.packs_add,
                    ),
                )
            }
        }
    }
}

@Composable
private fun statusText(status: PackStatus): String =
    when (status) {
        PackStatus.ReadyToAdd -> stringResource(R.string.packs_status_ready)
        PackStatus.Added -> stringResource(R.string.packs_status_added)
    }
