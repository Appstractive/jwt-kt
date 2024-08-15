package com.appstractive.jwt

import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

enum class CurveType {
  @SerialName("P-256") P256,
  @SerialName("P-384") P384,
  @SerialName("P-521") P521,
}

val CurveType.curve: EC.Curve
  get() =
      when (this) {
        CurveType.P256 -> EC.Curve.P256
        CurveType.P384 -> EC.Curve.P384
        CurveType.P521 -> EC.Curve.P521
      }

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kty")
sealed interface JSONWebKey {
  val alg: Algorithm?
  val kid: String?
}

@Serializable
@SerialName("RSA")
data class JSONWebKeyRSA(
    override val alg: Algorithm? = null,
    override val kid: String,
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
    override val alg: Algorithm? = null,
    override val kid: String,
    val crv: CurveType,
    val x: String,
    val y: String,
    val d: String,
) : JSONWebKey

@Serializable
@SerialName("oct")
data class JSONWebKeyHMAC(
    override val alg: Algorithm? = null,
    override val kid: String,
    val k: String,
) : JSONWebKey

@Serializable
data class JSONWebKeySet(
    val keys: List<JSONWebKey>,
)
