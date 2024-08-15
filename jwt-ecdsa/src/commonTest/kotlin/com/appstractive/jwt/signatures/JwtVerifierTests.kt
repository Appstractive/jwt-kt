package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class JwtVerifierTests {

  @Test
  fun createJwtEC256() = runTest {
    val jwt =
        createJwtEC(
            builder = { ec256 { pem(it) } },
            verifier = { ec256 { pem(it) } },
        )

    assertEquals(Algorithm.EC256, jwt.header.alg)
  }

  @Test
  fun createJwtEC384() = runTest {
    val jwt =
        createJwtEC(
            builder = { ec384 { pem(it) } },
            verifier = { ec384 { pem(it) } },
        )

    assertEquals(Algorithm.EC384, jwt.header.alg)
  }

  @Test
  fun createJwtEC512() = runTest {
    val jwt =
        createJwtEC(
            builder = { ec512 { pem(it) } },
            verifier = { ec512 { pem(it) } },
        )

    assertEquals(Algorithm.EC512, jwt.header.alg)
  }

  private suspend fun createJwtEC(
      curve: EC.Curve = EC.Curve.P256,
      builder: Signer.(ByteArray) -> Unit,
      verifier: Verifier.(ByteArray) -> Unit,
  ): JWT {
    val unsignedJwt = jwt { claims { issuer = "someone" } }
    val keys = ecdsa.keyPairGenerator(curve).generateKey()
    val privateKey = keys.privateKey.encodeTo(EC.PrivateKey.Format.PEM)
    val publicKey = keys.publicKey.encodeTo(EC.PublicKey.Format.PEM)
    println(privateKey.decodeToString())
    println(publicKey.decodeToString())

    val signedJwt = unsignedJwt.sign { builder(privateKey) }

    println(signedJwt.toString())

    assertTrue(signedJwt.verify { verifier(publicKey) })

    val wrongKey =
        ecdsa.keyPairGenerator(curve).generateKey().publicKey.encodeTo(EC.PublicKey.Format.PEM)
    assertFalse(signedJwt.verify { verifier(wrongKey) })

    return signedJwt
  }
}
