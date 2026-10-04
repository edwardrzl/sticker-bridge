package com.edrl.stickerbridge.core.link

/**
 * A link to a public TikTok post (video or photo) whose comments are read.
 *
 * Recognising the different TikTok link shapes is added with the link parser in unit U2.
 */
@JvmInline
value class PostLink(
    val url: String,
)
