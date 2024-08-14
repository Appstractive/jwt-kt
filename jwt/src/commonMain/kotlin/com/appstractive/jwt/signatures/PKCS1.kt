package com.appstractive.jwt.signatures

import com.appstractive.jwt.Algorithm
import com.appstractive.jwt.SignatureBuilder
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.digest.SHA384
import dev.whyoleg.cryptography.algorithms.digest.SHA512

fun SignatureBuilder.rs256(configure: RSABase.Config.() -> Unit) {
    val config = RSABase.Config().apply(configure)
    algorithm = RS256(config)
}

fun SignatureBuilder.rs384(configure: RSABase.Config.() -> Unit) {
    val config = RSABase.Config().apply(configure)
    algorithm = RS384(config)
}

fun SignatureBuilder.rs512(configure: RSABase.Config.() -> Unit) {
    val config = RSABase.Config().apply(configure)
    algorithm = RS512(config)
}

abstract class PKCS1(
    config: RSABase.Config,
    digest: CryptographyAlgorithmId<Digest>,
) : RSABase<RSA.PKCS1.PublicKey, RSA.PKCS1.PrivateKey, RSA.PKCS1.KeyPair>(
    config = config,
    digest = digest,
) {

    override val type: Algorithm
        get() = when (digest) {
            SHA256 -> Algorithm.RS256
            SHA384 -> Algorithm.RS384
            SHA512 -> Algorithm.RS512
            else -> throw IllegalArgumentException("Unsupported digest algorithm")
        }

    override val alg: RSA<RSA.PKCS1.PublicKey, RSA.PKCS1.PrivateKey, RSA.PKCS1.KeyPair> = provider.get(RSA.PKCS1)

    override suspend fun sign(headerEncoded: String, claimsEncoded: String): ByteArray {
        val privateKey: RSA.PKCS1.PrivateKey =
            alg.privateKeyDecoder(digest).decodeFrom(checkNotNull(config.format), checkNotNull(config.privateKey))

        return privateKey.signatureGenerator().generateSignature("$headerEncoded.$claimsEncoded".encodeToByteArray())
    }
}

class RS256(config: Config) : PKCS1(digest = SHA256, config = config)

class RS384(config: Config) : PKCS1(digest = SHA384, config = config)

class RS512(config: Config) : PKCS1(digest = SHA512, config = config)
