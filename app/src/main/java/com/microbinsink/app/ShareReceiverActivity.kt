package com.microbinsink.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import com.microbinsink.app.upload.MicrobinUploader
import com.microbinsink.app.upload.UploadConfig
import com.microbinsink.app.upload.UploadResult
import com.microbinsink.app.util.PreferencesHelper
import kotlinx.coroutines.*
import java.io.File

class ShareReceiverActivity : Activity() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val uploader = MicrobinUploader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            finish()
            return
        }

        when (intent.action) {
            Intent.ACTION_SEND -> handleSendIntent(intent)
            Intent.ACTION_SEND_MULTIPLE -> handleSendMultipleIntent(intent)
            else -> {
                Toast.makeText(this, "Unsupported action", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleSendIntent(intent: Intent) {
        val config = PreferencesHelper.getUploadConfig(this)

        @Suppress("DEPRECATION")
        val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)

        if (uri != null) {
            uploadFileFromUri(uri, config)
        } else {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (text != null) {
                uploadText(text, config)
            } else {
                Toast.makeText(this, "Nothing to upload", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleSendMultipleIntent(intent: Intent) {
        @Suppress("DEPRECATION")
        val uris: ArrayList<Uri>? = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        if (uris.isNullOrEmpty()) {
            Toast.makeText(this, "No files to upload", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val config = PreferencesHelper.getUploadConfig(this)

        scope.launch {
            for (uri in uris) {
                uploadFileFromUriSuspend(uri, config)
            }
            finish()
        }
    }

    private fun uploadFileFromUri(uri: Uri, config: UploadConfig) {
        scope.launch {
            uploadFileFromUriSuspend(uri, config)
            finish()
        }
    }

    private suspend fun uploadFileFromUriSuspend(uri: Uri, config: UploadConfig) {
        val fileName = getFileName(uri) ?: "shared_file"
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        val tempFile = withContext(Dispatchers.IO) {
            val temp = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            temp
        }

        try {
            val result = uploader.uploadFile(tempFile, fileName, mimeType, config)
            handleResult(result)
        } finally {
            tempFile.delete()
        }
    }

    private fun uploadText(text: String, config: UploadConfig) {
        scope.launch {
            val result = uploader.uploadText(text, config)
            handleResult(result)
            finish()
        }
    }

    private fun handleResult(result: UploadResult) {
        when (result) {
            is UploadResult.Success -> {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("MicroBin URL", result.pasteUrl))
                Toast.makeText(this, "Uploaded — URL copied", Toast.LENGTH_SHORT).show()
            }
            is UploadResult.Error -> {
                Toast.makeText(this, "Upload failed: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
        }
        return uri.lastPathSegment
    }
}
