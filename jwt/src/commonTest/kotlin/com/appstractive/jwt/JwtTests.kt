package com.appstractive.jwt

import com.appstractive.jwt.signatures.*
import com.appstractive.jwt.utils.claim
import com.appstractive.jwt.utils.urlEncoded
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
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC
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

class JwtTests {

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
        createJwtHmac {
            hs256 {
                secret = it
            }
        }
    }

    @Test
    fun createJwtHS384() = runTest {
        createJwtHmac {
            hs384 {
                secret = it
            }
        }
    }

    @Test
    fun createJwtHS512() = runTest {
        createJwtHmac {
            hs512 {
                secret = it
            }
        }
    }

    private suspend fun createJwtHmac(builder: SignatureBuilder.(ByteArray) -> Unit): JWT {
        val secret = "your-256-bit-secret".encodeToByteArray()
        val unsignedJwt = unsignedJwt()

        val signedJwt = unsignedJwt.sign {
            builder(secret)
        }

        println(signedJwt.toString())

        val hmac = provider.get(HMAC)
        val key = hmac.keyDecoder(signedJwt.header.alg.digest)
            .decodeFrom(HMAC.Key.Format.RAW, secret)
        key.signatureVerifier().verifySignature(signedJwt.urlEncoded().encodeToByteArray(), signedJwt.signature)

        return signedJwt
    }

    @Test
    fun createJwtRS256() = runTest {
        val jwt = createJwtRS(SHA256) {
            rs256 {
                pem(it)
            }
        }

        assertEquals(Algorithm.RS256, jwt.header.alg)
    }

    @Test
    fun createJwtR384() = runTest {
        val jwt = createJwtRS(SHA384) {
            rs384 {
                pem(it)
            }
        }

        assertEquals(Algorithm.RS384, jwt.header.alg)
    }

    @Test
    fun createJwtR512() = runTest {
        val jwt = createJwtRS(SHA512) {
            rs512 {
                pem(it)
            }
        }

        assertEquals(Algorithm.RS512, jwt.header.alg)
    }

    @Test
    fun createJwtPS256() = runTest {
        val jwt = createJwtPS(SHA256) {
            ps256 {
                pem(it)
            }
        }
        assertEquals(Algorithm.PS256, jwt.header.alg)
    }

    @Test
    fun createJwtP384() = runTest {
        val jwt = createJwtPS(SHA384) {
            ps384 {
                pem(it)
            }
        }

        assertEquals(Algorithm.PS384, jwt.header.alg)
    }

    @Test
    fun createJwtP512() = runTest {
        val jwt = createJwtPS(SHA512) {
            ps512 {
                pem(it)
            }
        }

        assertEquals(Algorithm.PS512, jwt.header.alg)
    }

    private suspend fun createJwtRS(digest: CryptographyAlgorithmId<Digest>, builder: SignatureBuilder.(ByteArray) -> Unit): JWT {
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
            keys.publicKey.signatureVerifier()
                .verifySignature(signedJwt.urlEncoded().encodeToByteArray(), signedJwt.signature)
        )

        return signedJwt
    }

    private suspend fun createJwtPS(digest: CryptographyAlgorithmId<Digest>, builder: SignatureBuilder.(ByteArray) -> Unit): JWT {
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
            keys.publicKey.signatureVerifier()
                .verifySignature(signedJwt.urlEncoded().encodeToByteArray(), signedJwt.signature)
        )

        return signedJwt
    }

    @Test
    fun createJwtEC256() = runTest {
        val jwt = createJwtEC(SHA256) {
            ec256 {
                pem(it)
            }
        }

        assertEquals(Algorithm.EC256, jwt.header.alg)
    }

    @Test
    fun createJwtEC384() = runTest {
        val jwt = createJwtEC(SHA384) {
            ec384 {
                pem(it)
            }
        }

        assertEquals(Algorithm.EC384, jwt.header.alg)
    }

    @Test
    fun createJwtEC512() = runTest {
        val jwt = createJwtEC(SHA512) {
            ec512 {
                pem(it)
            }
        }

        assertEquals(Algorithm.EC512, jwt.header.alg)
    }

    private suspend fun createJwtEC(digest: CryptographyAlgorithmId<Digest>, curve: EC.Curve = EC.Curve.P256, builder: SignatureBuilder.(ByteArray) -> Unit): JWT {
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
            keys.publicKey.signatureVerifier(digest)
                .verifySignature(signedJwt.urlEncoded().encodeToByteArray(), signedJwt.signature)
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