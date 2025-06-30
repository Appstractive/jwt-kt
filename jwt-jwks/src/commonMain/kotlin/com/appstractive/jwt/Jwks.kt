package com.appstractive.jwt

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.operations.SignatureVerifier
import dev.whyoleg.cryptography.operations.VerifyFunction
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Clock
import kotlin.time.Instant

private val provider by lazy { CryptographyProvider.Default }
internal val pkcs1: RSA<RSA.PKCS1.PublicKey, RSA.PKCS1.PrivateKey, RSA.PKCS1.KeyPair> by lazy {
  provider.get(RSA.PKCS1)
}
internal val hmac by lazy { provider.get(HMAC) }
internal val ecdsa: ECDSA by lazy { provider.get(ECDSA) }
internal val pss: RSA.PSS by lazy { provider.get(RSA.PSS) }

@OptIn(CryptographyProviderApi::class)
internal class JwksVerifier(
  private val config: JwksConfig,
) : VerificationAlgorithm {

  private var keySet: JSONWebKeySet = JSONWebKeySet(emptyList())
  private var lastUpdate: Instant = Clock.System.now() - config.cacheDuration

  private val keyVerifier: MutableMap<String?, SignatureVerifier> = mutableMapOf()

  private val client = config.client.config { install(ContentNegotiation) { json(json) } }
  private val endpoint = checkNotNull(config.endpoint) { "Endpoint not configured" }

  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    updateKeySet()

    val kid = jwt.header.kid
    val key: JSONWebKey = keySet.getKey(kid)

    val verifier: SignatureVerifier = keyVerifier.getOrPut(kid) { key.getVerifier() }

    return object : SignatureVerifier {
      override fun createVerifyFunction(): VerifyFunction {
        return verifier.createVerifyFunction()
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

fun Verifier.jwks(configure: JwksConfig.() -> Unit) {
  val config = JwksConfig().apply(configure)
  val jwks = JwksVerifier(config = config)
  Algorithm.entries.forEach {
    algorithm(
        type = it,
        algorithm = jwks,
    )
  }
}
