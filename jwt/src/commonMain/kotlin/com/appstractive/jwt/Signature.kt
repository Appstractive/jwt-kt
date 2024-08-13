package com.appstractive.jwt

import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
import kotlinx.serialization.json.JsonObject

enum class Algorithm {
    HS256,
}

interface SigningAlgorithmConfig

interface SigningAlgorithm<C : SigningAlgorithmConfig> {

    val type: Algorithm

    suspend fun sign(headerEncoded: String, claimsEncoded: String): ByteArray

    class HS256(val config: Config) : SigningAlgorithm<HS256.Config> {
        override val type: Algorithm = Algorithm.HS256

        private val provider = CryptographyProvider.Default
        private val hmac = provider.get(HMAC)

        override suspend fun sign(headerEncoded: String, claimsEncoded: String): ByteArray {
            val key = hmac.keyDecoder(SHA256).decodeFrom(HMAC.Key.Format.RAW, config.secret.encodeToByteArray())

            return key.signatureGenerator().generateSignature("$headerEncoded.$claimsEncoded".encodeToByteArray())
        }

        class Config : SigningAlgorithmConfig {
            var secret: String = ""
        }
    }

}

class SignatureBuilder {

    private var algorithm: SigningAlgorithm<*>? = null
    internal val type: Algorithm
        get() = algorithm?.type ?: throw IllegalStateException("No signature configured")

    fun hs256(configure: SigningAlgorithm.HS256.Config.() -> Unit) {
        val config = SigningAlgorithm.HS256.Config().apply(configure)
        algorithm = SigningAlgorithm.HS256(config)
    }

    internal suspend fun sign(header: Header, claims: JsonObject): ByteArray {
        val headerEncoded = header.urlEncoded()
        val claimsEncoded = claims.urlEncoded()

        return algorithm?.sign(
            headerEncoded = headerEncoded,
            claimsEncoded = claimsEncoded,
        ) ?: throw IllegalStateException("No algorithm provided")
    }
}
