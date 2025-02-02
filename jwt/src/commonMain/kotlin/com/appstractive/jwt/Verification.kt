package com.appstractive.jwt

import dev.whyoleg.cryptography.operations.SignatureVerifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface VerificationAlgorithm {
  suspend fun verifier(jwt: JWT): SignatureVerifier

  suspend fun verify(jwt: JWT): Boolean {
    return verifier(jwt = jwt).tryVerifySignature(jwt.signedData.encodeToByteArray(), jwt.signature)
  }
}

class Verifier {
  private val algorithms: MutableMap<Algorithm, VerificationAlgorithm> = mutableMapOf()

  private var verifyExpiresAt: Instant? = null
  private var verifyNotBefore: Instant? = null

  private var audiences: MutableSet<String> = mutableSetOf()
  private var issuers: MutableSet<String> = mutableSetOf()

  fun algorithm(type: Algorithm, algorithm: VerificationAlgorithm) {
    algorithms[type] = algorithm
  }

  fun audience(vararg audience: String) {
    audiences.addAll(audience)
  }

  fun issuer(vararg issuer: String) {
    issuers.addAll(issuer)
  }

  fun expiresAt(now: Instant = Clock.System.now()) {
    verifyExpiresAt = now
  }

  fun notBefore(now: Instant = Clock.System.now()) {
    verifyNotBefore = now
  }

  internal suspend fun verify(jwt: JWT): Boolean {
    if (audiences.isNotEmpty() && !audiences.contains(jwt.audience)) {
      return false
    }

    if (issuers.isNotEmpty() && !issuers.contains(jwt.issuer)) {
      return false
    }

    verifyExpiresAt?.let {
      val expiresAt = jwt.expiresAt ?: return false

      if (expiresAt < it) {
        return false
      }
    }

    verifyNotBefore?.let {
      val notBefore = jwt.notBefore ?: return false

      if (notBefore >= it) {
        return false
      }
    }

    return algorithms[jwt.header.alg]?.verify(jwt = jwt)
      ?: throw IllegalStateException("No verifier configured for ${jwt.header.alg}")
  }
}

fun jwtVerifier(builder: Verifier.() -> Unit): Verifier = Verifier().apply(builder)

suspend fun JWT.verify(builder: Verifier.() -> Unit): Boolean = verify(jwtVerifier(builder))

suspend fun JWT.verify(verifier: Verifier): Boolean {
  return verifier.verify(this)
}
