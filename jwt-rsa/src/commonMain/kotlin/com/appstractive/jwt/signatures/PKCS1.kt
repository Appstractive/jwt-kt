package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier

private val provider by lazy { CryptographyProvider.Default }
internal val pkcs1: RSA<RSA.PKCS1.PublicKey, RSA.PKCS1.PrivateKey, RSA.PKCS1.KeyPair> by lazy { provider.get(RSA.PKCS1) }

fun SignatureBuilder.rs256(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    algorithm(
        algorithm = PKCS1Signer(config = config),
        type = Algorithm.RS256,
    )
}

fun SignatureBuilder.rs384(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    algorithm(
        algorithm = PKCS1Signer(config = config),
        type = Algorithm.RS384,
    )
}

fun SignatureBuilder.rs512(configure: RSASigningConfig.() -> Unit) {
    val config = RSASigningConfig().apply(configure)
    algorithm(
        algorithm = PKCS1Signer(config = config),
        type = Algorithm.RS512,
    )
}

fun VerificationBuilder.rs256(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    verifier(
        type = Algorithm.RS256,
        algorithm = PKCS1Verifier(config = config),
    )
}

fun VerificationBuilder.rs384(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    verifier(
        type = Algorithm.RS384,
        algorithm = PKCS1Verifier(config = config),
    )
}

fun VerificationBuilder.rs512(configure: RSAVerifierConfig.() -> Unit) {
    val config = RSAVerifierConfig().apply(configure)
    verifier(
        type = Algorithm.RS512,
        algorithm = PKCS1Verifier(config = config),
    )
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
