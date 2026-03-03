package com.microbinsink.app

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.microbinsink.app.util.PreferencesHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PreferencesHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `getUploadConfig returns defaults when no preferences set`() {
        val config = PreferencesHelper.getUploadConfig(context)

        assertEquals("https://pub.microbin.eu", config.serverUrl)
        assertEquals("public", config.privacy)
        assertEquals("1hour", config.expiration)
        assertEquals("0", config.burnAfter)
        assertEquals("", config.uploaderPassword)
    }

    @Test
    fun `getUploadConfig reads stored preferences`() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putString("server_url", "https://my.server.com")
            .putString("privacy", "secret")
            .putString("expiration", "1week")
            .putString("burn_after", "100")
            .putString("uploader_password", "mypass")
            .apply()

        val config = PreferencesHelper.getUploadConfig(context)

        assertEquals("https://my.server.com", config.serverUrl)
        assertEquals("secret", config.privacy)
        assertEquals("1week", config.expiration)
        assertEquals("100", config.burnAfter)
        assertEquals("mypass", config.uploaderPassword)
    }

    @Test
    fun `getUploadConfig normalizes server URL`() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putString("server_url", "my.server.com/")
            .apply()

        val config = PreferencesHelper.getUploadConfig(context)

        assertEquals("https://my.server.com", config.serverUrl)
    }
}
