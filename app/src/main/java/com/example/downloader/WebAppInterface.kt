package com.example.downloader

import android.webkit.JavascriptInterface

class WebAppInterface(
    private val onDownloadRequest: (url: String, fileName: String?, mimeType: String?) -> Unit,
    private val onMediaDiscovered: ((url: String, qualityOrTitle: String?, mimeType: String?) -> Unit)? = null
) {
    @JavascriptInterface
    fun onDownloadRequested(url: String, fileName: String?, mimeType: String?) {
        if (url.isNotBlank()) {
            onDownloadRequest(url, fileName, mimeType)
        }
    }

    @JavascriptInterface
    fun onMediaDiscovered(url: String, qualityOrTitle: String?, mimeType: String?) {
        if (url.isNotBlank()) {
            onMediaDiscovered?.invoke(url, qualityOrTitle, mimeType)
        }
    }
}
