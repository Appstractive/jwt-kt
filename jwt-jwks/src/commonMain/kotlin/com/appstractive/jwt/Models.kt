package com.appstractive.jwt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

enum class CurveType {
  @SerialName("P-256")
  P256,
  @SerialName("P-384")
  P384,
  @SerialName("P-521")
  P521,
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kty")
sealed interface JSONWebKey

@Serializable
@SerialName("RSA")
data class JSONWebKeyRSA(
    val alg: Algorithm? = null,
    val kid: String,
    val use: String,
    val n: String,
    val e: String,
    val d: String? = null,
    val p: String? = null,
    val q: String? = null,
    val dp: String? = null,
    val dq: String? = null,
    val qi: String? = null,
) : JSONWebKey

@Serializable
@SerialName("EC")
data class JSONWebKeyEC(
    val crv: CurveType,
    val x: String,
    val y: String,
    val d: String,
) : JSONWebKey

@Serializable
@SerialName("oct")
data class JSONWebKeyHMAC(
    val kid: String,
    val alg: Algorithm? = null,
    val k: String,
) : JSONWebKey

@Serializable
data class JSONWebKeySet(
    val keys: List<JSONWebKey>,
)
