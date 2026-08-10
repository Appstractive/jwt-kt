package com.appstractive.jwt

import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.operations.SignatureVerifier
import kotlinx.serialization.json.Json

fun JSONWebKeySet.getKey(kid: String?): JSONWebKey? =
    when {
      kid == null && keys.size == 1 -> {
        keys.firstOrNull()
      }

      kid != null -> {
        keys.firstOrNull { it.kid == kid }
      }

      else -> null
    }

@OptIn(CryptographyProviderApi::class)
suspend fun JSONWebKey.getVerifier(serializer: Json = json): SignatureVerifier {
  val digest = alg?.digest ?: SHA256

  return when (this) {
    is JSONWebKeyEC ->
      ecdsa
          .publicKeyDecoder(curve = crv.curve)
          .decodeFromByteArray(
              format = EC.PublicKey.Format.JWK,
              bytes = serializer
                  .encodeToString(this)
                  .encodeToByteArray(),
          )
          .signatureVerifier(digest, ECDSA.SignatureFormat.RAW)

    is JSONWebKeyHMAC ->
      hmac
          .keyDecoder(digest)
          .decodeFromByteArray(
              format = HMAC.Key.Format.JWK,
              bytes = serializer
                  .encodeToString(this)
                  .encodeToByteArray(),
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
                  bytes = serializer
                      .encodeToString(this)
                      .encodeToByteArray(),
              )
              .signatureVerifier()

        Algorithm.RS256,
        Algorithm.RS384,
        Algorithm.RS512 ->
          pkcs1
              .publicKeyDecoder(digest)
              .decodeFromByteArrayBlocking(
                  format = RSA.PublicKey.Format.JWK,
                  bytes = serializer
                      .encodeToString(this)
                      .encodeToByteArray(),
              )
              .signatureVerifier()

        else -> throw IllegalArgumentException("Unknown algorithm $alg")
      }
  }
}
