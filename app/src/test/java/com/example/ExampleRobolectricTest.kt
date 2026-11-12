package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.downloader.AdBlockManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AR-Vids", appName)
  }

  @Test
  fun `verify ad domains are blocked`() {
    assertTrue(AdBlockManager.isAdUrl("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"))
    assertTrue(AdBlockManager.isAdUrl("https://adservice.google.com/adsid/google/ui"))
    assertTrue(AdBlockManager.isAdUrl("https://securepubads.g.doubleclick.net/gampad/ads"))
  }
}
