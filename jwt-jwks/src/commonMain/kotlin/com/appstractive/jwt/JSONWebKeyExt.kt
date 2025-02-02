package com.appstractive.jwt

import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.operations.SignatureVerifier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun JSONWebKeySet.getKey(kid: String?): JSONWebKey =
    when {
      kid == null && keys.size == 1 -> {
        keys.first()
      }

      kid != null -> {
        keys.first { it.kid == kid }
      }

      else -> throw IllegalArgumentException("No valid key found for JWT")
    }

suspend fun JSONWebKey.getVerifier(serializer: Json = json): SignatureVerifier {
  val digest = alg?.digest ?: SHA256

  return when (this) {
    is JSONWebKeyEC ->
      ecdsa
          .publicKeyDecoder(curve = crv.curve)
          .decodeFromByteArray(
              format = EC.PublicKey.Format.JWK,
              bytes = serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
          )
          .signatureVerifier(digest, ECDSA.SignatureFormat.RAW)

    is JSONWebKeyHMAC ->
      hmac
          .keyDecoder(digest)
          .decodeFromByteArray(
              format = HMAC.Key.Format.JWK,
              bytes = serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
          )
          .signatureVerifier()

    is JSONWebKeyRSA ->
      when (alg) {
        Algorithm.PS256,
        Algorithm.PS384,
        Algorithm.PS512 ->
          pss.publicKeyDecoder(digest)
              .decodeFromByteArrayBlocking(
                  format = RSA.PublicKey.Format.JWK,
                  bytes =
                  serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
              )
              .signatureVerifier()

        Algorithm.RS256,
        Algorithm.RS384,
        Algorithm.RS512 ->
          pkcs1
              .publicKeyDecoder(digest)
              .decodeFromByteArrayBlocking(
                  format = RSA.PublicKey.Format.JWK,
                  bytes =
                  serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
              )
              .signatureVerifier()

        else -> throw IllegalArgumentException("Unknown algorithm $alg")
      }
  }
}
