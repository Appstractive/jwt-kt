package com.appstractive.jwt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

class JwtParserTests {

  @Test
  fun testParse() {
    val jwt =
        JWT.from(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjEyMzQ1NiJ9.eyJpc3MiOiJleGFtcGxlLmNvbSIsInN1YiI6Im1lIiwiYXVkIjoiYXBpLmV4YW1wbGUuY29tIiwiZXhwIjoxNzIzNzMyMjY4LCJuYmYiOjE3MjM2NDU4NjgsImlhdCI6MTcyMzY0NTg2OCwianRpIjoiMTIzNDU2IiwiZG91YmxlIjoxLjEsImxvbmciOjEsImJvb2wiOnRydWUsInN0cmluZyI6InRlc3QiLCJudWxsIjpudWxsLCJvYmplY3QiOnsia2V5IjoidmFsdWUifSwibGlzdCI6WzAsMSwyXSwiY29tcGxleCI6eyJsb25nIjoxMjM0NDM1NDM2NDIzNTMsImludCI6MSwiZG91YmxlIjoxLjI0MzIxNDEyMzI1MzI1RTgsImZsb2F0IjoxLjI0MTQsImJvb2wiOnRydWUsInN0cmluZyI6InRlc3QiLCJlbnVtIjoiVkFMVUUxIiwibGlzdCI6W3sibG9uZyI6MTIzNDQzNTQzNjQyMzUzLCJpbnQiOjEsImRvdWJsZSI6MS4yNDMyMTQxMjMyNTMyNUU4LCJmbG9hdCI6MS4yNDE0LCJib29sIjpmYWxzZSwic3RyaW5nIjoidGVzdDIiLCJlbnVtIjoiVkFMVUUyIiwibGlzdCI6W119XX19.15OVSybg14VvqHOthDNZrpGLI18BGmB6LdHMOg--LtE",
        )

    assertEquals("example.com", jwt.issuer)
    assertEquals("me", jwt.subject)
    assertEquals(Instant.fromEpochSeconds(1723732268), jwt.expiresAt)
    assertEquals(Instant.fromEpochSeconds(1723645868), jwt.notBefore)
    assertEquals(Instant.fromEpochSeconds(1723645868), jwt.issuedAt)
    assertEquals("123456", jwt.id)
    assertEquals(1.1, jwt.claims["double"]?.jsonPrimitive?.double)
  }
}
