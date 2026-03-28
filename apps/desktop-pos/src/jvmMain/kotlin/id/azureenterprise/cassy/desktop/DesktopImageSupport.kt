package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image
import java.io.File

const val CASSY_BRAND_ICON_RESOURCE = "icon/cassy-app-icon.svg"

@Composable
fun ManagedImagePreview(
    imagePath: String?,
    fallbackLabel: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap = remember(imagePath) { loadManagedImageBitmap(imagePath) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            FallbackMonogram(
                label = fallbackLabel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BoxScope.FallbackMonogram(
    label: String,
    modifier: Modifier
) {
    Text(
        text = label.trim().take(1).uppercase().ifBlank { "C" },
        modifier = modifier.padding(8.dp).align(Alignment.Center),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun ManagedAvatarPreview(
    imagePath: String?,
    fallbackLabel: String,
    contentDescription: String,
    size: Dp = 72.dp
) {
    ManagedImagePreview(
        imagePath = imagePath,
        fallbackLabel = fallbackLabel,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun ManagedRoundAvatarPreview(
    imagePath: String?,
    fallbackLabel: String,
    contentDescription: String,
    size: Dp = 40.dp
) {
    ManagedImagePreview(
        imagePath = imagePath,
        fallbackLabel = fallbackLabel,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        shape = CircleShape
    )
}

private fun loadManagedImageBitmap(imagePath: String?): ImageBitmap? {
    val path = imagePath?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val file = File(path)
    if (!file.exists() || !file.isFile) return null
    return runCatching {
        Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap()
    }.getOrNull()
}
