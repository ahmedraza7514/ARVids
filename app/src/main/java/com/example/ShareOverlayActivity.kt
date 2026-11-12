package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.components.FloatingDownloadOverlay
import com.example.ui.theme.MyApplicationTheme

class ShareOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedUrl = extractUrlFromIntent(intent) ?: ""

        if (sharedUrl.isBlank()) {
            // If no valid link was passed, redirect to main activity
            startMainActivity("")
            finish()
            return
        }

        setContent {
            MyApplicationTheme {
                FloatingDownloadOverlay(
                    sharedUrl = sharedUrl,
                    onDismiss = {
                        finish()
                    },
                    onOpenFullApp = { url ->
                        startMainActivity(url)
                        finish()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun startMainActivity(url: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_URL, url)
        }
        startActivity(intent)
    }

    private fun extractUrlFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val words = text.split("\\s+".toRegex())
        for (word in words) {
            if (word.startsWith("http://") || word.startsWith("https://")) {
                return word
            }
        }
        return null
    }

    companion object {
        const val EXTRA_NAVIGATE_URL = "extra_navigate_url"
    }
}
