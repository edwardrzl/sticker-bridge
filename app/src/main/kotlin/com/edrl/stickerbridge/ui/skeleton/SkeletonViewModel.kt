package com.edrl.stickerbridge.ui.skeleton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edrl.stickerbridge.core.conversion.ConversionResult
import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.extraction.ExtractionSession
import com.edrl.stickerbridge.core.link.PostLinkParser
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackService
import com.edrl.stickerbridge.core.pack.PackValidator
import com.edrl.stickerbridge.core.pack.StickerLimits
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StorageException
import com.edrl.stickerbridge.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.time.TimeSource

data class SkeletonState(
    val link: String = "",
    val running: Boolean = false,
    val lines: List<String> = emptyList(),
    val canLoadMore: Boolean = false,
    /** A pack waiting for WhatsApp's add or update confirmation, launched by the screen. */
    val pendingAdd: StickerPack? = null,
)

/**
 * Provisional test screen: link → comment images ordered by likes → stickers → packs → WhatsApp,
 * with the time of each step on screen and in the diagnostic log. Replaced by the real screens in U5.
 */
class SkeletonViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SkeletonState())
    val state: StateFlow<SkeletonState> = mutableState.asStateFlow()

    /** Kept open after a search so "load more" can continue it. */
    private var session: ExtractionSession? = null

    fun onLinkChange(link: String) = mutableState.update { it.copy(link = link) }

    fun run() =
        launchStep {
            session?.close()
            session = null
            mutableState.update { it.copy(lines = emptyList(), canLoadMore = false) }
            val link = PostLinkParser.parse(state.value.link)
            if (link == null) {
                report("No es un enlace de video de TikTok")
                return@launchStep
            }
            val mark = TimeSource.Monotonic.markNow()
            val opened = container.extractor.open(link).also { session = it }
            val outcome = opened.loadInitial()
            describe(outcome, mark.elapsedNow().inWholeMilliseconds).forEach(::report)
            if (outcome is ExtractionOutcome.Loaded) {
                mutableState.update { it.copy(canLoadMore = outcome.page.hasMore) }
                outcome.page.images
                    .take(MAX_IMAGES_PER_RUN)
                    .forEach { convertAndStore(it) }
            }
        }

    /** Loads one more batch of comments and reports the reordered list (FR2.4, FR2.8). */
    fun loadMore() =
        launchStep {
            val current = session ?: return@launchStep
            val mark = TimeSource.Monotonic.markNow()
            val outcome = current.loadMore()
            describe(outcome, mark.elapsedNow().inWholeMilliseconds).forEach(::report)
            mutableState.update { it.copy(canLoadMore = outcome is ExtractionOutcome.Loaded && outcome.page.hasMore) }
        }

    fun onAddLaunched() = mutableState.update { it.copy(pendingAdd = null) }

    fun onAddResult(accepted: Boolean) =
        report(if (accepted) "WhatsApp confirmó el paquete" else "WhatsApp no confirmó el paquete")

    override fun onCleared() {
        session?.close()
    }

    private fun launchStep(block: suspend () -> Unit) {
        if (state.value.running) return
        viewModelScope.launch {
            mutableState.update { it.copy(running = true) }
            try {
                block()
            } catch (e: StorageException) {
                report("No se pudieron guardar los stickers: ${e.message}")
            } finally {
                mutableState.update { it.copy(running = false) }
            }
        }
    }

    private suspend fun convertAndStore(image: CommentImage) {
        val mark = TimeSource.Monotonic.markNow()
        val converted =
            when (val result = container.converter.convert(ImageRef.Remote(image.url))) {
                is ConversionResult.Converted -> result.sticker
                is ConversionResult.Failed -> {
                    report("La conversión falló: ${result.error}")
                    return
                }
            }
        val kind =
            when {
                converted.series == PackSeries.ANIMATED -> "animado"
                converted.animationDropped -> "estático (se guardó sin animación)"
                else -> "estático"
            }
        report(
            "Sticker $kind (${image.likes} likes): ${converted.file.sizeBytes / BYTES_PER_KB} KB " +
                "(${mark.elapsedNow().inWholeMilliseconds} ms)",
        )

        var pack: StickerPack? = null
        stickersToReachMinimum(container.packService, converted).forEach { pack = container.packService.addSticker(it) }
        pack?.let { deliver(it) }
    }

    private suspend fun deliver(pack: StickerPack) {
        report("Paquete ${pack.name}: ${pack.stickers.size} stickers, versión ${pack.imageDataVersion}")
        val publisher = container.publisher
        when {
            !publisher.isWhatsAppInstalled() -> report("WhatsApp no está instalado")
            PackValidator.validate(pack).isNotEmpty() -> report("Paquete inválido: ${PackValidator.validate(pack)}")
            else -> {
                // WhatsApp only reloads an added pack when its add screen is opened again (FR5.6).
                if (publisher.isAdded(pack.identifier)) publisher.notifyChanged(pack)
                mutableState.update { it.copy(pendingAdd = pack) }
            }
        }
    }

    private fun report(line: String) {
        container.diagnostics.event("skeleton", line)
        mutableState.update { it.copy(lines = it.lines + line) }
    }

    private companion object {
        const val BYTES_PER_KB = 1024

        /** The test screen converts at most this many of the images found, with no selection screen. */
        const val MAX_IMAGES_PER_RUN = 5
    }
}

private fun describe(
    outcome: ExtractionOutcome,
    elapsedMs: Long,
): List<String> =
    when (outcome) {
        is ExtractionOutcome.Loaded ->
            listOf(
                "Imágenes encontradas: ${outcome.page.images.size} ($elapsedMs ms)" +
                    if (outcome.page.hasMore) ", hay más comentarios" else "",
                "Likes, en orden: ${outcome.page.images.joinToString { it.likes.toString() }}",
            )
        is ExtractionOutcome.Failed -> listOf("La extracción falló: ${outcome.error} ($elapsedMs ms)")
    }

/** The test screen repeats a sticker until a new pack reaches WhatsApp's minimum of 3. */
private suspend fun stickersToReachMinimum(
    packService: PackService,
    converted: ConvertedSticker,
): List<ConvertedSticker> {
    val current =
        packService
            .listPacks()
            .filter { it.series == converted.series }
            .maxByOrNull { it.number }
            ?.stickers
            ?.size ?: 0
    val copies = (StickerLimits.MIN_STICKERS - current - 1).coerceAtLeast(0)
    return withContext(Dispatchers.IO) {
        val source = File(converted.file.path)
        listOf(converted) +
            List(copies) {
                val copy = source.copyTo(File(source.parentFile, "${UUID.randomUUID()}.webp"))
                converted.copy(file = converted.file.copy(path = copy.absolutePath))
            }
    }
}
