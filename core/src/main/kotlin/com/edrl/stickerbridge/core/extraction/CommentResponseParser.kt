package com.edrl.stickerbridge.core.extraction

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

sealed interface ParsedComments {
    data class Parsed(
        val images: List<CommentImage>,
        val hasMore: Boolean,
    ) : ParsedComments

    data class Malformed(
        val detail: String,
    ) : ParsedComments
}

/**
 * Reads one response of TikTok's comment list and returns the images attached to its comments.
 *
 * The response format is not documented by TikTok, so reading is tolerant: unknown fields are
 * ignored, comments without images are skipped, and only a missing comment list is an error.
 */
class CommentResponseParser {
    fun parse(body: String): ParsedComments {
        val root = parseObject(body)
        val comments = root?.let(::commentsOf)
        return when {
            root == null -> ParsedComments.Malformed("response is not a JSON object")
            comments == null -> ParsedComments.Malformed("response has no '$COMMENTS' list")
            else ->
                ParsedComments.Parsed(
                    images = comments.flatMap(::imagesOf).distinctBy { it.url },
                    hasMore = isTrue(root[HAS_MORE]),
                )
        }
    }

    private fun parseObject(body: String): JsonObject? =
        try {
            Json.parseToJsonElement(body) as? JsonObject
        } catch (_: SerializationException) {
            null
        }

    /** The comment list; an explicit null means "no comments", a missing field means an unknown format. */
    private fun commentsOf(root: JsonObject): List<JsonElement>? =
        when (val value = root[COMMENTS]) {
            is JsonNull -> emptyList()
            is JsonArray -> value
            else -> null
        }

    /** Photos come in `image_list`, stickers in `cmt_sticker_struct`; a comment may have either. */
    private fun imagesOf(comment: JsonElement): List<CommentImage> {
        val obj = comment as? JsonObject ?: return emptyList()
        return photosOf(obj) + listOfNotNull(stickerOf(obj))
    }

    private fun photosOf(comment: JsonObject): List<CommentImage> {
        val imageList = comment[IMAGE_LIST] as? JsonArray ?: return emptyList()
        return imageList.mapNotNull { entry ->
            val image = entry as? JsonObject ?: return@mapNotNull null
            val original = firstUrl(image[ORIGIN_URL]) ?: return@mapNotNull null
            CommentImage(url = original, thumbnailUrl = firstUrl(image[CROP_URL]) ?: original)
        }
    }

    /** The animated file when there is one (it keeps the animation), otherwise the static one. */
    private fun stickerOf(comment: JsonObject): CommentImage? {
        val sticker = comment[STICKER] as? JsonObject ?: return null
        val animated = sticker[ANIMATED_URL] as? JsonObject
        val static = sticker[STATIC_URL] as? JsonObject
        val url = firstUrl(animated?.get(HIGH_RESOLUTION)) ?: firstUrl(static?.get(HIGH_RESOLUTION))
        return url?.let { CommentImage(url = it, thumbnailUrl = firstUrl(static?.get(LOW_RESOLUTION)) ?: it) }
    }

    private fun firstUrl(element: JsonElement?): String? {
        val urls = (element as? JsonObject)?.get(URL_LIST) as? JsonArray ?: return null
        return urls.firstNotNullOfOrNull { (it as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank) }
    }

    private fun isTrue(element: JsonElement?): Boolean {
        val primitive = element as? JsonPrimitive ?: return false
        return primitive.intOrNull?.let { it != 0 } ?: primitive.booleanOrNull ?: false
    }

    private companion object {
        const val COMMENTS = "comments"
        const val HAS_MORE = "has_more"
        const val IMAGE_LIST = "image_list"
        const val ORIGIN_URL = "origin_url"
        const val CROP_URL = "crop_url"
        const val URL_LIST = "url_list"
        const val STICKER = "cmt_sticker_struct"
        const val ANIMATED_URL = "animated_url"
        const val STATIC_URL = "static_url"
        const val HIGH_RESOLUTION = "high_resolution_url"
        const val LOW_RESOLUTION = "low_resolution_url"
    }
}
