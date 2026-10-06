package com.edrl.stickerbridge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackStatus
import com.edrl.stickerbridge.core.pack.Sticker
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.save.SaveResult
import com.edrl.stickerbridge.ui.entry.EntryScreen
import com.edrl.stickerbridge.ui.packs.PackItem
import com.edrl.stickerbridge.ui.packs.PacksActions
import com.edrl.stickerbridge.ui.packs.PacksScreen
import com.edrl.stickerbridge.ui.packs.PacksState
import com.edrl.stickerbridge.ui.search.SearchActions
import com.edrl.stickerbridge.ui.search.SearchPhase
import com.edrl.stickerbridge.ui.search.SearchScreen
import com.edrl.stickerbridge.ui.search.SearchState
import com.edrl.stickerbridge.ui.theme.StickerBridgeTheme
import com.edrl.stickerbridge.ui.thumbnails.ThumbnailLoader
import okhttp3.OkHttpClient

private const val PHONE = "spec:width=360dp,height=760dp,dpi=420"

private val thumbnails = ThumbnailLoader(OkHttpClient())

private val searchActions = SearchActions({}, {}, {}, {}, {}, {}, {}, {}, {})

private val images =
    listOf(419_937L, 168_241L, 3_340L, 479L, 38L, 12L, 5L, 4L, 2L, 1L, 0L).mapIndexed { index, likes ->
        CommentImage("https://example.invalid/$index.webp", "https://example.invalid/$index.webp", likes)
    }

private fun pack(
    series: PackSeries,
    stickers: Int,
) = StickerPack(
    identifier = "pack_${series.name}",
    name = if (series == PackSeries.ANIMATED) "TikTok animados 1" else "TikTok estáticos 1",
    publisher = "TikTok Stickers",
    series = series,
    number = 1,
    imageDataVersion = 1,
    stickers = List(stickers) { Sticker("s$it", "s$it.webp", "😀", 40_000) },
)

private val gridState =
    SearchState(
        SearchPhase.Loaded,
        images,
        selected = setOf(images[0].url, images[3].url),
        hasMore = true,
        hasMoreReplies = true,
    )

private val packsState =
    PacksState(
        listOf(
            PackItem(pack(PackSeries.ANIMATED, 7), PackStatus.Added),
            PackItem(pack(PackSeries.STATIC, 2), PackStatus.ReadyToAdd),
        ),
        loading = false,
    )

private val packsActions = PacksActions({ _, _ -> null }, {}, { _, _ -> }, {})

@Composable
private fun Screen(
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    StickerBridgeTheme(dark) { Surface(color = MaterialTheme.colorScheme.background) { content() } }
}

@PreviewTest
@Preview
@Composable
fun LauncherIconPreview() =
    Box(Modifier.size(108.dp).clip(CircleShape).background(colorResource(R.color.launcher_background))) {
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null)
    }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun GridDarkPreview() = Screen(dark = true) { SearchScreen(gridState, thumbnails, searchActions) }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun EntryDarkPreview() =
    Screen(dark = true) { EntryScreen(invalidLink = true, onSearch = {}, onImport = {}, onOpenPacks = {}) }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun PacksDarkPreview() = Screen(dark = true) { PacksScreen(packsState, thumbnails, packsActions) }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun EntryPreview() = Screen { EntryScreen(invalidLink = false, onSearch = {}, onImport = {}, onOpenPacks = {}) }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun SearchingPreview() = Screen { SearchScreen(SearchState(SearchPhase.Searching), thumbnails, searchActions) }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun GridPreview() = Screen { SearchScreen(gridState, thumbnails, searchActions) }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun SavedPreview() =
    Screen {
        SearchScreen(
            SearchState(
                SearchPhase.Saved(SaveResult(mapOf("TikTok animados 1" to 2), 1, 0, emptyList(), true)),
                images,
            ),
            thumbnails,
            searchActions,
        )
    }

@PreviewTest
@Preview(device = PHONE)
@Composable
fun PacksPreview() = Screen { PacksScreen(packsState, thumbnails, packsActions) }
