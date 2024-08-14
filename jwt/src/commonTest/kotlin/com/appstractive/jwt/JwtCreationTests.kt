package com.appstractive.jwt

import com.appstractive.jwt.signatures.*
import com.appstractive.jwt.utils.claim
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA.PKCS1
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.digest.SHA384
import dev.whyoleg.cryptography.algorithms.digest.SHA512
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private enum class TestEnum {
    VALUE1,
    VALUE2,
    VALUE3,
}

@Serializable
private data class TestClass(
    val long: Long,
    val int: Int,
    val double: Double,
    val float: Float,
    val bool: Boolean,
    val string: String,
    val enum: TestEnum,
    val list: List<TestClass>,
    val nullable: Nothing? = null,
)

class JwtCreationTests {

    @OptIn(ExperimentalSerializationApi::class)
    private fun unsignedJwt(): UnsignedJWT = jwt {
        header {
            keyId = "123456"
        }

        claims {
            issuer = "example.com"
            subject = "me"
            audience = "api.example.com"
            expiresAt = Clock.System.now() + 24.hours
            notBeforeNow()
            issuedNow()
            id = "123456"

            claim("double", 1.1)
            claim("long", 1L)
            claim("bool", true)
            claim("string", "test")
            claim("null", null)
            objectClaim("object") {
                put("key", JsonPrimitive("value"))
            }
            arrayClaim("list") {
                add(JsonPrimitive(0))
                add(JsonPrimitive(1))
                add(JsonPrimitive(2))
            }

            claim(
                key = "complex",
                value = TestClass(
                    long = 123443543642353L,
                    int = 1,
                    double = 124321412.325325,
                    float = 1.2414f,
                    bool = true,
                    string = "test",
                    enum = TestEnum.VALUE1,
                    list = listOf(
                        TestClass(
                            long = 123443543642353L,
                            int = 1,
                            double = 124321412.325325,
                            float = 1.2414f,
                            bool = false,
                            string = "test2",
                            enum = TestEnum.VALUE2,
                            list = emptyList(),
                        ),
                    ),
                ),
            )
        }
    }


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

    private suspend fun createJwtHmac(
        builder: SignatureBuilder.(ByteArray) -> Unit,
        verifier: VerificationBuilder.(ByteArray) -> Unit,
    ): JWT {
        val secret = "your-256-bit-secret".encodeToByteArray()
        val unsignedJwt = unsignedJwt()

        val signedJwt = unsignedJwt.sign {
            builder(secret)
        }

        println(signedJwt.toString())

        assertTrue(signedJwt.verify { verifier(secret) })

        return signedJwt
    }

