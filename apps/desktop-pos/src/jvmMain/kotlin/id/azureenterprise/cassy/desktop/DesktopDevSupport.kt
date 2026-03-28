package id.azureenterprise.cassy.desktop

import java.io.File

private val devResetArtifacts = listOf(
    "kernel.db",
    "sales.db",
    "inventory.db",
    "masterdata.db"
)

fun resolveDesktopDataRoot(): File {
    val explicit = System.getProperty("cassy.data.dir")
        ?: System.getenv("CASSY_DATA_DIR")
    return explicit?.let(::File) ?: File(System.getProperty("user.home"), ".cassy")
}

fun isDevResetEnabled(): Boolean {
    val explicit = System.getProperty("cassy.dev.reset.enabled")
        ?: System.getenv("CASSY_DEV_RESET_ENABLED")
    return explicit.equals("true", ignoreCase = true)
}

fun resetDesktopDataForDevelopment(): List<File> {
    check(isDevResetEnabled()) {
        "Reset database dev diblokir. Aktifkan dengan -Dcassy.dev.reset.enabled=true atau CASSY_DEV_RESET_ENABLED=true."
    }
    val dataRoot = resolveDesktopDataRoot()
    if (!dataRoot.exists()) return emptyList()

    return buildList {
        devResetArtifacts.forEach { name ->
            val primary = File(dataRoot, name)
            listOf(primary, File("${primary.absolutePath}-wal"), File("${primary.absolutePath}-shm"))
                .filter(File::exists)
                .forEach { file ->
                    if (file.delete()) add(file)
                }
        }
    }
}

fun devResetCommandHint(): String =
    "powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopSandbox.ps1 -ResetDemo"
