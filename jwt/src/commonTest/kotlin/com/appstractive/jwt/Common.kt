﻿package com.appstractive.jwt

import com.appstractive.jwt.signatures.hs256
import com.appstractive.jwt.utils.claim
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.hours

private enum class TestEnum {
    VALUE1,
    VALUE2,
    VALUE3,
}

val hmacSecret = "your-256-bit-secret".encodeToByteArray()

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

suspend fun signedJwt(
    builder: SignatureBuilder.() -> Unit = {
        hs256 {
            secret = hmacSecret
        }
    },
): JWT {
    return unsignedJwt().sign(builder)
}