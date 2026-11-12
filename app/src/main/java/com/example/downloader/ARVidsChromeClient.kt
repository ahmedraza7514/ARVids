package com.example.downloader

import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebView

class ARVidsChromeClient(
    private val onProgressChanged: (Int) -> Unit
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged(newProgress)
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        // Suppress popups / new window ad redirects
        return false
    }
}
