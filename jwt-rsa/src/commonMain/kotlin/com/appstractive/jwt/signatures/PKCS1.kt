package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.operations.SignatureGenerator
import dev.whyoleg.cryptography.operations.SignatureVerifier

private val provider by lazy { CryptographyProvider.Default }
internal val pkcs1: RSA.PKCS1 by lazy {
  provider.get(RSA.PKCS1)
}

fun Signer.rs256(configure: RSASigningConfig.() -> Unit) {
  val config = RSASigningConfig().apply(configure)
  algorithm(
      algorithm = PKCS1Signer(config = config),
      type = Algorithm.RS256,
  )
}

fun Signer.rs384(configure: RSASigningConfig.() -> Unit) {
  val config = RSASigningConfig().apply(configure)
  algorithm(
      algorithm = PKCS1Signer(config = config),
      type = Algorithm.RS384,
  )
}

fun Signer.rs512(configure: RSASigningConfig.() -> Unit) {
  val config = RSASigningConfig().apply(configure)
  algorithm(
      algorithm = PKCS1Signer(config = config),
      type = Algorithm.RS512,
  )
}

fun Verifier.rs256(configure: RSAVerifierConfig.() -> Unit) {
  val config = RSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.RS256,
      algorithm = PKCS1Verifier(config = config),
  )
}

fun Verifier.rs384(configure: RSAVerifierConfig.() -> Unit) {
  val config = RSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.RS384,
      algorithm = PKCS1Verifier(config = config),
  )
}

fun Verifier.rs512(configure: RSAVerifierConfig.() -> Unit) {
  val config = RSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.RS512,
      algorithm = PKCS1Verifier(config = config),
  )
}

internal class PKCS1Signer(
    private val config: RSASigningConfig,
) : SigningAlgorithm {
  override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
    return pkcs1
        .privateKeyDecoder(digest)
        .decodeFromByteArray(checkNotNull(config.format), checkNotNull(config.privateKey))
        .signatureGenerator()
  }
}

internal class PKCS1Verifier(
    private val config: RSAVerifierConfig,
) : VerificationAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return pkcs1
        .publicKeyDecoder(digest = jwt.header.alg.digest)
        .decodeFromByteArray(checkNotNull(config.format), checkNotNull(config.publicKey))
        .signatureVerifier()
  }
}
