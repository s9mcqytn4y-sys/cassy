package id.azureenterprise.cassy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun BusinessDayScreen(
    onNavigateToCatalog: () -> Unit
) {
    val service: BusinessDayService = koinInject()
    val scope = rememberCoroutineScope()
    var isOpen by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isOpen = service.isOpen()
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isOpen == null -> CircularProgressIndicator()
                isOpen == true -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Business Day is Open", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToCatalog) {
                            Text("Go to Catalog")
                        }
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Business Day is Closed", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Button(onClick = {
                                scope.launch {
                                    isLoading = true
                                    service.openNewDay().onSuccess {
                                        isOpen = true
                                    }.onFailure {
                                        error = it.message
                                    }
                                    isLoading = false
                                }
                            }) {
                                Text("Open Business Day")
                            }
                        }
                        error?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
