package com.appstractive.jwt

import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.digest.SHA384
import dev.whyoleg.cryptography.algorithms.digest.SHA512
import kotlinx.serialization.json.JsonObject

enum class Algorithm {
    HS256,
    HS384,
    HS512,
    RS256,
    RS384,
    RS512,
    PS256,
    PS384,
    PS512,
    EC256,
    EC384,
    EC512,
}

interface SigningAlgorithmConfig

interface SigningAlgorithm<C : SigningAlgorithmConfig> {

    val type: Algorithm

    suspend fun sign(header: Header, claims: JsonObject): ByteArray
}

class SignatureBuilder {

    internal var algorithm: SigningAlgorithm<*>? = null
    internal val type: Algorithm
        get() = algorithm?.type ?: throw IllegalStateException("No signature configured")

    internal suspend fun sign(header: Header, claims: JsonObject): ByteArray {
        return algorithm?.sign(
            header = header,
            claims = claims,
        ) ?: throw IllegalStateException("No algorithm provided")
    }
}

suspend fun UnsignedJWT.sign(builder: SignatureBuilder.() -> Unit): JWT {
    val signature = SignatureBuilder().apply(builder)
    val finalHeader = header.copy(
        alg = signature.type,
        kid = header.kid,
    )
    return JWT(
        header = finalHeader,
        claims = claims,
        signature = signature.sign(
            header = finalHeader,
            claims = claims,
        ),
    )
}

val Algorithm.digest: CryptographyAlgorithmId<Digest>
    get() = when(this) {
        Algorithm.EC256,
        Algorithm.PS256,
        Algorithm.RS256,
        Algorithm.HS256 -> SHA256
        Algorithm.EC384,
        Algorithm.RS384,
        Algorithm.PS384,
        Algorithm.HS384 -> SHA384
        Algorithm.EC512,
        Algorithm.HS512,
        Algorithm.RS512,
        Algorithm.PS512 -> SHA512
    }
