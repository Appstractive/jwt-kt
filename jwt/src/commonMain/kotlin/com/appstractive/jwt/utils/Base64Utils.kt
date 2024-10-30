@file:OptIn(ExperimentalEncodingApi::class, ExperimentalEncodingApi::class)

package com.appstractive.jwt.utils

import com.appstractive.jwt.Claims
import com.appstractive.jwt.Header
import com.appstractive.jwt.UnsignedJWT
import kotlin.io.encoding.Base64
import kotlin.io.encoding.Base64.Default.UrlSafe
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.encodeToString

fun UnsignedJWT.urlEncoded(): String = "${header.urlEncoded()}.${claims.urlEncoded()}"

private val base64 = UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

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
  return base64.encode(source)
}

fun String.urlDecodedString(): String {
  return base64.decode(this).decodeToString()
}

fun String.urlDecodedBytes(): ByteArray {
  return base64.decode(this)
}
