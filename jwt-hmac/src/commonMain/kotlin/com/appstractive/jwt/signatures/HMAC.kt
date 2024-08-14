package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier

internal val provider by lazy { CryptographyProvider.Default }
internal val hmac by lazy { provider.get(HMAC) }

fun SignatureBuilder.hs256(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    algorithm(
        algorithm = Hmac(config = config),
        type = Algorithm.HS256,
    )
}

fun SignatureBuilder.hs384(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    algorithm(
        algorithm = Hmac(config = config),
        type = Algorithm.HS384,
    )
}

fun SignatureBuilder.hs512(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    algorithm(
        algorithm = Hmac(config = config),
        type = Algorithm.HS512,
    )
}

fun VerificationBuilder.hs256(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    verifier(
        type = Algorithm.HS256,
        algorithm = Hmac(config = config),
    )
}

fun VerificationBuilder.hs384(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    verifier(
        type = Algorithm.HS384,
        algorithm = Hmac(config = config),
    )
}

fun VerificationBuilder.hs512(configure: Hmac.Config.() -> Unit) {
    val config = Hmac.Config().apply(configure)
    verifier(
        type = Algorithm.HS512,
        algorithm = Hmac(config = config),
    )
}

class Hmac(
    private val config: Config,
) : SigningAlgorithm, VerificationAlgorithm {

    override suspend fun sign(header: Header, claims: Claims): ByteArray {
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