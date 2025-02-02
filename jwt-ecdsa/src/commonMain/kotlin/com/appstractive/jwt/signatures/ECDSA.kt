package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.ECDSA.SignatureFormat
import dev.whyoleg.cryptography.operations.SignatureGenerator
import dev.whyoleg.cryptography.operations.SignatureVerifier

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
        .decodeFromByteArray(checkNotNull(config.privateKeyFormat), checkNotNull(config.privateKey))
        .signatureGenerator(digest = digest, format = config.signatureFormat)
  }
}

internal class ECDSAVerifier(
    private val config: ECDSAVerifierConfig,
) : VerificationAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return ecdsa
        .publicKeyDecoder(curve = config.curve)
        .decodeFromByteArray(checkNotNull(config.publicKeyFormat), checkNotNull(config.publicKey))
        .signatureVerifier(digest = jwt.header.alg.digest, format = config.signatureFormat)
  }
}

class ECDSASignerConfig {
  internal var privateKey: ByteArray? = null
  internal var privateKeyFormat: EC.PrivateKey.Format? = null
  internal var curve: EC.Curve = EC.Curve.P256
  internal var signatureFormat: SignatureFormat = SignatureFormat.RAW

  fun pem(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key
    privateKeyFormat = EC.PrivateKey.Format.PEM
    this.curve = curve
  }

  fun pem(key: String, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key.encodeToByteArray()
    privateKeyFormat = EC.PrivateKey.Format.PEM
    this.curve = curve
  }

  fun der(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key
    privateKeyFormat = EC.PrivateKey.Format.DER
    this.curve = curve
  }

  fun der(key: String, curve: EC.Curve = EC.Curve.P256) {
    privateKey = key.encodeToByteArray()
    privateKeyFormat = EC.PrivateKey.Format.DER
    this.curve = curve
  }
}

class ECDSAVerifierConfig {
  internal var publicKey: ByteArray? = null
  internal var publicKeyFormat: EC.PublicKey.Format? = null
  internal var curve: EC.Curve = EC.Curve.P256
  internal var signatureFormat: SignatureFormat = SignatureFormat.RAW

  fun pem(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key
    publicKeyFormat = EC.PublicKey.Format.PEM
    this.curve = curve
  }

  fun pem(key: String, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key.encodeToByteArray()
    publicKeyFormat = EC.PublicKey.Format.PEM
    this.curve = curve
  }

  fun der(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key
    publicKeyFormat = EC.PublicKey.Format.DER
    this.curve = curve
  }

  fun der(key: String, curve: EC.Curve = EC.Curve.P256) {
    publicKey = key.encodeToByteArray()
    publicKeyFormat = EC.PublicKey.Format.DER
    this.curve = curve
  }
}
