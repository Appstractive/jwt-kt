package com.appstractive.jwt.signatures

import com.appstractive.jwt.Algorithm
import com.appstractive.jwt.JWT
import com.appstractive.jwt.Signer
import com.appstractive.jwt.Verifier
import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.verify
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlin.io.encoding.Base64.Default.UrlSafe
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class JwtVerifierTests {

  @Test
  fun createJwtHS256() = runTest {
    val jwt =
        createJwtHmac(
            builder = { hs256 { secret = it } },
            verifier = { hs256 { secret = it } },
        )

    assertEquals(Algorithm.HS256, jwt.header.alg)
  }

  @Test
  fun createJwtHS384() = runTest {
    val jwt =
        createJwtHmac(
            secret = CryptographyRandom.nextBytes(128),
            builder = { hs384 { secret = it } },
            verifier = { hs384 { secret = it } },
        )

    assertEquals(Algorithm.HS384, jwt.header.alg)
  }

  @Test
  fun createJwtHS512() = runTest {
    val jwt =
        createJwtHmac(
            secret = CryptographyRandom.nextBytes(128),
            builder = { hs512 { secret = it } },
            verifier = { hs512 { secret = it } },
        )

    assertEquals(Algorithm.HS512, jwt.header.alg)
  }

  @OptIn(ExperimentalEncodingApi::class)
  private suspend fun createJwtHmac(
    secret: ByteArray = CryptographyRandom.nextBytes(64),
    builder: Signer.(ByteArray) -> Unit,
    verifier: Verifier.(ByteArray) -> Unit,
  ): JWT {
    val unsignedJwt = jwt { claims { issuer = "someone" } }

    val signedJwt = unsignedJwt.sign { builder(secret) }

    println(UrlSafe.encode(secret).replace("=", ""))
    println(signedJwt.toString())

    assertTrue(signedJwt.verify { verifier(secret) })

    assertFalse(signedJwt.verify { verifier(CryptographyRandom.nextBytes(secret.size)) })

    return signedJwt
  }
}
