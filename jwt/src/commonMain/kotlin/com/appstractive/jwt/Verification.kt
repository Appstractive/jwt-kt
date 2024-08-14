package com.appstractive.jwt

import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface VerificationAlgorithm {
    suspend fun verifier(jwt: JWT): SignatureVerifier

    suspend fun verify(jwt: JWT): Boolean {
        return verifier(jwt = jwt).verifySignature(jwt.urlEncoded().encodeToByteArray(), jwt.signature)
    }
}

class VerificationBuilder {
    internal val algorithms: MutableMap<Algorithm, VerificationAlgorithm> = mutableMapOf()

    private var verifyExpiresAt: Instant? = null
    private var verifyNotBefore: Instant? = null

    var audience: String? = null
    var issuer: String? = null

    fun expiresAt(now: Instant = Clock.System.now()) {
        verifyExpiresAt = now
    }

    fun notBefore(now: Instant = Clock.System.now()) {
        verifyNotBefore = now
    }

    internal suspend fun verify(jwt: JWT): Boolean {
        audience?.let {
            if (jwt.audience != it) {
                return false
            }
        }

        issuer?.let {
            if (jwt.issuer != it) {
                return false
            }
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

suspend fun JWT.verify(builder: VerificationBuilder.() -> Unit): Boolean {
    val verifier = VerificationBuilder().apply(builder)
    return verifier.verify(this)
}
