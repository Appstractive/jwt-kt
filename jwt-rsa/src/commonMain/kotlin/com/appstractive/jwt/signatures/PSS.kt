package com.appstractive.jwt.signatures

import com.appstractive.jwt.Algorithm
import com.appstractive.jwt.JWT
import com.appstractive.jwt.Signer
import com.appstractive.jwt.SigningAlgorithm
import com.appstractive.jwt.VerificationAlgorithm
import com.appstractive.jwt.Verifier
import com.appstractive.jwt.digest
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.operations.SignatureGenerator
import dev.whyoleg.cryptography.operations.SignatureVerifier

private val provider by lazy { CryptographyProvider.Default }
internal val pss: RSA.PSS by lazy { provider.get(RSA.PSS) }

fun Signer.ps256(configure: RSASigningConfig.() -> Unit) {
  val config = RSASigningConfig().apply(configure)
  algorithm(
      algorithm = PSSSigner(config = config),
      type = Algorithm.PS256,
  )
}

fun Signer.ps384(configure: RSASigningConfig.() -> Unit) {
  val config = RSASigningConfig().apply(configure)
  algorithm(
      algorithm = PSSSigner(config = config),
      type = Algorithm.PS384,
  )
}

fun Signer.ps512(configure: RSASigningConfig.() -> Unit) {
  val config = RSASigningConfig().apply(configure)
  algorithm(
      algorithm = PSSSigner(config = config),
      type = Algorithm.PS512,
  )
}

fun Verifier.ps256(configure: RSAVerifierConfig.() -> Unit) {
  val config = RSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.PS256,
      algorithm = PSSVerifier(config = config),
  )
}

fun Verifier.ps384(configure: RSAVerifierConfig.() -> Unit) {
  val config = RSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.PS384,
      algorithm = PSSVerifier(config = config),
  )
}

fun Verifier.ps512(configure: RSAVerifierConfig.() -> Unit) {
  val config = RSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.PS512,
      algorithm = PSSVerifier(config = config),
  )
}

internal class PSSSigner(
  private val config: RSASigningConfig,
) : SigningAlgorithm {
  override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
    return pss.privateKeyDecoder(digest)
        .decodeFromByteArray(checkNotNull(config.format), checkNotNull(config.privateKey))
        .signatureGenerator()
  }
}

internal class PSSVerifier(
  private val config: RSAVerifierConfig,
) : VerificationAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return pss.publicKeyDecoder(digest = jwt.header.alg.digest)
        .decodeFromByteArray(checkNotNull(config.format), checkNotNull(config.publicKey))
        .signatureVerifier()
  }
}
