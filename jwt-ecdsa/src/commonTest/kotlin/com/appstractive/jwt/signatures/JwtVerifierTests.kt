package com.appstractive.jwt.signatures

import com.appstractive.jwt.Algorithm
import com.appstractive.jwt.JWT
import com.appstractive.jwt.Signer
import com.appstractive.jwt.Verifier
import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.verify
import dev.whyoleg.cryptography.algorithms.EC
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class JwtVerifierTests {

  @Test
  fun createJwtES256() = runTest {
    val jwt =
        createJwtEC(
            builder = { es256 { pem(it) } },
            verifier = { es256 { pem(it) } },
        )

    assertEquals(Algorithm.ES256, jwt.header.alg)
  }

  @Test
  fun createJwtES384() = runTest {
    val jwt =
        createJwtEC(
            builder = { es384 { pem(it) } },
            verifier = { es384 { pem(it) } },
        )

    assertEquals(Algorithm.ES384, jwt.header.alg)
  }

  @Test
  fun createJwtES512() = runTest {
    val jwt =
        createJwtEC(
            builder = { es512 { pem(it) } },
            verifier = { es512 { pem(it) } },
        )

    assertEquals(Algorithm.ES512, jwt.header.alg)
  }

  private suspend fun createJwtEC(
    curve: EC.Curve = EC.Curve.P256,
    builder: Signer.(ByteArray) -> Unit,
    verifier: Verifier.(ByteArray) -> Unit,
  ): JWT {
    val unsignedJwt = jwt { claims { issuer = "someone" } }
    val keys = ecdsa.keyPairGenerator(curve).generateKey()
    val privateKey = keys.privateKey.encodeToByteArray(EC.PrivateKey.Format.PEM)
    val publicKey = keys.publicKey.encodeToByteArray(EC.PublicKey.Format.PEM)
    println(privateKey.decodeToString())
    println(publicKey.decodeToString())

    val signedJwt = unsignedJwt.sign { builder(privateKey) }

    println(signedJwt.toString())

    assertTrue(signedJwt.verify { verifier(publicKey) })

    val wrongKey =
        ecdsa.keyPairGenerator(curve).generateKey().publicKey.encodeToByteArray(EC.PublicKey.Format.PEM)
    assertFalse(signedJwt.verify { verifier(wrongKey) })

    return signedJwt
  }
}
