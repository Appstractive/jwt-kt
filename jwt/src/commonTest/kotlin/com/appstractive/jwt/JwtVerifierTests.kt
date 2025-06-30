package com.appstractive.jwt

import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyProvider
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock

class JwtVerifierTests {

  @Test
  fun testVerifyAudSuccess() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          audience("api.example.com", "api.example2.com")
        }

    assertTrue(result)
  }

  @Test
  fun testVerifyAudFail() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          audience("example.com", "example2.com")
        }

    assertFalse(result)
  }

  @Test
  fun testVerifyIssSuccess() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          issuer("example.com", "example2.com")
        }

    assertTrue(result)
  }

  @Test
  fun testVerifyIssFail() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          issuer("example2.com", "example3.com")
        }

    assertFalse(result)
  }

  @Test
  fun testVerifyExpSuccess() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          expiresAt()
        }

    assertTrue(result)
  }

  @Test
  fun testVerifyExpFail() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          expiresAt(now = Clock.System.now() + 25.hours)
        }

    assertFalse(result)
  }

  @Test
  fun testVerifyNbfSuccess() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          notBefore()
        }

    assertTrue(result)
  }

  @Test
  fun testVerifyNbfFail() = runTest {
    val jwt = signedJwt {
      algorithm(
          type = Algorithm.HS256,
          algorithm = MockSignerVerifier,
      )
    }

    val result =
        jwt.verify {
          algorithm(
              type = Algorithm.HS256,
              algorithm = MockSignerVerifier,
          )

          notBefore(now = Clock.System.now() - 1.hours)
        }

    assertFalse(result)
  }

  @Test
  fun testVerifyDifferentHeaderOrderSuccess() = runTest {
    val signedHeaderNormal = urlEncoded("""{"alg":"ES256","typ":"JWT"}""")
    assertTrue { checkSignatureHash(signedHeaderNormal, Algorithm.ES256) }
    val signedHeaderReverse = urlEncoded("""{"typ":"JWT","alg":"ES256"}""")
    assertTrue { checkSignatureHash(signedHeaderReverse, Algorithm.ES256) }
  }

  private suspend fun checkSignatureHash(signedHeader: String, sigAlg: Algorithm): Boolean {
    val signedBody = urlEncoded("{}")
    val signedPart = "$signedHeader.$signedBody"
    val signaturePart = urlEncoded("don't care")
    val expectedHash = CryptographyProvider.Default.get(sigAlg.digest).hasher().hash(signedPart.encodeToByteArray())
    val jwt = JWT.from("$signedPart.$signaturePart")

    return jwt.verify {
      this.algorithm(
          type = sigAlg,
          algorithm = MockHashingVerifier(expectedHash, sigAlg.digest),
      )
    }
  }
}
