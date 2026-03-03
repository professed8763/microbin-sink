package com.microbinsink.app

import android.content.Intent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ShareReceiverActivityTest {

    @Test
    fun `activity finishes when intent has no action`() {
        val intent = Intent()
        val activity = Robolectric.buildActivity(ShareReceiverActivity::class.java, intent)
            .create()
            .get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun `activity handles SEND intent with text`() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Hello from test")
        }

        val activity = Robolectric.buildActivity(ShareReceiverActivity::class.java, intent)
            .create()
            .get()

        assertNotNull(activity)
    }

    @Test
    fun `activity finishes with no files on SEND_MULTIPLE`() {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
        }

        val activity = Robolectric.buildActivity(ShareReceiverActivity::class.java, intent)
            .create()
            .get()

        assertTrue(activity.isFinishing)
    }
}
