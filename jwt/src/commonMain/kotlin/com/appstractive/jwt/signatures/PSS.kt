package com.appstractive.jwt.signatures

import com.appstractive.jwt.Algorithm
import com.appstractive.jwt.SignatureBuilder
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.digest.SHA384
import dev.whyoleg.cryptography.algorithms.digest.SHA512

fun SignatureBuilder.ps256(configure: RSABase.Config.() -> Unit) {
    val config = RSABase.Config().apply(configure)
    algorithm = PS256(config)
}

fun SignatureBuilder.ps384(configure: RSABase.Config.() -> Unit) {
    val config = RSABase.Config().apply(configure)
    algorithm = PS384(config)
}

fun SignatureBuilder.ps512(configure: RSABase.Config.() -> Unit) {
    val config = RSABase.Config().apply(configure)
    algorithm = PS512(config)
}

abstract class PSS(
    digest: CryptographyAlgorithmId<Digest>,
    config: RSABase.Config,
) : RSABase<RSA.PSS.PublicKey, RSA.PSS.PrivateKey, RSA.PSS.KeyPair>(
    digest = digest,
    config = config,
) {
    override val type: Algorithm
        get() = when (digest) {
            SHA256 -> Algorithm.PS256
            SHA384 -> Algorithm.PS384
            SHA512 -> Algorithm.PS512
            else -> throw IllegalArgumentException("Unsupported digest algorithm")
        }

    override val alg: RSA.PSS = provider.get(RSA.PSS)

    override suspend fun sign(headerEncoded: String, claimsEncoded: String): ByteArray {
        val privateKey: RSA.PSS.PrivateKey =
            alg.privateKeyDecoder(digest).decodeFrom(checkNotNull(config.format), checkNotNull(config.privateKey))

        return privateKey.signatureGenerator().generateSignature("$headerEncoded.$claimsEncoded".encodeToByteArray())
    }
}

class PS256(config: Config) : PSS(digest = SHA256, config = config)

class PS384(config: Config) : PSS(digest = SHA384, config = config)

class PS512(config: Config) : PSS(digest = SHA512, config = config)