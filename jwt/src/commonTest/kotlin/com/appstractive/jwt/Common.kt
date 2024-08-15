package com.appstractive.jwt

import com.appstractive.jwt.utils.claim
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import dev.whyoleg.cryptography.operations.signature.SignatureVerifier
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@OptIn(CryptographyProviderApi::class)
object MockSignerVerifier : VerificationAlgorithm, SigningAlgorithm {
  override suspend fun verifier(jwt: JWT): SignatureVerifier {
    return object : SignatureVerifier {
      override fun verifySignatureBlocking(
          dataInput: ByteArray,
          signatureInput: ByteArray
      ): Boolean {
        return true
      }
    }
  }

  override suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator {
    return object : SignatureGenerator {
      override fun generateSignatureBlocking(dataInput: ByteArray): ByteArray {
        return "123456".encodeToByteArray()
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
    expiresAt = Clock.System.now() + 24.hours
    notBeforeNow()
    issuedNow()
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