    @Test
    fun createJwtRS256() = runTest {
        val jwt = createJwtRS(
            SHA256,
            builder = {
                rs256 {
                    pem(it)
                }
            },
            verifier = {
                rs256 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.RS256, jwt.header.alg)
    }

    @Test
    fun createJwtR384() = runTest {
        val jwt = createJwtRS(
            SHA384,
            builder = {
                rs384 {
                    pem(it)
                }
            },
            verifier = {
                rs384 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.RS384, jwt.header.alg)
    }

    @Test
    fun createJwtR512() = runTest {
        val jwt = createJwtRS(
            SHA512,
            builder = {
                rs512 {
                    pem(it)
                }
            },
            verifier = {
                rs512 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.RS512, jwt.header.alg)
    }

    @Test
    fun createJwtPS256() = runTest {
        val jwt = createJwtPS(
            SHA256,
            builder = {
                ps256 {
                    pem(it)
                }
            },
            verifier = {
                ps256 {
                    pem(it)
                }
            },
        )
        assertEquals(Algorithm.PS256, jwt.header.alg)
    }

    @Test
    fun createJwtP384() = runTest {
        val jwt = createJwtPS(
            SHA384,
            builder = {
                ps384 {
                    pem(it)
                }
            },
            verifier = {
                ps384 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.PS384, jwt.header.alg)
    }

    @Test
    fun createJwtP512() = runTest {
        val jwt = createJwtPS(
            SHA512,
            builder = {
                ps512 {
                    pem(it)
                }
            },
            verifier = {
                ps512 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.PS512, jwt.header.alg)
    }

    private suspend fun createJwtRS(
        digest: CryptographyAlgorithmId<Digest>,
        builder: SignatureBuilder.(ByteArray) -> Unit,
        verifier: VerificationBuilder.(ByteArray) -> Unit,
    ): JWT {
        val unsignedJwt = unsignedJwt()
        val keys = PKCS1.keyPairGenerator(digest = digest).generateKey()
        val privateKey = keys.privateKey.encodeTo(RSA.PrivateKey.Format.PEM.Generic)
        val publicKey = keys.publicKey.encodeTo(RSA.PublicKey.Format.PEM.Generic)
        println(privateKey.decodeToString())
        println(publicKey.decodeToString())

        val signedJwt = unsignedJwt.sign {
            builder(privateKey)
        }

        println(signedJwt.toString())

        assertTrue(
            signedJwt.verify { verifier(publicKey) }
        )

        return signedJwt
    }

    private suspend fun createJwtPS(
        digest: CryptographyAlgorithmId<Digest>,
        builder: SignatureBuilder.(ByteArray) -> Unit,
        verifier: VerificationBuilder.(ByteArray) -> Unit,
    ): JWT {
        val unsignedJwt = unsignedJwt()
        val keys = PSS.keyPairGenerator(digest = digest).generateKey()
        val privateKey = keys.privateKey.encodeTo(RSA.PrivateKey.Format.PEM.Generic)
        val publicKey = keys.publicKey.encodeTo(RSA.PublicKey.Format.PEM.Generic)
        println(privateKey.decodeToString())
        println(publicKey.decodeToString())

        val signedJwt = unsignedJwt.sign {
            builder(privateKey)
        }

        println(signedJwt.toString())

        assertTrue(
            signedJwt.verify { verifier(publicKey) }
        )

        return signedJwt
    }

    @Test
    fun createJwtEC256() = runTest {
        val jwt = createJwtEC(
            SHA256,
            builder = {
                ec256 {
                    pem(it)
                }
            },
            verifier = {
                ec256 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.EC256, jwt.header.alg)
    }

    @Test
    fun createJwtEC384() = runTest {
        val jwt = createJwtEC(
            SHA384,
            builder = {
                ec384 {
                    pem(it)
                }
            },
            verifier = {
                ec384 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.EC384, jwt.header.alg)
    }

    @Test
    fun createJwtEC512() = runTest {
        val jwt = createJwtEC(
            SHA512,
            builder = {
                ec512 {
                    pem(it)
                }
            },
            verifier = {
                ec512 {
                    pem(it)
                }
            },
        )

        assertEquals(Algorithm.EC512, jwt.header.alg)
    }

    private suspend fun createJwtEC(
        digest: CryptographyAlgorithmId<Digest>,
        curve: EC.Curve = EC.Curve.P256,
        builder: SignatureBuilder.(ByteArray) -> Unit,
        verifier: VerificationBuilder.(ByteArray) -> Unit,
    ): JWT {
        val unsignedJwt = unsignedJwt()
        val keys = ECD.keyPairGenerator(curve).generateKey()
        val privateKey = keys.privateKey.encodeTo(EC.PrivateKey.Format.PEM)
        val publicKey = keys.publicKey.encodeTo(EC.PublicKey.Format.PEM)
        println(privateKey.decodeToString())
        println(publicKey.decodeToString())

        val signedJwt = unsignedJwt.sign {
            builder(privateKey)
        }

        println(signedJwt.toString())

        assertTrue(
            signedJwt.verify { verifier(publicKey) }
        )

        return signedJwt
    }

    companion object {
        val provider = CryptographyProvider.Default
        val PKCS1: PKCS1 = provider.get(RSA.PKCS1)
        val PSS: RSA.PSS = provider.get(RSA.PSS)
        val ECD: ECDSA = provider.get(ECDSA)
    }
}