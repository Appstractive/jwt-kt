﻿package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier

internal val provider by lazy { CryptographyProvider.Default }
internal val ecdsa: ECDSA by lazy { provider.get(ECDSA) }

fun Signer.es256(configure: ECDSASignerConfig.() -> Unit) {
  val config = ECDSASignerConfig().apply(configure)
  algorithm(
      algorithm = ECDSASigner(config = config),
      type = Algorithm.ES256,
  )
}

fun Signer.es384(configure: ECDSASignerConfig.() -> Unit) {
  val config = ECDSASignerConfig().apply(configure)
  algorithm(
      algorithm = ECDSASigner(config = config),
      type = Algorithm.ES384,
  )
}

fun Signer.es512(configure: ECDSASignerConfig.() -> Unit) {
  val config = ECDSASignerConfig().apply(configure)
  algorithm(
      algorithm = ECDSASigner(config = config),
      type = Algorithm.ES512,
  )
}

fun Verifier.es256(configure: ECDSAVerifierConfig.() -> Unit) {
  val config = ECDSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.ES256,
      algorithm = ECDSAVerifier(config = config),
  )
}

fun Verifier.es384(configure: ECDSAVerifierConfig.() -> Unit) {
  val config = ECDSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.ES384,
      algorithm = ECDSAVerifier(config = config),
  )
}

fun Verifier.es512(configure: ECDSAVerifierConfig.() -> Unit) {
  val config = ECDSAVerifierConfig().apply(configure)
  algorithm(
      type = Algorithm.ES512,
      algorithm = ECDSAVerifier(config = config),
  )
}

internal class ECDSASigner(
    private val config: ECDSASignerConfig,
) : SigningAlgorithm {

  override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
    return ecdsa
        .privateKeyDecoder(config.curve)
        .decodeFrom(checkNotNull(config.format), checkNotNull(config.privateKey))
        .signatureGenerator(digest)
  }
}

internal class ECDSAVerifier(
    private val config: ECDSAVerifierConfig,
) : VerificationAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return ecdsa
        .publicKeyDecoder(curve = config.curve)
        .decodeFrom(checkNotNull(config.format), checkNotNull(config.publicKey))
        .signatureVerifier(digest = jwt.header.alg.digest)
  }
}

class ECDSASignerConfig {
  internal var privateKey: ByteArray? = null
  internal var format: EC.PrivateKey.Format? = null
  internal var curve: EC.Curve = EC.Curve.P256

  fun pem(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key
    format = EC.PrivateKey.Format.PEM
    this.curve = curve
  }

  fun pem(key: String, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key.encodeToByteArray()
    format = EC.PrivateKey.Format.PEM
    this.curve = curve
  }

  fun der(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key
    format = EC.PrivateKey.Format.DER
    this.curve = curve
  }

  fun der(key: String, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key.encodeToByteArray()
    format = EC.PrivateKey.Format.DER
    this.curve = curve
  }
}

class ECDSAVerifierConfig {
  internal var publicKey: ByteArray? = null
  internal var format: EC.PublicKey.Format? = null
  internal var curve: EC.Curve = EC.Curve.P256

  fun pem(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key
    format = EC.PublicKey.Format.PEM
    this.curve = curve
  }

  fun pem(key: String, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key.encodeToByteArray()
    format = EC.PublicKey.Format.PEM
    this.curve = curve
  }

  fun der(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key
    format = EC.PublicKey.Format.DER
    this.curve = curve
  }

  fun der(key: String, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key.encodeToByteArray()
    format = EC.PublicKey.Format.DER
    this.curve = curve
  }
}
