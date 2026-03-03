package com.microbinsink.app.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.microbinsink.app.upload.UploadConfig

object PreferencesHelper {

    private const val DEFAULT_SERVER_URL = "https://pub.microbin.eu"
    private const val DEFAULT_PRIVACY = "public"
    private const val DEFAULT_EXPIRATION = "1hour"
    private const val DEFAULT_BURN_AFTER = "0"

    fun getUploadConfig(context: Context): UploadConfig {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return UploadConfig(
            serverUrl = normalizeUrl(prefs.getString("server_url", DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL),
            privacy = prefs.getString("privacy", DEFAULT_PRIVACY) ?: DEFAULT_PRIVACY,
            expiration = prefs.getString("expiration", DEFAULT_EXPIRATION) ?: DEFAULT_EXPIRATION,
            burnAfter = prefs.getString("burn_after", DEFAULT_BURN_AFTER) ?: DEFAULT_BURN_AFTER,
            uploaderPassword = prefs.getString("uploader_password", "") ?: "",
        )
    }

    fun normalizeUrl(url: String): String {
        var normalized = url.trim()
        if (normalized.isBlank()) return DEFAULT_SERVER_URL
        if (!normalized.contains("://")) {
            normalized = "https://$normalized"
        }
        return normalized.trimEnd('/')
    }

    fun isValidUrl(url: String): Boolean {
        val normalized = normalizeUrl(url)
        return try {
            val parsed = java.net.URL(normalized)
            parsed.host.isNotBlank() && (parsed.protocol == "http" || parsed.protocol == "https")
        } catch (e: Exception) {
            false
        }
    }
}
