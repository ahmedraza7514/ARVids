package com.example.downloader

import android.graphics.Bitmap
import android.util.Base64
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

class ARVidsWebClient(
    private val adShieldEnabled: Boolean = true,
    private val onPageLoadingChanged: (Boolean) -> Unit,
    private val onPageError: (String?) -> Unit,
    private val onDownloadTriggered: (url: String, fileName: String?, mimeType: String?) -> Unit
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        if (adShieldEnabled && AdBlockManager.isAdUrl(url)) {
            // Return empty response to drop the ad request completely
            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        val lower = url.lowercase()

        // Check if direct video/audio download link
        if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mp3")) {
            onDownloadTriggered(url, null, "video/mp4")
            return true
        }

        // Drop external ad redirects or intent schemes
        if (lower.startsWith("intent:") || lower.startsWith("market:") || lower.startsWith("itms:")) {
            return true
        }

        // Allow navigation within the downloader
        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageLoadingChanged(true)
        onPageError(null)
        injectCustomStyleAndScript(view)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageLoadingChanged(false)
        injectCustomStyleAndScript(view)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            onPageLoadingChanged(false)
            onPageError(error?.description?.toString() ?: "Network error. Please check connection.")
        }
    }

    private fun injectCustomStyleAndScript(view: WebView?) {
        view ?: return
        try {
            val encodedCss = Base64.encodeToString(
                AdBlockManager.INJECTED_CSS.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            val cssScript = """
                (function() {
                    var style = document.getElementById('ar-vids-custom-style');
                    if (!style) {
                        style = document.createElement('style');
                        style.id = 'ar-vids-custom-style';
                        style.type = 'text/css';
                        style.innerHTML = window.atob('$encodedCss');
                        (document.head || document.documentElement).appendChild(style);
                    }
                })();
            """.trimIndent()

            view.evaluateJavascript(cssScript, null)
            view.evaluateJavascript(AdBlockManager.INJECTED_JS, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
