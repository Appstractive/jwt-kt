package com.appstractive.jwt

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val provider by lazy { CryptographyProvider.Default }
internal val pkcs1: RSA<RSA.PKCS1.PublicKey, RSA.PKCS1.PrivateKey, RSA.PKCS1.KeyPair> by lazy {
  provider.get(RSA.PKCS1)
}
internal val hmac by lazy { provider.get(HMAC) }
internal val ecdsa: ECDSA by lazy { provider.get(ECDSA) }

@OptIn(CryptographyProviderApi::class)
class JwksVerifier(
    private val config: JwksConfig,
) : VerificationAlgorithm {

  private var keySet: JSONWebKeySet = JSONWebKeySet(emptyList())
  private var lastUpdate: Instant = Clock.System.now()

  private val keyVerifier: MutableMap<String?, SignatureVerifier> = mutableMapOf()

  private val serializer: Json = Json {
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
    ignoreUnknownKeys = true
  }

  private val client =
      config.client.config {
        install(ContentNegotiation) {
          json(serializer)
        }
      }
  private val endpoint = checkNotNull(config.endpoint) { "Endpoint not configured" }

  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    updateKeySet()

    val kid = jwt.header.kid
    val key: JSONWebKey =
        when {
          kid == null && keySet.keys.size == 1 -> {
            keySet.keys.first()
          }
          kid != null -> {
            keySet.keys.first { it.kid == kid }
          }
          else -> throw IllegalArgumentException("No valid key found for JWT")
        }

    val verifier: SignatureVerifier =
        getVerifier(
            key = key,
        )

    return object : SignatureVerifier {
      override fun verifySignatureBlocking(
          dataInput: ByteArray,
          signatureInput: ByteArray
      ): Boolean {
        return verifier.verifySignatureBlocking(
            dataInput = dataInput, signatureInput = signatureInput)
      }
    }
  }

  private suspend fun getVerifier(key: JSONWebKey): SignatureVerifier {
    val digest = key.alg?.digest ?: SHA256

    return keyVerifier.getOrPut(key.kid) {
      when (key) {
        is JSONWebKeyEC ->
          ecdsa
              .publicKeyDecoder(curve = key.crv.curve)
              .decodeFrom(
                  format = EC.PublicKey.Format.JWK,
                  input = serializer.encodeToString(key).encodeToByteArray(), // TODO NYI in Crypto
              )
              .signatureVerifier(digest)

        is JSONWebKeyHMAC ->
          hmac
              .keyDecoder(digest)
              .decodeFrom(
                  format = HMAC.Key.Format.JWK,
                  input = serializer.encodeToString(key).encodeToByteArray(), // TODO NYI in Crypto
              )
              .signatureVerifier()

        is JSONWebKeyRSA ->
          pkcs1
              .publicKeyDecoder(digest)
              .decodeFromBlocking(
                  format = RSA.PublicKey.Format.JWK,
                  input = serializer.encodeToString(key).encodeToByteArray(), // TODO NYI in Crypto
              )
              .signatureVerifier()
      }
    }
  }

  private suspend fun updateKeySet() {
    if (Clock.System.now() - lastUpdate > config.cacheDuration) {
      val response = client.get(endpoint)

      if (response.status == HttpStatusCode.OK) {
        lastUpdate = Clock.System.now()
        keySet = response.body()
      }
    }
  }
}

class JwksConfig {
  var endpoint: String? = null
  var client: HttpClient = HttpClient()
  var cacheDuration: Duration = 24.hours
}
