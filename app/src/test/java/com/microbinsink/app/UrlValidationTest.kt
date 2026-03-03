package com.microbinsink.app

import com.microbinsink.app.util.PreferencesHelper
import org.junit.Assert.*
import org.junit.Test

class UrlValidationTest {

    @Test
    fun `normalizeUrl adds https scheme when missing`() {
        assertEquals("https://example.com", PreferencesHelper.normalizeUrl("example.com"))
    }

    @Test
    fun `normalizeUrl preserves http scheme`() {
        assertEquals("http://example.com", PreferencesHelper.normalizeUrl("http://example.com"))
    }

    @Test
    fun `normalizeUrl preserves https scheme`() {
        assertEquals("https://example.com", PreferencesHelper.normalizeUrl("https://example.com"))
    }

    @Test
    fun `normalizeUrl removes trailing slash`() {
        assertEquals("https://example.com", PreferencesHelper.normalizeUrl("https://example.com/"))
    }

    @Test
    fun `normalizeUrl trims whitespace`() {
        assertEquals("https://example.com", PreferencesHelper.normalizeUrl("  example.com  "))
    }

    @Test
    fun `normalizeUrl returns default for blank input`() {
        assertEquals("https://pub.microbin.eu", PreferencesHelper.normalizeUrl(""))
        assertEquals("https://pub.microbin.eu", PreferencesHelper.normalizeUrl("   "))
    }

    @Test
    fun `isValidUrl accepts valid URLs`() {
        assertTrue(PreferencesHelper.isValidUrl("https://example.com"))
        assertTrue(PreferencesHelper.isValidUrl("http://192.168.1.1:8080"))
        assertTrue(PreferencesHelper.isValidUrl("https://my.microbin.server.com/path"))
        assertTrue(PreferencesHelper.isValidUrl("example.com"))
    }

    @Test
    fun `isValidUrl rejects invalid URLs`() {
        assertFalse(PreferencesHelper.isValidUrl("ftp://example.com"))
    }
}
