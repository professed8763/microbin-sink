package com.microbinsink.app.upload

data class UploadConfig(
    val serverUrl: String,
    val privacy: String = "public",
    val expiration: String = "1hour",
    val burnAfter: String = "0",
    val uploaderPassword: String = "",
)
