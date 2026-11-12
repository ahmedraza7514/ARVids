package com.example

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.components.ClipboardBanner
import com.example.ui.components.FloatingDownloadOverlay
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavTab

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle navigation URL from ShareOverlayActivity or external intents
        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        checkClipboardForSocialLink()
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val extraNavUrl = intent?.getStringExtra(ShareOverlayActivity.EXTRA_NAVIGATE_URL)
        if (!extraNavUrl.isNullOrBlank()) {
            viewModel.triggerUrlDownload(extraNavUrl)
            return
        }

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val extractedUrl = extractUrl(sharedText)
                if (extractedUrl != null) {
                    viewModel.triggerUrlDownload(extractedUrl)
                }
            }
        }
    }

    private fun checkClipboardForSocialLink() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard?.hasPrimaryClip() == true) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0)?.text?.toString() ?: ""
                    val url = extractUrl(text)
                    if (url != null) {
                        viewModel.onClipboardUrlDetected(url)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractUrl(text: String): String? {
        val words = text.split("\\s+".toRegex())
        for (word in words) {
            if (word.startsWith("http://") || word.startsWith("https://")) {
                return word
            }
        }
        return null
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var reloadTrigger by remember { mutableLongStateOf(0L) }
    var activeFloatingOverlayUrl by remember { mutableStateOf<String?>(null) }

    // Request notification permission on Android 13+ for DownloadManager completion alerts
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Display transient toast messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            topBar = {
                AppTopBar(
                    adShieldActive = uiState.adShieldActive,
                    onToggleShield = { viewModel.toggleAdShield() },
                    onReload = { reloadTrigger = System.currentTimeMillis() },
                    onPasteFromClipboard = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clipText.isNullOrBlank()) {
                            activeFloatingOverlayUrl = clipText
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            bottomBar = {
                AppBottomNav(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBackground)
            ) {
                // Floating Clipboard Detected Link Banner
                ClipboardBanner(
                    detectedUrl = uiState.clipboardDetectedUrl,
                    onDownloadClicked = { url ->
                        activeFloatingOverlayUrl = url
                        viewModel.dismissClipboardBanner()
                    },
                    onDismiss = {
                        viewModel.dismissClipboardBanner()
                    }
                )

                // Content based on selected tab
                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.selectedTab) {
                        NavTab.DOWNLOADER -> {
                            DownloaderScreen(
                                adShieldActive = uiState.adShieldActive,
                                isPageLoading = uiState.isPageLoading,
                                pageProgress = uiState.pageProgress,
                                pageError = uiState.pageError,
                                urlToInject = uiState.urlToInject,
                                pendingInputUrl = uiState.pendingInputUrl,
                                onPendingInputChanged = { viewModel.setPendingInputUrl(it) },
                                onUrlSubmitted = {
                                    activeFloatingOverlayUrl = it
                                },
                                onUrlInjectedConsumed = { viewModel.onUrlInjectedConsumed() },
                                onPageLoadingChanged = { viewModel.onPageLoadingChanged(it) },
                                onPageProgressChanged = { viewModel.onPageProgressChanged(it) },
                                onPageError = { viewModel.onPageError(it) },
                                onDownloadTriggered = { url, fileName, mime ->
                                    viewModel.enqueueDownload(url, fileName, mime)
                                },
                                reloadTrigger = reloadTrigger
                            )
                        }

                        NavTab.LIBRARY -> {
                            DownloadsScreen(
                                downloads = uiState.downloads,
                                onDeleteDownload = { viewModel.deleteDownload(it) },
                                onClearAll = { viewModel.clearAllDownloads() },
                                onNavigateToDownloader = { viewModel.selectTab(NavTab.DOWNLOADER) }
                            )
                        }

                        NavTab.GUIDE -> {
                            GuideScreen(
                                onTrySampleLink = { sampleUrl ->
                                    activeFloatingOverlayUrl = sampleUrl
                                }
                            )
                        }

                        NavTab.SETTINGS -> {
                            SettingsScreen(
                                adShieldActive = uiState.adShieldActive,
                                onToggleAdShield = { viewModel.toggleAdShield() },
                                autoClipboardActive = uiState.autoClipboardActive,
                                onToggleAutoClipboard = { viewModel.toggleAutoClipboard() }
                            )
                        }
                    }
                }
            }
        }

        // In-app Floating Download Overlay when triggered from clipboard or paste
        if (activeFloatingOverlayUrl != null) {
            FloatingDownloadOverlay(
                sharedUrl = activeFloatingOverlayUrl!!,
                onDismiss = { activeFloatingOverlayUrl = null },
                onOpenFullApp = { url ->
                    activeFloatingOverlayUrl = null
                    viewModel.triggerUrlDownload(url)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
