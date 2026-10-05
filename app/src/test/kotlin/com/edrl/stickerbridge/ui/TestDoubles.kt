package com.edrl.stickerbridge.ui

import com.edrl.stickerbridge.core.conversion.ConversionResult
import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import com.edrl.stickerbridge.core.conversion.EncodedFile
import com.edrl.stickerbridge.core.conversion.ImageConverter
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.core.extraction.CommentImageExtractor
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.extraction.ExtractionPage
import com.edrl.stickerbridge.core.extraction.ExtractionSession
import com.edrl.stickerbridge.core.link.PostLink
import com.edrl.stickerbridge.core.pack.PackFile
import com.edrl.stickerbridge.core.pack.PackRepository
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackService
import com.edrl.stickerbridge.core.pack.StagedFile
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StickerPackPublisher

fun image(
    name: String,
    likes: Long,
) = CommentImage("https://p16.tiktokcdn.com/$name", "https://p16.tiktokcdn.com/$name", likes)

/** An extractor whose sessions answer with the queued outcomes, in order. */
class FakeExtractor(
    vararg outcomes: ExtractionOutcome,
) : CommentImageExtractor {
    private val pending = ArrayDeque(outcomes.toList())
    val opened = mutableListOf<PostLink>()
    var closed = 0

    override fun open(link: PostLink): ExtractionSession {
        opened += link
        return object : ExtractionSession {
            override suspend fun loadInitial() = pending.removeFirst()

            override suspend fun loadMore() = pending.removeFirst()

            override suspend fun loadReplies() = pending.removeFirst()

            override fun close() {
                closed++
            }
        }
    }
}

fun loaded(
    vararg images: CommentImage,
    hasMore: Boolean = false,
    hasMoreReplies: Boolean = false,
) = ExtractionOutcome.Loaded(ExtractionPage(images.toList(), hasMore, hasMoreReplies))

class InMemoryPackRepository : PackRepository {
    var packs: List<StickerPack> = emptyList()

    override suspend fun load() = packs

    override suspend fun commit(
        packs: List<StickerPack>,
        staged: List<StagedFile>,
        obsolete: List<PackFile>,
    ) {
        this.packs = packs
    }
}

class FakePublisher : StickerPackPublisher {
    val added = mutableSetOf<String>()
    val notified = mutableListOf<String>()

    override fun isWhatsAppInstalled() = true

    override suspend fun isAdded(identifier: String) = identifier in added

    override fun notifyChanged(pack: StickerPack) {
        notified += pack.identifier
    }
}

/** Every image converts to a static sticker; the converted URLs are recorded in order. */
class RecordingConverter : ImageConverter {
    val converted = mutableListOf<ImageRef>()

    override suspend fun convert(ref: ImageRef): ConversionResult {
        converted += ref
        return ConversionResult.Converted(
            ConvertedSticker(EncodedFile("${converted.size}.webp", 10_000), PackSeries.STATIC, false),
        )
    }
}

fun packService(repository: PackRepository): PackService {
    var id = 0
    return PackService(repository, { EncodedFile("tray.png", 1_000) }, { "id${id++}" })
}
