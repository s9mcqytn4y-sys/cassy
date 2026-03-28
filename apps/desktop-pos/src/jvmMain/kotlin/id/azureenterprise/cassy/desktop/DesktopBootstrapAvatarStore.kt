package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.kernel.domain.OperatorRole
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

interface BootstrapAvatarStore {
    fun chooseAndImport(role: OperatorRole, existingPath: String? = null): Result<String?>
    fun deleteManaged(path: String?)
}

class DesktopBootstrapAvatarStore : BootstrapAvatarStore {
    override fun chooseAndImport(role: OperatorRole, existingPath: String?): Result<String?> {
        return runCatching {
            val chooser = JFileChooser().apply {
                dialogTitle = when (role) {
                    OperatorRole.CASHIER -> "Pilih foto kasir"
                    OperatorRole.SUPERVISOR -> "Pilih foto supervisor"
                    OperatorRole.OWNER -> "Pilih foto owner"
                }
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
            val targetDir = File(resolveDesktopDataRoot(), "assets/operator-avatars").apply { mkdirs() }
            val target = File(targetDir, "${role.name.lowercase()}-initial.$extension")
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)

            if (existingPath != null && existingPath != target.absolutePath) {
                deleteManaged(existingPath)
            }

            target.absolutePath
        }
    }

    override fun deleteManaged(path: String?) {
        val candidate = path?.let(::File) ?: return
        val managedRoot = File(resolveDesktopDataRoot(), "assets/operator-avatars").absoluteFile
        val absoluteCandidate = candidate.absoluteFile
        if (absoluteCandidate.exists() && absoluteCandidate.parentFile == managedRoot) {
            absoluteCandidate.delete()
        }
    }

    private fun validateImageFile(file: File) {
        require(file.exists() && file.isFile) { "File foto tidak ditemukan" }
        require(file.length() in 1..MAX_FILE_SIZE_BYTES) { "Ukuran foto maksimal 5 MB" }
        val extension = file.extension.lowercase()
        require(extension in allowedExtensions) { "Format foto harus PNG atau JPG" }
        val image = ImageIO.read(file) ?: error("File gambar tidak bisa dibaca")
        require(image.width in 1..MAX_DIMENSION && image.height in 1..MAX_DIMENSION) {
            "Ukuran dimensi foto terlalu besar"
        }
    }

    private companion object {
        val allowedExtensions = setOf("png", "jpg", "jpeg")
        const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L
        const val MAX_DIMENSION = 4096
    }
}
