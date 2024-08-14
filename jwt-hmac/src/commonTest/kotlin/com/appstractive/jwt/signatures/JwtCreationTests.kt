package com.appstractive.jwt.signatures

import com.appstractive.jwt.*
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64.Default.UrlSafe
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JwtCreationTests {

    @Test
    fun createJwtHS256() = runTest {
        val jwt = createJwtHmac(
            builder = {
                hs256 {
                    secret = it
                }
            },
            verifier = {
                hs256 {
                    secret = it
                }
            }
        )

        assertEquals(Algorithm.HS256, jwt.header.alg)
    }

    @Test
    fun createJwtHS384() = runTest {
        val jwt = createJwtHmac(
            secret = CryptographyRandom.nextBytes(128),
            builder = {
                hs384 {
                    secret = it
                }
            },
            verifier = {
                hs384 {
                    secret = it
                }
            }
        )

        assertEquals(Algorithm.HS384, jwt.header.alg)
    }

    @Test
    fun createJwtHS512() = runTest {
        val jwt = createJwtHmac(
            secret = CryptographyRandom.nextBytes(128),
            builder = {
                hs512 {
                    secret = it
                }
            },
            verifier = {
                hs512 {
                    secret = it
                }
            }
        )

        assertEquals(Algorithm.HS512, jwt.header.alg)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun createJwtHmac(
        secret: ByteArray = CryptographyRandom.nextBytes(64),
        builder: SignatureBuilder.(ByteArray) -> Unit,
        verifier: VerificationBuilder.(ByteArray) -> Unit,
    ): JWT {
        val unsignedJwt = jwt {
            claims {
                issuer = "someone"
            }
        }

        val signedJwt = unsignedJwt.sign {
            builder(secret)
        }

        println(UrlSafe.encode(secret).replace("=", ""))
        println(signedJwt.toString())

        assertTrue(signedJwt.verify { verifier(secret) })

        return signedJwt
    }
}