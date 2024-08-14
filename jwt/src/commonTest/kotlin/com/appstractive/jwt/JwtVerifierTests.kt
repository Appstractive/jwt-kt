package com.appstractive.jwt

import com.appstractive.jwt.signatures.hs256
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class JwtVerifierTests {

    @Test
    fun testVerifyAudSuccess() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            audience = "api.example.com"
        }

        assertTrue(result)
    }

    @Test
    fun testVerifyAudFail() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            audience = "example.com"
        }

        assertFalse(result)
    }

    @Test
    fun testVerifyIssSuccess() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            issuer = "example.com"
        }

        assertTrue(result)
    }

    @Test
    fun testVerifyIssFail() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            issuer = "example2.com"
        }

        assertFalse(result)
    }

    @Test
    fun testVerifyExpSuccess() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            expiresAt()
        }

        assertTrue(result)
    }

    @Test
    fun testVerifyExpFail() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            expiresAt(now = Clock.System.now() + 25.hours)
        }

        assertFalse(result)
    }

    @Test
    fun testVerifyNbfSuccess() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            notBefore()
        }

        assertTrue(result)
    }

    @Test
    fun testVerifyNbfFail() = runTest {
        val jwt = signedJwt()

        val result = jwt.verify {
            hs256 {
                secret = hmacSecret
            }

            notBefore(now = Clock.System.now() - 1.hours)
        }

        assertFalse(result)
    }

}