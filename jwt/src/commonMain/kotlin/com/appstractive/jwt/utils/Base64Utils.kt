@file:OptIn(ExperimentalEncodingApi::class)

package com.appstractive.jwt.utils

import com.appstractive.jwt.Header
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64.Default.UrlSafe
import kotlin.io.encoding.ExperimentalEncodingApi

fun Header.urlEncoded(): String {
    return urlEncoded(json.encodeToString(this))
}

fun JsonObject.urlEncoded(): String {
    return urlEncoded(json.encodeToString(this))
}

fun urlEncoded(source: String): String {
    return urlEncoded(source.encodeToByteArray())
}

fun urlEncoded(source: ByteArray): String {
    return UrlSafe.encode(source).replace("=", "")
}
