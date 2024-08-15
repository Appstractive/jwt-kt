package com.appstractive.jwt

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier
import io.ktor.client.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private val provider by lazy { CryptographyProvider.Default }
internal val pkcs1: RSA<RSA.PKCS1.PublicKey, RSA.PKCS1.PrivateKey, RSA.PKCS1.KeyPair> by lazy {
  provider.get(RSA.PKCS1)
}

@OptIn(CryptographyProviderApi::class)
class JwksVerifier(
    private val config: JwksConfig,
): VerificationAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {

    return object : SignatureVerifier {
      override fun verifySignatureBlocking(dataInput: ByteArray, signatureInput: ByteArray): Boolean {
        val publicKey = pkcs1.publicKeyDecoder(SHA256).decodeFromBlocking(RSA.PublicKey.Format.JWK, ByteArray(0))

        return publicKey.signatureVerifier().verifySignatureBlocking(dataInput = dataInput, signatureInput = signatureInput)
      }
    }
  }
}

class JwksConfig {
  var endpoint: String? = null
  var client: HttpClient = HttpClient()
  var cacheDuration: Duration = 24.hours
}
