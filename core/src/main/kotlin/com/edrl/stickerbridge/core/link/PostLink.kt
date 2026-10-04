package com.edrl.stickerbridge.core.link

enum class PostKind {
    VIDEO,
    PHOTO,

    /** A short link that redirects to a video or photo post. */
    SHORT,
}

/** A link to a public TikTok post whose comments are read, normalised to https without parameters. */
data class PostLink(
    val url: String,
    val kind: PostKind,
)

/** Finds the first TikTok post link in a shared or pasted text (BR-01, FR1.3, FR1.4). */
object PostLinkParser {
    private const val HOST = """(?:www\.|m\.)?tiktok\.com"""

    private val forms =
        listOf(
            Regex("""^($HOST/@[\w.-]+/video/\d+)""", RegexOption.IGNORE_CASE) to PostKind.VIDEO,
            Regex("""^($HOST/@[\w.-]+/photo/\d+)""", RegexOption.IGNORE_CASE) to PostKind.PHOTO,
            Regex("""^(m\.tiktok\.com/v/\d+)""", RegexOption.IGNORE_CASE) to PostKind.VIDEO,
            Regex("""^((?:vm|vt)\.tiktok\.com/[\w-]+/?)""", RegexOption.IGNORE_CASE) to PostKind.SHORT,
            Regex("""^($HOST/t/[\w-]+/?)""", RegexOption.IGNORE_CASE) to PostKind.SHORT,
        )

    private val candidates = Regex("""(?:https?://)?[\w.-]*tiktok\.com/\S*""", RegexOption.IGNORE_CASE)

    fun parse(text: String): PostLink? =
        candidates
            .findAll(text)
            .mapNotNull { match ->
                recognise(match.value.replaceFirst(Regex("^https?://", RegexOption.IGNORE_CASE), ""))
            }.firstOrNull()

    private fun recognise(addressWithoutScheme: String): PostLink? {
        for ((form, kind) in forms) {
            val path = form.find(addressWithoutScheme)?.groupValues?.get(1) ?: continue
            return PostLink("https://$path", kind)
        }
        return null
    }
}
