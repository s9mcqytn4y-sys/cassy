package id.azureenterprise.cassy.desktop

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

interface StoreProfileLogoStore {
    fun chooseAndImport(storeId: String, existingPath: String? = null): Result<String?>
    fun deleteManaged(path: String?)
}

class DesktopStoreProfileLogoStore : StoreProfileLogoStore {
    override fun chooseAndImport(storeId: String, existingPath: String?): Result<String?> {
        return runCatching {
            val chooser = JFileChooser().apply {
                dialogTitle = "Pilih logo usaha"
                fileSelectionMode = JFileChooser.FILES_ONLY
                isAcceptAllFileFilterUsed = false
                fileFilter = FileNameExtensionFilter("Gambar PNG atau JPG", "png", "jpg", "jpeg")
            }

            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                return@runCatching null
            }

            val source = chooser.selectedFile ?: return@runCatching null
            validateImageFile(source)

            val extension = source.extension.lowercase().ifBlank { "png" }
            val targetDir = File(resolveDesktopDataRoot(), "assets/store-profile/$storeId").apply { mkdirs() }
            val target = File(targetDir, "store-logo.$extension")
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)

            if (existingPath != null && existingPath != target.absolutePath) {
                deleteManaged(existingPath)
            }

            target.absolutePath
        }
    }

    override fun deleteManaged(path: String?) {
        val candidate = path?.let(::File) ?: return
        val managedRoot = File(resolveDesktopDataRoot(), "assets/store-profile").absoluteFile
        val absoluteCandidate = candidate.absoluteFile
        if (absoluteCandidate.exists() && absoluteCandidate.toPath().startsWith(managedRoot.toPath())) {
            absoluteCandidate.delete()
        }
    }

    private fun validateImageFile(file: File) {
        require(file.exists() && file.isFile) { "File logo tidak ditemukan" }
        require(file.length() in 1..MAX_FILE_SIZE_BYTES) { "Ukuran logo maksimal 5 MB" }
        val extension = file.extension.lowercase()
        require(extension in allowedExtensions) { "Format logo harus PNG atau JPG" }
        val image = ImageIO.read(file) ?: error("File logo tidak bisa dibaca")
        require(image.width in 1..MAX_DIMENSION && image.height in 1..MAX_DIMENSION) {
            "Ukuran dimensi logo terlalu besar"
        }
    }

    private companion object {
        val allowedExtensions = setOf("png", "jpg", "jpeg")
        const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L
        const val MAX_DIMENSION = 4096
    }
}
