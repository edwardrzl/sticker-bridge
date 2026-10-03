package com.edrl.stickerbridge.pack.storage

import com.edrl.stickerbridge.core.pack.PackIndexCodec
import com.edrl.stickerbridge.core.pack.PackRepository
import com.edrl.stickerbridge.core.pack.StagedFile
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Stores packs under `<root>/packs/<identifier>/` with one JSON index (ADR-004).
 *
 * A commit first moves the staged files into place and then replaces the index with an atomic
 * rename. The index is the source of truth: files it does not reference are not part of any pack.
 */
class FilePackRepository(
    root: File,
) : PackRepository {
    private val packsDir = File(root, "packs")
    private val index = File(packsDir, "packs.json")
    private val tempIndex = File(packsDir, "packs.json.tmp")

    override suspend fun load(): List<StickerPack> =
        withContext(Dispatchers.IO) {
            if (!index.exists()) return@withContext emptyList()
            try {
                PackIndexCodec.decode(index.readText())
            } catch (e: IOException) {
                throw StorageException("cannot read pack index", e)
            }
        }

    override suspend fun commit(
        packs: List<StickerPack>,
        staged: List<StagedFile>,
    ): Unit =
        withContext(Dispatchers.IO) {
            try {
                staged.forEach(::moveIntoPack)
                packsDir.mkdirs()
                tempIndex.writeText(PackIndexCodec.encode(packs))
                Files.move(
                    tempIndex.toPath(),
                    index.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (e: IOException) {
                throw StorageException("cannot save packs", e)
            }
        }

    /** The file of a pack, or null when the names would escape the packs folder. */
    fun fileOf(
        packIdentifier: String,
        fileName: String,
    ): File? {
        if (!isSafeName(packIdentifier) || !isSafeName(fileName)) return null
        return File(File(packsDir, packIdentifier), fileName)
    }

    private fun moveIntoPack(file: StagedFile) {
        val target =
            fileOf(file.packIdentifier, file.fileName)
                ?: throw StorageException("unsafe file name ${file.packIdentifier}/${file.fileName}")
        target.parentFile?.mkdirs()
        Files.move(File(file.sourcePath).toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    private fun isSafeName(name: String): Boolean =
        name.isNotBlank() && name != "." && name != ".." && SAFE_NAME.matches(name)

    private companion object {
        val SAFE_NAME = Regex("^[A-Za-z0-9_.-]+$")
    }
}
