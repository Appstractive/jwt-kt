package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.digest.SHA384
import dev.whyoleg.cryptography.algorithms.digest.SHA512
import kotlinx.serialization.json.JsonObject

fun SignatureBuilder.ec256(configure: ECDSABase.Config.() -> Unit) {
    val config = ECDSABase.Config().apply(configure)
    algorithm = EC256(config)
}

fun SignatureBuilder.ec384(configure: ECDSABase.Config.() -> Unit) {
    val config = ECDSABase.Config().apply(configure)
    algorithm = EC384(config)
}

fun SignatureBuilder.ec512(configure: ECDSABase.Config.() -> Unit) {
    val config = ECDSABase.Config().apply(configure)
    algorithm = EC512(config)
}

abstract class ECDSABase(
    private val digest: CryptographyAlgorithmId<Digest>,
    private val config: ECDSABase.Config,
) : SigningAlgorithm<ECDSABase.Config> {

    private val provider = CryptographyProvider.Default
    private val alg: ECDSA = provider.get(ECDSA)

    override val type: Algorithm
        get() = when (digest) {
            SHA256 -> Algorithm.EC256
            SHA384 -> Algorithm.EC384
            SHA512 -> Algorithm.EC512
            else -> throw IllegalArgumentException("Unsupported digest algorithm")
        }

    override suspend fun sign(header: Header, claims: JsonObject): ByteArray {
        val headerEncoded = header.urlEncoded()
        val claimsEncoded = claims.urlEncoded()

        val privateKey: ECDSA.PrivateKey =
            alg.privateKeyDecoder(config.curve).decodeFrom(checkNotNull(config.format), checkNotNull(config.privateKey))

        return privateKey.signatureGenerator(digest)
            .generateSignature("$headerEncoded.$claimsEncoded".encodeToByteArray())
    }

    class Config : SigningAlgorithmConfig {
        internal var privateKey: ByteArray? = null
        internal var format: EC.PrivateKey.Format? = null
        internal var curve: EC.Curve = EC.Curve.P256

        fun pem(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
            privateKey = key
            format = EC.PrivateKey.Format.PEM
            this.curve = curve
        }

        fun pem(key: String, curve: EC.Curve = EC.Curve.P256) {
            privateKey = key.encodeToByteArray()
            format = EC.PrivateKey.Format.PEM
            this.curve = curve
        }

        fun der(key: ByteArray, curve: EC.Curve = EC.Curve.P256) {
            privateKey = key
            format = EC.PrivateKey.Format.DER
            this.curve = curve
        }

        fun der(key: String, curve: EC.Curve = EC.Curve.P256) {
            privateKey = key.encodeToByteArray()
            format = EC.PrivateKey.Format.DER
            this.curve = curve
        }
    }
}

class EC256(config: Config) : ECDSABase(digest = SHA256, config = config)

class EC384(config: Config) : ECDSABase(digest = SHA384, config = config)

class EC512(config: Config) : ECDSABase(digest = SHA512, config = config)