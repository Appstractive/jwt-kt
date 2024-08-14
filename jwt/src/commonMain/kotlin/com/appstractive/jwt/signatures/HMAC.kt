package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier
import kotlinx.serialization.json.JsonObject

fun SignatureBuilder.hs256(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    type = Algorithm.HS256
    algorithm = Hmac(config = config)
}

fun SignatureBuilder.hs384(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    type = Algorithm.HS384
    algorithm = Hmac(config = config)
}

fun SignatureBuilder.hs512(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    type = Algorithm.HS512
    algorithm = Hmac(config = config)
}

fun VerificationBuilder.hs256(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    Hmac(config).also {
        algorithms[Algorithm.HS256] = it
    }
}

fun VerificationBuilder.hs384(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    Hmac(config).also {
        algorithms[Algorithm.HS384] = it
    }
}

fun VerificationBuilder.hs512(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    Hmac(config).also {
        algorithms[Algorithm.HS512] = it
    }
}

class Hmac(
    private val config: Config,
) : SigningAlgorithm, VerificationAlgorithm {

    override suspend fun sign(header: Header, claims: JsonObject): ByteArray {
        val headerEncoded = header.urlEncoded()
        val claimsEncoded = claims.urlEncoded()

        val key = hmac.keyDecoder(header.alg.digest).decodeFrom(HMAC.Key.Format.RAW, config.secret)

        return key.signatureGenerator().generateSignature("$headerEncoded.$claimsEncoded".encodeToByteArray())
    }

    override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
        return hmac.keyDecoder(digest).decodeFrom(HMAC.Key.Format.RAW, config.secret).signatureGenerator()
    }

    override suspend fun verifier(jwt: JWT): SignatureVerifier {
        return hmac.keyDecoder(jwt.header.alg.digest).decodeFrom(HMAC.Key.Format.RAW, config.secret).signatureVerifier()
    }

    class Config {
        var secret: ByteArray = ByteArray(0)
    }
}