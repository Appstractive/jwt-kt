package com.appstractive.jwt

import com.appstractive.jwt.utils.claim
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.operations.SignFunction
import dev.whyoleg.cryptography.operations.SignatureGenerator
import dev.whyoleg.cryptography.operations.SignatureVerifier
import dev.whyoleg.cryptography.operations.VerifyFunction
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.io.RawSource
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@OptIn(CryptographyProviderApi::class)
object MockSignerVerifier : VerificationAlgorithm, SigningAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return object : SignatureVerifier {
      override fun createVerifyFunction(): VerifyFunction {
        return object : VerifyFunction {
          override fun tryVerify(signature: ByteArray, startIndex: Int, endIndex: Int): Boolean {
            return true
          }

          override fun close() = Unit

          override fun reset() = Unit

          override fun update(source: ByteArray, startIndex: Int, endIndex: Int) = Unit

          override fun verify(signature: ByteArray, startIndex: Int, endIndex: Int) = Unit
        }
      }
    }
  }

  override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
    return object : SignatureGenerator {
      override fun createSignFunction(): SignFunction {
        return object : SignFunction {
          override fun signIntoByteArray(destination: ByteArray, destinationOffset: Int): Int {
            return 0
          }

          override fun close() = Unit

          override fun reset() = Unit

          override fun signToByteArray(): ByteArray {
            return "123456".encodeToByteArray()
          }

          override fun update(source: ByteArray, startIndex: Int, endIndex: Int) = Unit
        }
      }
    }
  }
}

@OptIn(CryptographyProviderApi::class)
open class MockHashingVerifier(val referenceHash: ByteArray, val hashAlg: CryptographyAlgorithmId<Digest>) : VerificationAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return object : SignatureVerifier {
      override fun createVerifyFunction(): VerifyFunction {
        return object : VerifyFunction {
          override fun tryVerify(signature: ByteArray, startIndex: Int, endIndex: Int): Boolean {
            return false
          }

          override fun close() = Unit

          override fun reset() = Unit

          override fun update(source: ByteArray, startIndex: Int, endIndex: Int) = Unit

          override fun verify(signature: ByteArray, startIndex: Int, endIndex: Int) = Unit
        }
      }

      override suspend fun verifySignature(data: ByteArray, signature: ByteArray) {
        check(commonVerify())
      }

      override suspend fun verifySignature(data: ByteString, signature: ByteString) {
        check(commonVerify())
      }

      override suspend fun verifySignature(data: RawSource, signature: ByteString) {
        check(commonVerify())
      }

      override suspend fun tryVerifySignature(data: ByteArray, signature: ByteArray): Boolean {
        return commonVerify()
      }

      override suspend fun tryVerifySignature(data: ByteString, signature: ByteString): Boolean {
        return commonVerify()
      }

      override suspend fun tryVerifySignature(data: RawSource, signature: ByteString): Boolean {
        return commonVerify()
      }

      private suspend fun commonVerify(): Boolean {
        val hash = CryptographyProvider.Default.get(hashAlg).hasher().hash(jwt.signedData.encodeToByteArray())
        return referenceHash contentEquals hash
      }
    }
  }
}

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

@OptIn(ExperimentalSerializationApi::class)
fun unsignedJwt(): UnsignedJWT = jwt {
  claims {
    issuer = "example.com"
    subject = "me"
    audience = "api.example.com"
    expires(at = Clock.System.now() + 24.hours)
    notBefore()
    issuedAt()
    id = "123456"

    claim("double", 1.1)
    claim("long", 1L)
    claim("bool", true)
    claim("string", "test")
    claim("null", null)
    objectClaim("object") { put("key", JsonPrimitive("value")) }
    arrayClaim("list") {
      add(JsonPrimitive(0))
      add(JsonPrimitive(1))
      add(JsonPrimitive(2))
    }

    claim(
        key = "complex",
        value =
            TestClass(
                long = 123443543642353L,
                int = 1,
                double = 124321412.325325,
                float = 1.2414f,
                bool = true,
                string = "test",
                enum = TestEnum.VALUE1,
                list =
                    listOf(
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

suspend fun signedJwt(
  builder: Signer.() -> Unit,
): JWT {
  return unsignedJwt().sign(builder = builder)
}
