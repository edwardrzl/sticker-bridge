package com.edrl.stickerbridge.pack.whatsapp

import android.content.Intent
import android.net.Uri
import com.edrl.stickerbridge.core.pack.StickerPack

/**
 * Names defined by WhatsApp's sticker integration for third-party apps.
 * Source: https://github.com/WhatsApp/stickers/tree/main/Android
 */
object WhatsAppContract {
    const val PACKAGE = "com.whatsapp"
    const val AUTHORITY = "com.edrl.stickerbridge.stickercontentprovider"

    const val METADATA = "metadata"
    const val STICKERS = "stickers"
    const val STICKERS_ASSET = "stickers_asset"

    const val PACK_IDENTIFIER = "sticker_pack_identifier"
    const val PACK_NAME = "sticker_pack_name"
    const val PACK_PUBLISHER = "sticker_pack_publisher"
    const val PACK_ICON = "sticker_pack_icon"
    const val ANDROID_STORE_LINK = "android_play_store_link"
    const val IOS_STORE_LINK = "ios_app_download_link"
    const val PUBLISHER_EMAIL = "sticker_pack_publisher_email"
    const val PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
    const val PRIVACY_POLICY = "sticker_pack_privacy_policy_website"
    const val LICENSE_AGREEMENT = "sticker_pack_license_agreement_website"
    const val IMAGE_DATA_VERSION = "image_data_version"
    const val AVOID_CACHE = "whatsapp_will_not_cache_stickers"
    const val ANIMATED_PACK = "animated_sticker_pack"

    const val STICKER_FILE_NAME = "sticker_file_name"
    const val STICKER_EMOJI = "sticker_emoji"
    const val STICKER_ACCESSIBILITY_TEXT = "sticker_accessibility_text"

    private const val ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
    private const val EXTRA_PACK_ID = "sticker_pack_id"
    private const val EXTRA_PACK_AUTHORITY = "sticker_pack_authority"
    private const val EXTRA_PACK_NAME = "sticker_pack_name"

    private const val WHITELIST_AUTHORITY = "com.whatsapp.provider.sticker_whitelist_check"
    private const val WHITELIST_PATH = "is_whitelisted"
    const val WHITELIST_RESULT = "result"

    /** Opens WhatsApp's own confirmation to add [pack]. Must be launched from an activity. */
    fun addPackIntent(pack: StickerPack): Intent =
        Intent(ACTION_ENABLE_STICKER_PACK)
            .setPackage(PACKAGE)
            .putExtra(EXTRA_PACK_ID, pack.identifier)
            .putExtra(EXTRA_PACK_AUTHORITY, AUTHORITY)
            .putExtra(EXTRA_PACK_NAME, pack.name)

    fun whitelistQuery(identifier: String): Uri =
        Uri
            .Builder()
            .scheme("content")
            .authority(WHITELIST_AUTHORITY)
            .appendPath(WHITELIST_PATH)
            .appendQueryParameter("authority", AUTHORITY)
            .appendQueryParameter("identifier", identifier)
            .build()

    fun packMetadataUri(identifier: String): Uri =
        Uri
            .Builder()
            .scheme("content")
            .authority(AUTHORITY)
            .appendPath(METADATA)
            .appendPath(identifier)
            .build()
}
