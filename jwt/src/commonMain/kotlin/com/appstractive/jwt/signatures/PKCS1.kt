package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier

fun SignatureBuilder.rs256(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    type = Algorithm.RS256
    algorithm = PKCS1Signer(config = config)
}

fun SignatureBuilder.rs384(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    type = Algorithm.RS384
    algorithm = PKCS1Signer(config = config)
}

fun SignatureBuilder.rs512(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    type = Algorithm.RS512
    algorithm = PKCS1Signer(config = config)
}

fun VerificationBuilder.rs256(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    PKCS1Verifier(config).also {
        algorithms[Algorithm.RS256] = it
    }
}

fun VerificationBuilder.rs384(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    PKCS1Verifier(config).also {
        algorithms[Algorithm.RS384] = it
    }
}

fun VerificationBuilder.rs512(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    PKCS1Verifier(config).also {
        algorithms[Algorithm.RS512] = it
    }
}

internal class PKCS1Signer(
    private val config: RSASigningConfig,
) : SigningAlgorithm {
    override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
        return pkcs1.privateKeyDecoder(digest)
            .decodeFrom(checkNotNull(config.format), checkNotNull(config.privateKey)).signatureGenerator()
    }
}

internal class PKCS1Verifier(
    private val config: RSAVerifierConfig,
) : VerificationAlgorithm {
    override suspend fun verifier(jwt: JWT): SignatureVerifier {
        return pkcs1.publicKeyDecoder(digest = jwt.header.alg.digest)
            .decodeFrom(checkNotNull(config.format), checkNotNull(config.publicKey)).signatureVerifier()
    }
}
