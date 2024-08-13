package com.appstractive.jwt

import com.appstractive.jwt.utils.claim
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.test.Test
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

    @Test
    fun createJwt() = runTest {
        val jwt = jwt {
            header {

            }

            claims {
                issuer = "example.com"
                subject = "me"
                audience = "api.example.com"
                expiresAt = Clock.System.now() + 24.hours
                notBeforeNow()
                issuedNow()
                id = "123456"

                claim(
                    "complex",
                    TestClass(
                        long = 123443543642353L,
                        int = 1,
                        double = 124321412.325325,
                        float = 12414214.243f,
                        bool = true,
                        string = "test",
                        enum = TestEnum.VALUE1,
                        list = listOf(
                            TestClass(
                                long = 123443543642353L,
                                int = 1,
                                double = 124321412.325325,
                                float = 1.2414214E7f,
                                bool = false,
                                string = "test2",
                                enum = TestEnum.VALUE2,
                                list = emptyList(),
                            ),
                        ),
                    ),
                )
            }

            signature {
                hs256 {
                    secret = "your-256-bit-secret"
                }
            }
        }

        println(jwt.toString())
    }

}