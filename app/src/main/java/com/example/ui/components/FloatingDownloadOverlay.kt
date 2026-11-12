package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.data.model.DownloadStatus
import com.example.data.repository.DownloadProgressInfo
import com.example.data.repository.DownloadRepository
import com.example.downloader.ARVidsChromeClient
import com.example.downloader.ARVidsWebClient
import com.example.downloader.AdBlockManager
import com.example.downloader.WebAppInterface
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

enum class OverlayState {
    EXTRACTING,
    READY,
    DOWNLOADING,
    COMPLETED,
    ERROR
}

data class DiscoveredMedia(
    val url: String,
    val title: String,
    val mimeType: String = "video/mp4"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FloatingDownloadOverlay(
    sharedUrl: String,
    onDismiss: () -> Unit,
    onOpenFullApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { DownloadRepository(context) }

    var overlayState by remember { mutableStateOf(OverlayState.EXTRACTING) }
    var discoveredMediaList by remember { mutableStateOf<List<DiscoveredMedia>>(emptyList()) }
    var selectedMedia by remember { mutableStateOf<DiscoveredMedia?>(null) }
    var downloadProgress by remember { mutableStateOf<DownloadProgressInfo?>(null) }
    var downloadedFileName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Detect social platform for friendly header
    val platformName = remember(sharedUrl) {
        val lower = sharedUrl.lowercase()
        when {
            lower.contains("instagram.com") -> "Instagram Reel / Video"
            lower.contains("tiktok.com") -> "TikTok Video (No Watermark)"
            lower.contains("facebook.com") || lower.contains("fb.watch") -> "Facebook Video"
            lower.contains("twitter.com") || lower.contains("x.com") -> "X / Twitter Clip"
            lower.contains("pinterest.com") -> "Pinterest Pin"
            lower.contains("reddit.com") -> "Reddit Media"
            else -> "Social Video"
        }
    }

    // Function to trigger download of selected media
    fun startDownload(media: DiscoveredMedia) {
        scope.launch {
            overlayState = OverlayState.DOWNLOADING
            downloadedFileName = media.title.ifBlank { "AR_Vids_Instagram_${System.currentTimeMillis()}.mp4" }
            if (!downloadedFileName.endsWith(".mp4")) {
                downloadedFileName += ".mp4"
            }

            val (_, dmId) = repository.enqueueDownload(
                url = media.url,
                suggestedFileName = downloadedFileName,
                mimeType = media.mimeType
            )

            // Monitor download progress
            if (dmId > 0) {
                repository.monitorProgress(dmId, downloadedFileName).collect { progress ->
                    downloadProgress = progress
                    if (progress.status == DownloadStatus.COMPLETED) {
                        overlayState = OverlayState.COMPLETED
                    } else if (progress.status == DownloadStatus.FAILED) {
                        errorMessage = "Download could not complete"
                        overlayState = OverlayState.ERROR
                    }
                }
            } else {
                overlayState = OverlayState.COMPLETED
            }
        }
    }

    // Timeout fallback: If after 15s extraction didn't resolve, give user option to open full app or retry
    LaunchedEffect(Unit) {
        delay(16000)
        if (overlayState == OverlayState.EXTRACTING) {
            errorMessage = "Extraction is taking longer than usual."
            overlayState = OverlayState.ERROR
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { /* prevent click through */ },
        contentAlignment = Alignment.Center
    ) {
        // Floating Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, IndigoPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("floating_download_card"),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(IndigoPrimary, CyanSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "AR-Vids",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AR-Vids",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IndigoPrimary.copy(alpha = 0.3f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "FAST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanSecondary
                                    )
                                }
                            }
                            Text(
                                text = platformName,
                                fontSize = 11.sp,
                                color = CyanSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("button_close_overlay")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Truncated URL chip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkOutline, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = sharedUrl,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // State Content
                when (overlayState) {
                    OverlayState.EXTRACTING -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = CyanSecondary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Extracting video...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fetching original high quality video stream",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    OverlayState.READY -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = "✓ Video Ready to Download",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldTertiary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Show format option buttons
                            val mediaToDownload = selectedMedia ?: discoveredMediaList.firstOrNull()

                            Button(
                                onClick = {
                                    if (mediaToDownload != null) {
                                        startDownload(mediaToDownload)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("button_overlay_download_now"),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Download Video (HD)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OverlayState.DOWNLOADING -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading Video...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${downloadProgress?.progressPercent ?: 0}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (downloadProgress?.progressPercent ?: 0) / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyanSecondary,
                                trackColor = DarkSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val downloadedMB = (downloadProgress?.bytesDownloaded ?: 0L) / (1024.0 * 1024.0)
                            val totalMB = (downloadProgress?.totalBytes ?: 0L) / (1024.0 * 1024.0)
                            val sizeText = if (totalMB > 0) {
                                String.format(Locale.US, "%.1f MB / %.1f MB", downloadedMB, totalMB)
                            } else {
                                String.format(Locale.US, "%.1f MB downloaded", downloadedMB)
                            }

                            Text(
                                text = sizeText,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    OverlayState.COMPLETED -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldTertiary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = EmeraldTertiary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Video Saved to Gallery!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = "Saved in Downloads > AR-Vids",
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        playSavedVideo(context, downloadedFileName)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("button_overlay_play"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        shareSavedVideo(context, downloadedFileName)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("button_overlay_share"),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    OverlayState.ERROR -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "Could not extract video link",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        overlayState = OverlayState.EXTRACTING
                                        webViewInstance?.reload()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { onOpenFullApp(sharedUrl) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Open App", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Footer Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Open full AR-Vids",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanSecondary,
                        modifier = Modifier
                            .clickable { onOpenFullApp(sharedUrl) }
                            .padding(4.dp)
                    )

                    Text(
                        text = "Dismiss",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Invisible background extractor WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(1, 1)
                    visibility = android.view.View.INVISIBLE

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                    }

                    CookieManager.getInstance().setAcceptCookie(true)

                    addJavascriptInterface(
                        WebAppInterface(
                            onDownloadRequest = { url, fileName, mime ->
                                val media = DiscoveredMedia(url, fileName ?: "video.mp4", mime ?: "video/mp4")
                                selectedMedia = media
                                startDownload(media)
                            },
                            onMediaDiscovered = { url, quality, mime ->
                                val media = DiscoveredMedia(url, quality ?: "video.mp4", mime ?: "video/mp4")
                                if (discoveredMediaList.none { it.url == url }) {
                                    discoveredMediaList = discoveredMediaList + media
                                    selectedMedia = media
                                    if (overlayState == OverlayState.EXTRACTING) {
                                        // Auto-start or mark ready!
                                        overlayState = OverlayState.READY
                                    }
                                }
                            }
                        ),
                        "AndroidApp"
                    )

                    webViewClient = ARVidsWebClient(
                        adShieldEnabled = true,
                        onPageLoadingChanged = { loading ->
                            if (!loading && overlayState == OverlayState.EXTRACTING) {
                                // Once page finishes, paste the shared URL into the downloader form
                                val pasteScript = AdBlockManager.buildPasteScript(sharedUrl)
                                evaluateJavascript(pasteScript, null)
                            }
                        },
                        onPageError = { err ->
                            if (overlayState == OverlayState.EXTRACTING) {
                                errorMessage = err ?: "Network connection issue"
                                overlayState = OverlayState.ERROR
                            }
                        },
                        onDownloadTriggered = { url, fileName, mime ->
                            val media = DiscoveredMedia(url, fileName ?: "video.mp4", mime ?: "video/mp4")
                            selectedMedia = media
                            startDownload(media)
                        }
                    )

                    webChromeClient = ARVidsChromeClient(onProgressChanged = {})

                    loadUrl("https://getindevice.com/")
                    webViewInstance = this
                }
            },
            modifier = Modifier.size(1.dp)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}

private fun playSavedVideo(context: Context, fileName: String) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "AR-Vids/$fileName")
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Play Video with"))
        } else {
            Toast.makeText(context, "Saved video file located in Downloads/AR-Vids", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open video player", Toast.LENGTH_SHORT).show()
    }
}

private fun shareSavedVideo(context: Context, fileName: String) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "AR-Vids/$fileName")
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, "Saved with AR-Vids")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share video", Toast.LENGTH_SHORT).show()
    }
}
