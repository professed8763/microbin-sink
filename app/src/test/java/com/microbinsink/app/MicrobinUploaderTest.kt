package com.microbinsink.app

import com.microbinsink.app.upload.MicrobinUploader
import com.microbinsink.app.upload.UploadConfig
import com.microbinsink.app.upload.UploadResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class MicrobinUploaderTest {

    private lateinit var server: MockWebServer
    private lateinit var uploader: MicrobinUploader

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        uploader = MicrobinUploader(MicrobinUploader.createDefaultClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun baseConfig(): UploadConfig {
        return UploadConfig(
            serverUrl = server.url("").toString().trimEnd('/'),
            privacy = "public",
            expiration = "1hour",
            burnAfter = "0",
            uploaderPassword = ""
        )
    }

    @Test
    fun `uploadText returns Success on 302 redirect`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", "/upload/fish-pony-crow")
        )

        val result = uploader.uploadText("Hello MicroBin", baseConfig())

        assertTrue(result is UploadResult.Success)
        val success = result as UploadResult.Success
        assertTrue(success.pasteUrl.endsWith("/upload/fish-pony-crow"))
    }

    @Test
    fun `uploadText sends correct form fields`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", "/upload/test-slug")
        )

        val config = baseConfig().copy(
            privacy = "readonly",
            expiration = "1week",
            burnAfter = "10"
        )
        uploader.uploadText("Test content", config)

        val request = server.takeRequest()
        val body = request.body.readUtf8()

        assertEquals("POST", request.method)
        assertTrue(request.path?.endsWith("/upload") == true)
        assertTrue(body.contains("Test content"))
        assertTrue(body.contains("readonly"))
        assertTrue(body.contains("1week"))
    }

    @Test
    fun `uploadFile returns Success on 302 redirect`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", "/upload/file-slug")
        )

        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("file content")

        try {
            val result = uploader.uploadFile(
                file = tempFile,
                fileName = "test.txt",
                mimeType = "text/plain",
                config = baseConfig()
            )

            assertTrue(result is UploadResult.Success)
            val success = result as UploadResult.Success
            assertTrue(success.pasteUrl.endsWith("/upload/file-slug"))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `uploadFile includes file in multipart body`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", "/upload/slug")
        )

        val tempFile = File.createTempFile("test", ".pdf")
        tempFile.writeBytes(byteArrayOf(0x25, 0x50, 0x44, 0x46))

        try {
            uploader.uploadFile(
                file = tempFile,
                fileName = "document.pdf",
                mimeType = "application/pdf",
                config = baseConfig()
            )

            val request = server.takeRequest()
            val body = request.body.readUtf8()
            assertTrue(body.contains("document.pdf"))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `upload returns Error on server error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        val result = uploader.uploadText("test", baseConfig())

        assertTrue(result is UploadResult.Error)
        val error = result as UploadResult.Error
        assertTrue(error.message.contains("500"))
    }

    @Test
    fun `upload returns Error on 302 without Location header`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(302))

        val result = uploader.uploadText("test", baseConfig())

        assertTrue(result is UploadResult.Error)
        val error = result as UploadResult.Error
        assertTrue(error.message.contains("Location"))
    }

    @Test
    fun `upload handles absolute Location header`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", "https://other.server.com/upload/slug")
        )

        val result = uploader.uploadText("test", baseConfig())

        assertTrue(result is UploadResult.Success)
        assertEquals("https://other.server.com/upload/slug", (result as UploadResult.Success).pasteUrl)
    }

    @Test
    fun `buildMultipartBody includes uploader_password when set`() {
        val config = baseConfig().copy(uploaderPassword = "secret123")
        val body = uploader.buildMultipartBody(
            file = null, fileName = null, mimeType = null,
            textContent = "test", config = config
        )

        assertEquals(6, body.parts.size)
    }

    @Test
    fun `buildMultipartBody excludes uploader_password when blank`() {
        val config = baseConfig().copy(uploaderPassword = "")
        val body = uploader.buildMultipartBody(
            file = null, fileName = null, mimeType = null,
            textContent = "test", config = config
        )

        assertEquals(5, body.parts.size)
    }
}
