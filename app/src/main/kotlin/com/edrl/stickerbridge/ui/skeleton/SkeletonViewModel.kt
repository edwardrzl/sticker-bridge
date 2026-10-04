package com.edrl.stickerbridge.ui.skeleton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edrl.stickerbridge.core.conversion.ConversionResult
import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.link.PostLink
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
    val canAddAnother: Boolean = false,
    /** A pack waiting for WhatsApp's add confirmation, launched by the screen. */
    val pendingAdd: StickerPack? = null,
)

/**
 * Walking skeleton (U1): link → first comment image → static sticker → pack → WhatsApp, with the
 * time of each step shown on screen and in the diagnostic log. Replaced by the real screens in U5.
 */
class SkeletonViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SkeletonState())
    val state: StateFlow<SkeletonState> = mutableState.asStateFlow()

    private var lastImage: CommentImage? = null

    fun onLinkChange(link: String) = mutableState.update { it.copy(link = link) }

    fun run() =
        launchStep {
            mutableState.update { it.copy(lines = emptyList(), canAddAnother = false) }
            val images = extractImages().take(MAX_IMAGES_PER_RUN)
            lastImage = images.firstOrNull()
            images.forEach { convertAndStore(it) }
        }

    fun addAnother() = launchStep { lastImage?.let { convertAndStore(it) } }

    fun onAddLaunched() = mutableState.update { it.copy(pendingAdd = null) }

    fun onAddResult(accepted: Boolean) =
        report(if (accepted) "WhatsApp confirmó el paquete" else "WhatsApp no confirmó el paquete")

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

    private suspend fun extractImages(): List<CommentImage> {
        val mark = TimeSource.Monotonic.markNow()
        container.extractor.open(PostLink(state.value.link.trim())).use { session ->
            return when (val outcome = session.loadInitial()) {
                is ExtractionOutcome.Loaded -> {
                    val images = outcome.page.images
                    report("Imágenes encontradas: ${images.size} (${mark.elapsedNow().inWholeMilliseconds} ms)")
                    images
                }
                is ExtractionOutcome.Failed -> {
                    report("La extracción falló: ${outcome.error} (${mark.elapsedNow().inWholeMilliseconds} ms)")
                    emptyList()
                }
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
            "Sticker $kind: ${converted.file.sizeBytes / BYTES_PER_KB} KB " +
                "(${mark.elapsedNow().inWholeMilliseconds} ms)",
        )

        var pack: StickerPack? = null
        stickersToReachMinimum(container.packService, converted).forEach { pack = container.packService.addSticker(it) }
        pack?.let { deliver(it) }
        mutableState.update { it.copy(canAddAnother = true) }
    }

    private suspend fun deliver(pack: StickerPack) {
        report("Paquete ${pack.name}: ${pack.stickers.size} stickers, versión ${pack.imageDataVersion}")
        val publisher = container.publisher
        when {
            !publisher.isWhatsAppInstalled() -> report("WhatsApp no está instalado")
            publisher.isAdded(pack.identifier) -> {
                publisher.notifyChanged(pack)
                report("El paquete ya está en WhatsApp: comprueba si aparece el sticker nuevo")
            }
            PackValidator.validate(pack).isNotEmpty() -> report("Paquete inválido: ${PackValidator.validate(pack)}")
            else -> mutableState.update { it.copy(pendingAdd = pack) }
        }
    }

    private fun report(line: String) {
        container.diagnostics.event("skeleton", line)
        mutableState.update { it.copy(lines = it.lines + line) }
    }

    private companion object {
        const val BYTES_PER_KB = 1024

        /** The skeleton converts at most this many of the images found, with no selection screen. */
        const val MAX_IMAGES_PER_RUN = 5
    }
}

/** The skeleton repeats its single sticker until the first pack reaches WhatsApp's minimum of 3. */
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
