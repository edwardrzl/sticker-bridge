package com.edrl.stickerbridge.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edrl.stickerbridge.StickerBridgeApp
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.di.AppContainer
import com.edrl.stickerbridge.pack.whatsapp.WhatsAppContract
import com.edrl.stickerbridge.ui.entry.EntryScreen
import com.edrl.stickerbridge.ui.packs.PacksActions
import com.edrl.stickerbridge.ui.packs.PacksScreen
import com.edrl.stickerbridge.ui.packs.PacksViewModel
import com.edrl.stickerbridge.ui.search.SearchActions
import com.edrl.stickerbridge.ui.search.SearchPhase
import com.edrl.stickerbridge.ui.search.SearchScreen
import com.edrl.stickerbridge.ui.search.SearchViewModel
import kotlinx.coroutines.flow.MutableStateFlow

/** The only activity. Receives TikTok's Share action (FR1.1) and hosts the three screens. */
class MainActivity : ComponentActivity() {
    private val sharedText = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) sharedText.value = intent.sharedText()
        val container = (application as StickerBridgeApp).container
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                StickerBridge(container, sharedText)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedText.value = intent.sharedText()
    }

    private fun Intent.sharedText(): String? =
        if (action ==
            Intent.ACTION_SEND
        ) {
            getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
}

private enum class Screen { Entry, Search, Packs }

/** Which screen is shown, and the moves between screens. */
private class Navigator(
    val search: SearchViewModel,
    val packs: PacksViewModel,
) {
    var screen by mutableStateOf(Screen.Entry)
    private var lastSearch = ""

    fun startSearch(text: String) {
        lastSearch = text
        search.search(text)
        if (search.state.value.phase != SearchPhase.InvalidLink) screen = Screen.Search
    }

    fun retry() = startSearch(lastSearch)

    fun showSearch() {
        screen = Screen.Search
    }

    fun openPacks() {
        packs.refresh()
        screen = Screen.Packs
    }

    fun toEntry() {
        if (screen == Screen.Search) search.cancel()
        screen = Screen.Entry
    }
}

@Composable
private fun StickerBridge(
    container: AppContainer,
    sharedText: MutableStateFlow<String?>,
) {
    val search =
        viewModel {
            SearchViewModel(
                container.extractor,
                container.saveStickers,
                WhatsAppConfirmations(container.publisher),
            )
        }
    val packs =
        viewModel {
            PacksViewModel(
                container.packService,
                container.publisher,
                WhatsAppConfirmations(container.publisher),
            )
        }
    val navigator = remember(search, packs) { Navigator(search, packs) }

    val shared by sharedText.collectAsStateWithLifecycle()
    LaunchedEffect(shared) {
        shared?.let {
            navigator.startSearch(it)
            sharedText.value = null
        }
    }

    val pickImages =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                search.saveImported(uris.map { it.toString() })
                navigator.showSearch()
            }
        }
    val importImages = { pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    WhatsAppScreens(search.confirmations, onFinished = packs::refresh)
    WhatsAppScreens(packs.confirmations, onFinished = packs::refresh)
    BackHandler(enabled = navigator.screen != Screen.Entry, onBack = navigator::toEntry)

    Scaffold { padding -> Content(navigator, container, importImages, Modifier.padding(padding)) }
}

@Composable
private fun Content(
    navigator: Navigator,
    container: AppContainer,
    onImport: () -> Unit,
    modifier: Modifier,
) {
    val searchState by navigator.search.state.collectAsStateWithLifecycle()
    val packsState by navigator.packs.state.collectAsStateWithLifecycle()
    when (navigator.screen) {
        Screen.Entry ->
            EntryScreen(
                invalidLink = searchState.phase == SearchPhase.InvalidLink,
                onSearch = navigator::startSearch,
                onImport = onImport,
                onOpenPacks = navigator::openPacks,
                modifier = modifier,
            )
        Screen.Search ->
            SearchScreen(
                state = searchState,
                thumbnails = container.thumbnails,
                actions =
                    SearchActions(
                        onToggle = navigator.search::toggle,
                        onSave = navigator.search::saveSelected,
                        onLoadMore = navigator.search::loadMore,
                        onCancel = navigator::toEntry,
                        onRetry = navigator::retry,
                        onImport = onImport,
                        onShowImages = navigator.search::showImages,
                        onBack = navigator::toEntry,
                    ),
                modifier = modifier,
            )
        Screen.Packs ->
            PacksScreen(
                state = packsState,
                thumbnails = container.thumbnails,
                actions =
                    PacksActions(
                        stickerFile = {
                            pack,
                            sticker,
                            ->
                            container.packRepository.fileOf(pack.identifier, sticker.fileName)
                        },
                        onOpenInWhatsApp = navigator.packs::openInWhatsApp,
                        onRemove = { pack, sticker -> navigator.packs.remove(pack.identifier, sticker.id) },
                        onBack = navigator::toEntry,
                    ),
                modifier = modifier,
            )
    }
}

/** Opens WhatsApp's add or update screen for each queued pack, one after another (FR5.5, FR5.6). */
@Composable
private fun WhatsAppScreens(
    confirmations: WhatsAppConfirmations,
    onFinished: () -> Unit,
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            confirmations.onFinished()
            onFinished()
        }
    val current: StickerPack? by confirmations.current.collectAsStateWithLifecycle()
    LaunchedEffect(current) {
        val pack = current ?: return@LaunchedEffect
        confirmations.onLaunched()
        try {
            launcher.launch(WhatsAppContract.addPackIntent(pack))
        } catch (_: ActivityNotFoundException) {
            confirmations.onFinished()
        }
    }
}
