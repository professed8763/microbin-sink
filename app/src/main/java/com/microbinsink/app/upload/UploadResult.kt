package com.microbinsink.app.upload

sealed class UploadResult {
    data class Success(val pasteUrl: String) : UploadResult()
    data class Error(val message: String, val exception: Throwable? = null) : UploadResult()
}
