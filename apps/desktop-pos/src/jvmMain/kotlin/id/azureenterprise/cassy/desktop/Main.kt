package id.azureenterprise.cassy.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import id.azureenterprise.cassy.di.initKoin
import org.koin.compose.KoinContext

fun main() = application {
    // Initialize Koin for Desktop
    initKoin()

    Window(onCloseRequest = ::exitApplication, title = "Cassy Desktop POS") {
        KoinContext {
            // Main Desktop UI Entry
            id.azureenterprise.cassy.ui.CatalogScreen()
        }
    }
}
