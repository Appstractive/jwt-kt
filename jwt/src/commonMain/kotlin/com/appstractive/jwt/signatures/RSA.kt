package com.appstractive.jwt.signatures

import com.appstractive.jwt.Header
import com.appstractive.jwt.SigningAlgorithm
import com.appstractive.jwt.SigningAlgorithmConfig
import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA.PrivateKey.Format
import dev.whyoleg.cryptography.algorithms.digest.Digest
import kotlinx.serialization.json.JsonObject

abstract class RSABase<PublicK : RSA.PublicKey, PrivateK : RSA.PrivateKey, KP : RSA.KeyPair<PublicK, PrivateK>>(
    protected val digest: CryptographyAlgorithmId<Digest>,
    protected val config: RSABase.Config,
) : SigningAlgorithm<RSABase.Config> {

    protected val provider = CryptographyProvider.Default
    abstract val alg: RSA<PublicK, PrivateK, KP>

    override suspend fun sign(header: Header, claims: JsonObject): ByteArray {
        val headerEncoded = header.urlEncoded()
        val claimsEncoded = claims.urlEncoded()

        return sign(headerEncoded = headerEncoded, claimsEncoded = claimsEncoded)
    }

    abstract suspend fun sign(headerEncoded: String, claimsEncoded: String): ByteArray

    class Config : SigningAlgorithmConfig {
        internal var privateKey: ByteArray? = null
        internal var format: Format? = null

        fun pem(key: ByteArray) {
            privateKey = key
            format = Format.PEM.Generic
        }

        fun pem(key: String) {
            privateKey = key.encodeToByteArray()
            format = Format.PEM.Generic
        }

        fun der(key: ByteArray) {
            privateKey = key
            format = Format.DER.Generic
        }

        fun der(key: String) {
            privateKey = key.encodeToByteArray()
            format = Format.DER.Generic
        }
    }
}