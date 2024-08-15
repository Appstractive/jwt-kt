package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier

internal val provider by lazy { CryptographyProvider.Default }
internal val hmac by lazy { provider.get(HMAC) }

fun Signer.hs256(configure: Hmac.Config.() -> Unit) {
  val config = Hmac.Config().apply(configure)
  algorithm(
      algorithm = Hmac(config = config),
      type = Algorithm.HS256,
  )
}

fun Signer.hs384(configure: Hmac.Config.() -> Unit) {
  val config = Hmac.Config().apply(configure)
  algorithm(
      algorithm = Hmac(config = config),
      type = Algorithm.HS384,
  )
}

fun Signer.hs512(configure: Hmac.Config.() -> Unit) {
  val config = Hmac.Config().apply(configure)
  algorithm(
      algorithm = Hmac(config = config),
      type = Algorithm.HS512,
  )
}

fun Verifier.hs256(configure: Hmac.Config.() -> Unit) {
  val config = Hmac.Config().apply(configure)
  algorithm(
      type = Algorithm.HS256,
      algorithm = Hmac(config = config),
  )
}

fun Verifier.hs384(configure: Hmac.Config.() -> Unit) {
  val config = Hmac.Config().apply(configure)
  algorithm(
      type = Algorithm.HS384,
      algorithm = Hmac(config = config),
  )
}

fun Verifier.hs512(configure: Hmac.Config.() -> Unit) {
  val config = Hmac.Config().apply(configure)
  algorithm(
      type = Algorithm.HS512,
      algorithm = Hmac(config = config),
  )
}

class Hmac(
    private val config: Config,
) : SigningAlgorithm, VerificationAlgorithm {

  override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
    return hmac
        .keyDecoder(digest)
        .decodeFrom(HMAC.Key.Format.RAW, config.secret)
        .signatureGenerator()
  }

  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return hmac
        .keyDecoder(jwt.header.alg.digest)
        .decodeFrom(HMAC.Key.Format.RAW, config.secret)
        .signatureVerifier()
  }

  class Config {
    var secret: ByteArray = ByteArray(0)
  }
}
