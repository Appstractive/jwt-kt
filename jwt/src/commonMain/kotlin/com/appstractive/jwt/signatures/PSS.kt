package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier

fun SignatureBuilder.ps256(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    type = Algorithm.PS256
    algorithm = PSSSigner(config = config)
}

fun SignatureBuilder.ps384(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    type = Algorithm.PS384
    algorithm = PSSSigner(config)
}

fun SignatureBuilder.ps512(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    type = Algorithm.PS512
    algorithm = PSSSigner(config)
}

fun VerificationBuilder.ps256(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    PSSVerifier(config = config).also {
        algorithms[Algorithm.PS256] = it
    }
}

fun VerificationBuilder.ps384(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    PSSVerifier(config).also {
        algorithms[Algorithm.PS384] = it
    }
}

fun VerificationBuilder.ps512(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    PSSVerifier(config).also {
        algorithms[Algorithm.PS512] = it
    }
}

internal class PSSSigner(
    private val config: RSASigningConfig,
) : SigningAlgorithm {
    override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
        return pss.privateKeyDecoder(digest).decodeFrom(checkNotNull(config.format), checkNotNull(config.privateKey))
            .signatureGenerator()
    }
}

internal class PSSVerifier(
    private val config: RSAVerifierConfig,
) : VerificationAlgorithm {
    override suspend fun verifier(jwt: JWT): SignatureVerifier {
        return pss.publicKeyDecoder(digest = jwt.header.alg.digest)
            .decodeFrom(checkNotNull(config.format), checkNotNull(config.publicKey)).signatureVerifier()
    }
}