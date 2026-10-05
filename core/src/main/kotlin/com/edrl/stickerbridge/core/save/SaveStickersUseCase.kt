package com.edrl.stickerbridge.core.save

import com.edrl.stickerbridge.core.conversion.ConversionResult
import com.edrl.stickerbridge.core.conversion.ImageConverter
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.pack.PackService
import com.edrl.stickerbridge.core.pack.PackValidator
import com.edrl.stickerbridge.core.pack.PackViolation
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StickerPackPublisher

/** What to do with WhatsApp for one pack touched by a save (BR-20, revised by FR5.6). */
sealed interface PackAction {
    val pack: StickerPack

    /** Open WhatsApp's screen: it adds a new pack or offers UPDATE for an added one. */
    data class OpenWhatsApp(
        override val pack: StickerPack,
    ) : PackAction

    data class Invalid(
        override val pack: StickerPack,
        val violations: List<PackViolation>,
    ) : PackAction
}

data class SaveResult(
    /** Stickers saved, by pack name. */
    val saved: Map<String, Int>,
    val withoutAnimation: Int,
    val failed: Int,
    val actions: List<PackAction>,
    val whatsAppInstalled: Boolean,
)

/**
 * The main use case: converts the chosen images one after another, adds them to their packs and
 * says what to do with WhatsApp for every pack touched. Extracted and imported images share it.
 */
class SaveStickersUseCase(
    private val converter: ImageConverter,
    private val packs: PackService,
    private val publisher: StickerPackPublisher,
) {
    suspend fun save(
        sources: List<ImageRef>,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): SaveResult {
        val saved = linkedMapOf<String, Int>()
        val affected = linkedMapOf<String, StickerPack>()
        var withoutAnimation = 0
        var failed = 0
        sources.forEachIndexed { index, source ->
            when (val result = converter.convert(source)) {
                is ConversionResult.Failed -> failed++
                is ConversionResult.Converted -> {
                    val added = packs.addSticker(result.sticker)
                    added.changed.forEach { affected[it.identifier] = it }
                    saved.merge(added.target.name, 1, Int::plus)
                    if (result.sticker.animationDropped) withoutAnimation++
                }
            }
            onProgress(index + 1, sources.size)
        }
        return SaveResult(
            saved = saved,
            withoutAnimation = withoutAnimation,
            failed = failed,
            actions = affected.values.map(::actionFor),
            whatsAppInstalled = publisher.isWhatsAppInstalled(),
        )
    }

    private fun actionFor(pack: StickerPack): PackAction {
        val violations = PackValidator.validate(pack)
        return if (violations.isEmpty()) PackAction.OpenWhatsApp(pack) else PackAction.Invalid(pack, violations)
    }
}
