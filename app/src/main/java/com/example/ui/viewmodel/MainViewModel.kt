package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DownloadItem
import com.example.data.repository.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NavTab {
    DOWNLOADER,
    LIBRARY,
    GUIDE,
    SETTINGS
}

data class MainUiState(
    val selectedTab: NavTab = NavTab.DOWNLOADER,
    val isPageLoading: Boolean = false,
    val pageProgress: Int = 0,
    val pageError: String? = null,
    val adShieldActive: Boolean = true,
    val autoClipboardActive: Boolean = true,
    val clipboardDetectedUrl: String? = null,
    val urlToInject: String? = null,
    val downloads: List<DownloadItem> = emptyList(),
    val pendingInputUrl: String = "",
    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DownloadRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allDownloads.collect { list ->
                _uiState.update { it.copy(downloads = list) }
            }
        }
    }

    fun selectTab(tab: NavTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onPageLoadingChanged(loading: Boolean) {
        _uiState.update { it.copy(isPageLoading = loading) }
    }

    fun onPageProgressChanged(progress: Int) {
        _uiState.update { it.copy(pageProgress = progress) }
    }

    fun onPageError(error: String?) {
        _uiState.update { it.copy(pageError = error) }
    }

    fun setPendingInputUrl(url: String) {
        _uiState.update { it.copy(pendingInputUrl = url) }
    }

    fun triggerUrlDownload(url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank()) {
            _uiState.update {
                it.copy(
                    urlToInject = trimmed,
                    pendingInputUrl = trimmed,
                    selectedTab = NavTab.DOWNLOADER,
                    clipboardDetectedUrl = null
                )
            }
        }
    }

    fun onUrlInjectedConsumed() {
        _uiState.update { it.copy(urlToInject = null) }
    }

    fun onClipboardUrlDetected(url: String) {
        if (!_uiState.value.autoClipboardActive) return
        val current = _uiState.value.clipboardDetectedUrl
        if (current != url && isValidSocialUrl(url)) {
            _uiState.update { it.copy(clipboardDetectedUrl = url) }
        }
    }

    fun dismissClipboardBanner() {
        _uiState.update { it.copy(clipboardDetectedUrl = null) }
    }

    fun toggleAdShield() {
        _uiState.update {
            val newState = !it.adShieldActive
            it.copy(
                adShieldActive = newState,
                toastMessage = if (newState) "Ad-Shield Activated" else "Ad-Shield Paused"
            )
        }
    }

    fun toggleAutoClipboard() {
        _uiState.update {
            val newState = !it.autoClipboardActive
            it.copy(
                autoClipboardActive = newState,
                toastMessage = if (newState) "Auto-clipboard enabled" else "Auto-clipboard disabled"
            )
        }
    }

    fun enqueueDownload(url: String, fileName: String?, mimeType: String?) {
        viewModelScope.launch {
            repository.enqueueDownload(url, fileName, mimeType)
            _uiState.update { it.copy(toastMessage = "Download started in AR-Vids!") }
        }
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch {
            repository.deleteDownload(item)
            _uiState.update { it.copy(toastMessage = "Item removed") }
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.update { it.copy(toastMessage = "All downloads cleared") }
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun isValidSocialUrl(url: String): Boolean {
        val lower = url.lowercase().trim()
        return (lower.startsWith("http://") || lower.startsWith("https://")) &&
                (lower.contains("instagram.com") ||
                        lower.contains("tiktok.com") ||
                        lower.contains("facebook.com") ||
                        lower.contains("fb.watch") ||
                        lower.contains("twitter.com") ||
                        lower.contains("x.com") ||
                        lower.contains("pinterest.com") ||
                        lower.contains("reddit.com") ||
                        lower.contains("threads.net") ||
                        lower.contains("twitch.tv") ||
                        lower.contains("vimeo.com"))
    }
}
