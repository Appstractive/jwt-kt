package com.appstractive.jwt

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.operations.SignatureVerifier
import dev.whyoleg.cryptography.operations.VerifyFunction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

  private val updateMutex: Mutex = Mutex()
  private val cacheMutex: Mutex = Mutex()

  private val keySet: MutableStateFlow<JSONWebKeySet> = MutableStateFlow(JSONWebKeySet(emptyList()))
  private val lastUpdate: MutableStateFlow<Instant> = MutableStateFlow(Clock.System.now() - config.cacheDuration)

  private val keyVerifierCache: MutableStateFlow<Map<String?, SignatureVerifier>> = MutableStateFlow(emptyMap())

  private val client = config.client.config { install(ContentNegotiation) { json(json) } }
  private val endpoint = checkNotNull(config.endpoint) { "Endpoint not configured" }

  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    updateKeySet()
    return getOrPutVerifier(jwt)
  }

  private suspend fun getOrPutVerifier(
    jwt: JWT,
  ): SignatureVerifier = cacheMutex.withLock {
    val kid = jwt.header.kid
    val key: JSONWebKey? = keySet.value.getKey(kid)

    val cached = keyVerifierCache.value[kid]

    if (cached != null) {
      cached
    } else {
      val verifier = key?.getVerifier() ?: UnknownKeyVerifier(kid)
      keyVerifierCache.update {
        it + (kid to verifier)
      }
      verifier
    }
  }

  private suspend fun updateKeySet() {
    updateMutex.withLock {
      if (Clock.System.now() - lastUpdate.value > config.cacheDuration) {
        val response = client.get(endpoint)

        if (response.status == HttpStatusCode.OK) {
          lastUpdate.value = Clock.System.now()
          keySet.value = response.body()
        }
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

@OptIn(CryptographyProviderApi::class)
private class UnknownKeyVerifier(
  private val keyId: String?,
) : SignatureVerifier {

  private val function by lazy { UnknownKeyVerifyFunction(keyId = keyId) }

  override fun createVerifyFunction(): VerifyFunction = function
}

@OptIn(CryptographyProviderApi::class)
private class UnknownKeyVerifyFunction(
  private val keyId: String?,
) : VerifyFunction {
  override fun tryVerify(signature: ByteArray, startIndex: Int, endIndex: Int): Boolean = false
  override fun verify(signature: ByteArray, startIndex: Int, endIndex: Int) = error("Unknown JWKS keyId: $keyId")
  override fun reset() = Unit
  override fun update(source: ByteArray, startIndex: Int, endIndex: Int) = Unit
  override fun close() = Unit
}
