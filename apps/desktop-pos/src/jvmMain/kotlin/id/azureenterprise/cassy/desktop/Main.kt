package id.azureenterprise.cassy.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import id.azureenterprise.cassy.di.initKoin
import id.azureenterprise.cassy.ui.BusinessDayScreen
import id.azureenterprise.cassy.ui.CatalogScreen
import org.koin.compose.KoinContext

fun main() = application {
    // Initialize Koin for Desktop
    initKoin()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.BusinessDay) }

    Window(onCloseRequest = ::exitApplication, title = "Cassy Desktop POS") {
        KoinContext {
            when (currentScreen) {
                Screen.BusinessDay -> BusinessDayScreen(
                    onNavigateToCatalog = { currentScreen = Screen.Catalog }
                )
                Screen.Catalog -> CatalogScreen()
            }
        }
    }
}

sealed interface Screen {
    object BusinessDay : Screen
    object Catalog : Screen
}
