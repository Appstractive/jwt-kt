package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.digest.SHA384
import dev.whyoleg.cryptography.algorithms.digest.SHA512
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
import kotlinx.serialization.json.JsonObject

fun SignatureBuilder.hs256(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    algorithm = HS256(config)
}

fun SignatureBuilder.hs384(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    algorithm = HS384(config)
}

fun SignatureBuilder.hs512(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    algorithm = HS512(config)
}

abstract class Hmac(
    private val digest: CryptographyAlgorithmId<Digest>,
    private val config: Config,
) : SigningAlgorithm<Hmac.Config> {

    private val provider = CryptographyProvider.Default
    private val hmac = provider.get(HMAC)

    override val type: Algorithm
        get() = when(digest) {
            SHA256 -> Algorithm.RS256
            SHA384 -> Algorithm.RS384
            SHA512 -> Algorithm.RS512
            else -> throw IllegalArgumentException("Unsupported digest algorithm")
        }

    override suspend fun sign(header: Header, claims: JsonObject): ByteArray {
        val headerEncoded = header.urlEncoded()
        val claimsEncoded = claims.urlEncoded()

        val key = hmac.keyDecoder(digest).decodeFrom(HMAC.Key.Format.RAW, config.secret)

        return key.signatureGenerator().generateSignature("$headerEncoded.$claimsEncoded".encodeToByteArray())
    }

    class Config : SigningAlgorithmConfig {
        var secret: ByteArray = ByteArray(0)
    }
}

class HS256(config: Config) : Hmac(digest = SHA256, config = config)

class HS384(config: Config) : Hmac(digest = SHA384, config = config)

class HS512(config: Config) : Hmac(digest = SHA512, config = config)