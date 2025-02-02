package com.appstractive.jwt

import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier
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
          .decodeFrom(
              format = EC.PublicKey.Format.JWK,
              input = serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
          )
          .signatureVerifier(digest)

    is JSONWebKeyHMAC ->
      hmac
          .keyDecoder(digest)
          .decodeFrom(
              format = HMAC.Key.Format.JWK,
              input = serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
          )
          .signatureVerifier()

    is JSONWebKeyRSA ->
      when (alg) {
        Algorithm.PS256,
        Algorithm.PS384,
        Algorithm.PS512 ->
          pss.publicKeyDecoder(digest)
              .decodeFromBlocking(
                  format = RSA.PublicKey.Format.JWK,
                  input =
                  serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
              )
              .signatureVerifier()

        Algorithm.RS256,
        Algorithm.RS384,
        Algorithm.RS512 ->
          pkcs1
              .publicKeyDecoder(digest)
              .decodeFromBlocking(
                  format = RSA.PublicKey.Format.JWK,
                  input =
                  serializer.encodeToString(this).encodeToByteArray(), // TODO NYI in Crypto
              )
              .signatureVerifier()

        else -> throw IllegalArgumentException("Unknown algorithm $alg")
      }
  }
}
