package com.appstractive.jwt.signatures

import com.appstractive.jwt.Algorithm
import com.appstractive.jwt.JWT
import com.appstractive.jwt.Signer
import com.appstractive.jwt.Verifier
import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.verify
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.algorithms.SHA384
import dev.whyoleg.cryptography.algorithms.SHA512
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class JwtVerifierTests {
  @Test
  fun createJwtRS256() = runTest {
    val jwt =
        createJwtRS(
            SHA256,
            builder = { rs256 { pem(it) } },
            verifier = { rs256 { pem(it) } },
        )

    assertEquals(Algorithm.RS256, jwt.header.alg)
  }

  @Test
  fun createJwtR384() = runTest {
    val jwt =
        createJwtRS(
            SHA384,
            builder = { rs384 { pem(it) } },
            verifier = { rs384 { pem(it) } },
        )

    assertEquals(Algorithm.RS384, jwt.header.alg)
  }

  @Test
  fun createJwtR512() = runTest {
    val jwt =
        createJwtRS(
            SHA512,
            builder = { rs512 { pem(it) } },
            verifier = { rs512 { pem(it) } },
        )

    assertEquals(Algorithm.RS512, jwt.header.alg)
  }

  @Test
  fun createJwtPS256() = runTest {
    val jwt =
        createJwtPS(
            SHA256,
            builder = { ps256 { pem(it) } },
            verifier = { ps256 { pem(it) } },
        )
    assertEquals(Algorithm.PS256, jwt.header.alg)
  }

  @Test
  fun createJwtP384() = runTest {
    val jwt =
        createJwtPS(
            SHA384,
            builder = { ps384 { pem(it) } },
            verifier = { ps384 { pem(it) } },
        )

    assertEquals(Algorithm.PS384, jwt.header.alg)
  }

  @Test
  fun createJwtP512() = runTest {
    val jwt =
        createJwtPS(
            SHA512,
            builder = { ps512 { pem(it) } },
            verifier = { ps512 { pem(it) } },
        )

    assertEquals(Algorithm.PS512, jwt.header.alg)
  }

  private suspend fun createJwtRS(
    digest: CryptographyAlgorithmId<Digest>,
    builder: Signer.(ByteArray) -> Unit,
    verifier: Verifier.(ByteArray) -> Unit,
  ): JWT {
    val unsignedJwt = jwt { claims { issuer = "someone" } }
    val keys = pkcs1.keyPairGenerator(digest = digest).generateKey()
    val privateKey = keys.privateKey.encodeToByteArray(RSA.PrivateKey.Format.PEM.Generic)
    val publicKey = keys.publicKey.encodeToByteArray(RSA.PublicKey.Format.PEM.Generic)
    println(privateKey.decodeToString())
    println(publicKey.decodeToString())

    val signedJwt = unsignedJwt.sign { builder(privateKey) }

    println(signedJwt.toString())

    assertTrue(signedJwt.verify { verifier(publicKey) })

    return signedJwt
  }

  private suspend fun createJwtPS(
    digest: CryptographyAlgorithmId<Digest>,
    builder: Signer.(ByteArray) -> Unit,
    verifier: Verifier.(ByteArray) -> Unit,
  ): JWT {
    val unsignedJwt = jwt { claims { issuer = "someone" } }
    val keys = pss.keyPairGenerator(digest = digest).generateKey()
    val privateKey = keys.privateKey.encodeToByteArray(RSA.PrivateKey.Format.PEM.Generic)
    val publicKey = keys.publicKey.encodeToByteArray(RSA.PublicKey.Format.PEM.Generic)
    println(privateKey.decodeToString())
    println(publicKey.decodeToString())

    val signedJwt = unsignedJwt.sign { builder(privateKey) }

    println(signedJwt.toString())

    assertTrue(signedJwt.verify { verifier(publicKey) })

    val wrongKey =
        pss.keyPairGenerator(digest = digest)
            .generateKey()
            .publicKey
            .encodeToByteArray(RSA.PublicKey.Format.PEM.Generic)
    assertFalse(signedJwt.verify { verifier(wrongKey) })

    return signedJwt
  }
}
