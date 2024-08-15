@file:OptIn(ExperimentalEncodingApi::class)

package com.appstractive.jwt.utils

import com.appstractive.jwt.Claims
import com.appstractive.jwt.Header
import com.appstractive.jwt.JWT
import com.appstractive.jwt.UnsignedJWT
import kotlin.io.encoding.Base64.Default.UrlSafe
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.encodeToString

fun UnsignedJWT.urlEncoded(): String = "${header.urlEncoded()}.${claims.urlEncoded()}"

fun JWT.urlEncoded(): String = "${header.urlEncoded()}.${claims.urlEncoded()}"

fun Header.urlEncoded(): String {
    return urlEncoded(json.encodeToString(this))
}

fun Claims.urlEncoded(): String {
    return urlEncoded(json.encodeToString(this))
}

fun urlEncoded(source: String): String {
    return urlEncoded(source.encodeToByteArray())
}

fun urlEncoded(source: ByteArray): String {
    return UrlSafe.encode(source).replace("=", "")
}

fun String.urlDecodedString(): String {
    return UrlSafe.decode(this).decodeToString()
}

fun String.urlDecodedBytes(): ByteArray {
    return UrlSafe.decode(this)
}
