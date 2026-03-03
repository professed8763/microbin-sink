package com.microbinsink.app.upload

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume

class MicrobinUploader(
    private val client: OkHttpClient = createDefaultClient()
) {

    companion object {
        fun createDefaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
        }
    }

    suspend fun uploadFile(
        file: File,
        fileName: String,
        mimeType: String,
        config: UploadConfig
    ): UploadResult = withContext(Dispatchers.IO) {
        try {
            val requestBody = buildMultipartBody(
                file = file,
                fileName = fileName,
                mimeType = mimeType,
                textContent = "",
                config = config
            )

            val request = Request.Builder()
                .url("${config.serverUrl}/upload")
                .post(requestBody)
                .build()

            executeAndParseResponse(request, config.serverUrl)
        } catch (e: Exception) {
            UploadResult.Error("Upload failed: ${e.message}", e)
        }
    }

    suspend fun uploadText(
        text: String,
        config: UploadConfig
    ): UploadResult = withContext(Dispatchers.IO) {
        try {
            val requestBody = buildMultipartBody(
                file = null,
                fileName = null,
                mimeType = null,
                textContent = text,
                config = config
            )

            val request = Request.Builder()
                .url("${config.serverUrl}/upload")
                .post(requestBody)
                .build()

            executeAndParseResponse(request, config.serverUrl)
        } catch (e: Exception) {
            UploadResult.Error("Upload failed: ${e.message}", e)
        }
    }

    internal fun buildMultipartBody(
        file: File?,
        fileName: String?,
        mimeType: String?,
        textContent: String,
        config: UploadConfig
    ): MultipartBody {
        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("content", textContent)
            .addFormDataPart("privacy", config.privacy)
            .addFormDataPart("expiration", config.expiration)
            .addFormDataPart("burn_after", config.burnAfter)
            .addFormDataPart("syntax_highlight", "")

        if (config.uploaderPassword.isNotBlank()) {
            builder.addFormDataPart("uploader_password", config.uploaderPassword)
        }

        if (file != null && fileName != null && mimeType != null) {
            val mediaType = mimeType.toMediaType()
            builder.addFormDataPart("file", fileName, file.asRequestBody(mediaType))
        }

        return builder.build()
    }

    private suspend fun executeAndParseResponse(
        request: Request,
        serverUrl: String
    ): UploadResult {
        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(
                        UploadResult.Error("Network error: ${e.message}", e)
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        when (response.code) {
                            302, 303 -> {
                                val location = response.header("Location")
                                if (location != null) {
                                    val pasteUrl = if (location.startsWith("http")) {
                                        location
                                    } else {
                                        "$serverUrl$location"
                                    }
                                    continuation.resume(UploadResult.Success(pasteUrl))
                                } else {
                                    continuation.resume(
                                        UploadResult.Error("Server returned redirect but no Location header")
                                    )
                                }
                            }
                            200 -> {
                                continuation.resume(
                                    UploadResult.Success("$serverUrl (upload may have succeeded)")
                                )
                            }
                            else -> {
                                val body = response.body?.string() ?: ""
                                continuation.resume(
                                    UploadResult.Error("Server error ${response.code}: $body")
                                )
                            }
                        }
                    }
                }
            })
        }
    }
}
