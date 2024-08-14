package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier

private val provider by lazy { CryptographyProvider.Default }
internal val pss: RSA.PSS by lazy { provider.get(RSA.PSS) }

fun SignatureBuilder.ps256(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    algorithm(
        algorithm = PSSSigner(config = config),
        type = Algorithm.PS256,
    )
}

fun SignatureBuilder.ps384(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    algorithm(
        algorithm = PSSSigner(config = config),
        type = Algorithm.PS384,
    )
}

fun SignatureBuilder.ps512(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    algorithm(
        algorithm = PSSSigner(config = config),
        type = Algorithm.PS512,
    )
}

fun VerificationBuilder.ps256(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    verifier(
        type = Algorithm.PS256,
        algorithm = PSSVerifier(config = config),
    )
}

fun VerificationBuilder.ps384(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    verifier(
        type = Algorithm.PS384,
        algorithm = PSSVerifier(config = config),
    )
}

fun VerificationBuilder.ps512(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    verifier(
        type = Algorithm.PS512,
        algorithm = PSSVerifier(config = config),
    )
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