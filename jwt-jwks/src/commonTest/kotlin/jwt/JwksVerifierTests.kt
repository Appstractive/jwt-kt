package jwt

import com.appstractive.jwt.JWT
import com.appstractive.jwt.from
import com.appstractive.jwt.jwks
import com.appstractive.jwt.jwtVerifier
import com.appstractive.jwt.verify
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class JwksVerifierTests {

  private val mockEngine = MockEngine { request ->
    val keySet = when (request.url.segments.last()) {
      "jwks2.json" -> KEY_SET_RSA2
      "jwksec.json" -> KEY_SET_EC
      "jwksec2.json" -> KEY_SET_EC2
      else -> KEY_SET_RSA
    }
    respond(
        content = ByteReadChannel(keySet),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
  }

  private val mockClient = HttpClient(mockEngine)

  @Test
  fun testVerifyRsaSuccess() = runTest {
    val verifier = jwtVerifier {
      jwks {
        client = mockClient
        endpoint = "http://example.com/.well-known/jwt/jwks.json"
        cacheDuration = 1.minutes
      }
    }

    val jwt = JWT.from(RSA_JWT_VALID)

    assertTrue(jwt.verify(verifier))
  }

  @Test
  fun testVerifyRsaFailNoKey() = runTest {
    val verifier = jwtVerifier {
      jwks {
        client = mockClient
        endpoint = "http://example.com/.well-known/jwt/jwks2.json"
        cacheDuration = 1.minutes
      }
    }

    val jwt = JWT.from(RSA_JWT_VALID)

    assertFalse(jwt.verify(verifier))
  }

  @Test
  fun testVerifyRsaFailTampered() = runTest {
    val verifier = jwtVerifier {
      jwks {
        client = mockClient
        endpoint = "http://example.com/.well-known/jwt/jwks.json"
        cacheDuration = 1.minutes
      }
    }

    val jwt = JWT.from(RSA_JWT_TAMPERED)

    assertFalse(jwt.verify(verifier))
  }

  @Test
  fun testVerifyEcSuccess() = runTest {
    val verifier = jwtVerifier {
      jwks {
        client = mockClient
        endpoint = "http://example.com/.well-known/jwt/jwksec.json"
        cacheDuration = 1.minutes
      }
    }

    val jwt = JWT.from(EC_JWT_VALID)

    assertTrue(jwt.verify(verifier))
  }

  @Test
  fun testVerifyEcFailNoKey() = runTest {
    val verifier = jwtVerifier {
      jwks {
        client = mockClient
        endpoint = "http://example.com/.well-known/jwt/jwksec2.json"
        cacheDuration = 1.minutes
      }
    }

    val jwt = JWT.from(EC_JWT_VALID)

    assertFalse(jwt.verify(verifier))
  }

  @Test
  fun testVerifyEcFailTampered() = runTest {
    val verifier = jwtVerifier {
      jwks {
        client = mockClient
        endpoint = "http://example.com/.well-known/jwt/jwksec.json"
        cacheDuration = 1.minutes
      }
    }

    val jwt = JWT.from(EC_JWT_TAMPERED)

    assertFalse(jwt.verify(verifier))
  }

  companion object {
    val KEY_SET_RSA = """
      {
          "keys": [
              {
                  "kty": "RSA",
                  "kid": "d-1723431926928",
                  "n": "qTLDwMJWj2upWCmnIRW9K6rfK_VI6PlNbF_P9tgWL5lwvksvphUa2lYGexbo7Sv0nU-ndQOEcGq9GELmg_H02BqwqRFZLmv9Gh3rgJWFUsx7KqD4Cy2IxJ55MYz_ZK0ucLhoX0Bur6tMkMcU2tySKBjm7ZupN141B4phOB0U6mhfswmKOdlEs5-sQnPz4akkcTw9RdK8-egGJqgUsTyT31NHGu8Szl-X87Z5vjP9Nd8jHfHJT4zDVSlI8O6LQFTPoZP-vs0GPvCAcypkNaHa7gjkNyCVRQ6tg0i2z24mINoczTC4jtR4UeqDPWqbb1xepHGWDgrhEpJqmVEq7Mh3wQ",
                  "e": "AQAB",
                  "alg": "RS256",
                  "use": "sig"
              },
              {
                  "kty": "RSA",
                  "kid": "s-dfffd02e-9a42-4430-a502-2d16253d7640",
                  "n": "guCueWspilZxxFfv3G60dZ8F6AEtDvM4CcrOJv1dkwYSXWWhBTAZCbq3GhIe1yPHsg2vnGaPP-QscBXIjZWs4_GwnohO1TENoi4Xehz3tsy6Dd-4upucaqpAvYNXqDRJ2STiv_JsISkQDMxjon5xFp61ipUreX1chz_HiIDirtr6YW_6HM_YyFOLj9rfr48l5rkxOR6s637787gkDrXikreFGEMOkk4ANLmojOzQBTfCeLOi2MaYsWs8xugZNdy3musrzWjjfJePytc4OvVM2FYoPhe0z9NuK5Q-p9QsqlvNGpNgjedz8HSIY0KUMtZTc9ojYUd7kMSIINAeSz2Z7Q",
                  "e": "AQAB",
                  "alg": "RS256",
                  "use": "sig"
              },
              {
                  "kid": "SC3IWaKN-7FqcsKo4VZGDPhovW9nlGcBn2YZOguGr_c",
                  "kty": "RSA",
                  "alg": "RSA-OAEP",
                  "use": "enc",
                  "n": "7NsBybCadmLmhU9ccokGixh-hGKIyzNKmgT1p8tIn7Z0RdxONoYpUM7sEqmE4AMFiSrBCkWzI6Yq9tY6eeJUSoyOZpOkTY1rRgaQ_kfp51RCNQPRSawrYs86AeeRm8O0ZSytewv1yPZbyyY-_1mVjsJiS6n-nxj5p5ZikZ2sD79sJVktn6ZbOzEoRMEQc2XXBD3S2xTvluc45us3fmhbOjt_qiH0u43a9SSqu6rWivfOp6hAl6LNu2xZy4uoxuQUIM_Ua6AVBGzrM_4kfAHPVo7q4boMO-FWsrDvq7m3RkLGUp5gRWdGaOoxy5GhXnE-EE9PSZ9S0NbZKuVzVjK1dQ",
                  "e": "AQAB",
                  "x5c": [
                    "MIIClzCCAX8CBgGf7F4ZrDANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDDAR0ZXN0MB4XDTI2MDgxMDE1NDgzN1oXDTM2MDgxMDE1NTAxN1owDzENMAsGA1UEAwwEdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAOzbAcmwmnZi5oVPXHKJBosYfoRiiMszSpoE9afLSJ+2dEXcTjaGKVDO7BKphOADBYkqwQpFsyOmKvbWOnniVEqMjmaTpE2Na0YGkP5H6edUQjUD0UmsK2LPOgHnkZvDtGUsrXsL9cj2W8smPv9ZlY7CYkup/p8Y+aeWYpGdrA+/bCVZLZ+mWzsxKETBEHNl1wQ90tsU75bnOObrN35oWzo7f6oh9LuN2vUkqruq1or3zqeoQJeizbtsWcuLqMbkFCDP1GugFQRs6zP+JHwBz1aO6uG6DDvhVrKw76u5t0ZCxlKeYEVnRmjqMcuRoV5xPhBPT0mfUtDW2Srlc1YytXUCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEApVa54i+ITGtzhWznAbnt8v3WoT7ZjSfWS7SpD+lR1y+JCzgJqvJDuysCcayfiPZNikLN0DWq99XE/KrNRJrZ2KpYL71hQu/QhJyNFKSZH/GAnqNYqmT+myHxIXBaCfXUHK4XqMqA2XSzt0bChYdMQsrB2bt+pvwvRYrqsXnt2rycL9FvKgrggD1I+hISEwo8WXye2L3RoA5eUHk6836M7S9DWWucOjPpbmNedJk92JzeAwOTHqaX8GHt2XmaXHb8DGH7IeKlRUW4cED+YW9IF+J4Fau/IwAHkMWWlyiM5ykZLZRDgjf40zt6tR3Crx7ZqzZ1wN6hY5318F5f6Pxwug=="
                  ],
                  "x5t": "YuEMCAcYeJnSnhMZ6uJdD2NyL5c",
                  "x5t#S256": "hjw3CtwkuDa7hZHcqOOmFM1hXUPdBzmlA7PrN9eWtlU"
              }
          ]
      }
    """.trimMargin()


    val KEY_SET_RSA2 = """
      {
        "keys": [
            {
                "kty": "RSA",
                "kid": "d-123",
                "n": "qTLDwMJWj2upWCmnIRW9K6rfK_VI6PlNbF_P9tgWL5lwvksvphUa2lYGexbo7Sv0nU-ndQOEcGq9GELmg_H02BqwqRFZLmv9Gh3rgJWFUsx7KqD4Cy2IxJ55MYz_ZK0ucLhoX0Bur6tMkMcU2tySKBjm7ZupN141B4phOB0U6mhfswmKOdlEs5-sQnPz4akkcTw9RdK8-egGJqgUsTyT31NHGu8Szl-X87Z5vjP9Nd8jHfHJT4zDVSlI8O6LQFTPoZP-vs0GPvCAcypkNaHa7gjkNyCVRQ6tg0i2z24mINoczTC4jtR4UeqDPWqbb1xepHGWDgrhEpJqmVEq7Mh3wQ",
                "e": "AQAB",
                "alg": "RS256",
                "use": "sig"
            },
            {
                "kty": "RSA",
                "kid": "s-456",
                "n": "guCueWspilZxxFfv3G60dZ8F6AEtDvM4CcrOJv1dkwYSXWWhBTAZCbq3GhIe1yPHsg2vnGaPP-QscBXIjZWs4_GwnohO1TENoi4Xehz3tsy6Dd-4upucaqpAvYNXqDRJ2STiv_JsISkQDMxjon5xFp61ipUreX1chz_HiIDirtr6YW_6HM_YyFOLj9rfr48l5rkxOR6s637787gkDrXikreFGEMOkk4ANLmojOzQBTfCeLOi2MaYsWs8xugZNdy3musrzWjjfJePytc4OvVM2FYoPhe0z9NuK5Q-p9QsqlvNGpNgjedz8HSIY0KUMtZTc9ojYUd7kMSIINAeSz2Z7Q",
                "e": "AQAB",
                "alg": "RS256",
                "use": "sig"
            }
        ]
      }
    """.trimMargin()

    val KEY_SET_EC = """
      {
        "keys": [
          {
            "alg": "ES256",
            "crv": "P-256",
            "ext": true,
            "key_ops": [
              "verify"
            ],
            "kty": "EC",
            "x": "kl_HyooML4CUsJLKEXs2bLdDaPNFP4rPZw34zNpZOYo",
            "y": "h_j1eSluwVYRD3pJNMQrzwfxejBRliza4ztU19fL_mI",
            "kid": "WfGM06LIYr5acjRHML5GVwpQx9JBV9iR4rgyMH5qUUU=",
            "use": "sig"
          }
        ]
      }
    """.trimMargin()

    val KEY_SET_EC2 = """
      {
        "keys": [
          {
            "alg": "ES256",
            "crv": "P-256",
            "ext": true,
            "key_ops": [
              "verify"
            ],
            "kty": "EC",
            "x": "kl_HyooML4CUsJLKEXs2bLdDaPNFP4rPZw34zNpZOYo",
            "y": "h_j1eSluwVYRD3pJNMQrzwfxejBRliza4ztU19fL_mI",
            "kid": "1234",
            "use": "sig"
          }
        ]
      }
    """.trimMargin()

    const val RSA_JWT_VALID =
        "eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNTAyLTJkMTYyNTNkNzY0MCIsInR5cCI6IkpXVCIsInZlcnNpb24iOiI1IiwiYWxnIjoiUlMyNTYifQ.eyJpYXQiOjE3MjM3MjI0MTUsImV4cCI6MTcyMzcyNjAxNSwic3ViIjoiZmE0NDhhYWItZjA3MS00MTRhLTliNzItMjQ4ZTEyM2JiNWIyIiwidElkIjoicHVibGljIiwicnN1YiI6ImZhNDQ4YWFiLWYwNzEtNDE0YS05YjcyLTI0OGUxMjNiYjViMiIsInNlc3Npb25IYW5kbGUiOiJmNDJjYWI2Mi03NmNjLTRlZTUtOTkyYy05YTNhNzhlYmFjNjIiLCJyZWZyZXNoVG9rZW5IYXNoMSI6IjlkNDZkMThkNGNiZDEwZGEzOTAyMTk0NDU3ZWNmOWU4NDlkNjdhYjJkN2I1ZGIyZjgwYjc5MmRmNGQyZjhjMGMiLCJwYXJlbnRSZWZyZXNoVG9rZW5IYXNoMSI6bnVsbCwiYW50aUNzcmZUb2tlbiI6bnVsbCwiaXNzIjoiYXV0aC5hcHBzdHJhY3RpdmUuY2xvdWQiLCJhdWQiOiJhdXRoLmFwcHN0cmFjdGl2ZS5jbG91ZCIsImVtYWlsIjoidGVzdDNAdGVzdC5kZSIsInN0LWV2IjpmYWxzZSwic3QtbWZhIjp7ImMiOnsiZW1haWxwYXNzd29yZCI6MTcyMzcyMjQxNTQzN30sInYiOmZhbHNlfSwiaXNFeGFtcGxlIjp0cnVlfQ.IMbcjLsi3xISvGu230-TDt41UkPIXOL5pdZ_20VMkNmeGLeorRKw0qE3KE-hW-7fs4fhGZMuksQwxnhQAfAm1nrcYoxtlemYn2cpYkrGIqkU6-o6yMmV-eFr0F9VYsd54wlvSIb4eRzW2y0YD0o6BuASRf3odX1m1haPPpPP-UhyYMLOsyBvmHqQy06OxHxax3IAmO9lIrcDNUE7Ve_OGmWaR_Lh1-xMfhHdlVgHdf-GHgyhoKJ_t1KukhKm49m2GiTpDKGazhks8H7iDt5lXcjR3heTSHIWrbt92iwT5dWrBwrLKoIClBQrFxkE9UieJba-SGQm2ymK_tM-sH3Few"
    const val RSA_JWT_TAMPERED =
        "eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNTAyLTJkMTYyNTNkNzY0MCIsInR5cCI6IkpXVCIsInZlcnNpb24iOiI1IiwiYWxnIjoiUlMyNTYifQ.eyJpYXQiOjE3MjM3MjI0MTUsImV4cCI6MTcyMzcyNjAxNSwic3ViIjoiZmE0NDhhYWItZjA3MS00MTRhLTliNzItMjQ4ZTEyM2JiNWIyIiwidElkIjoicHVibGljIiwicnN1YiI6ImZhNDQ4YWFiLWYwNzEtNDE0YS05YjcyLTI0OGUxMjNiYjViMiIsInNlc3Npb25IYW5kbGUiOiJmNDJjYWI2Mi03NmNjLTRlZTUtOTkyYy05YTNhNzhlYmFjNjIiLCJyZWZyZXNoVG9rZW5IYXNoMSI6IjlkNDZkMThkNGNiZDEwZGEzOTAyMTk0NDU3ZWNmOWU4NDlkNjdhYjJkN2I1ZGIyZjgwYjc5MmRmNGQyZjhjMGMiLCJwYXJlbnRSZWZyZXNoVG9rZW5IYXNoMSI6bnVsbCwiYW50aUNzcmZUb2tlbiI6bnVsbCwiaXNzIjoiYXV0aC5hcHBzdHJhY3RpdmUuY2xvdWQiLCJhdWQiOiJhdXRoLmFwcHN0cmFjdGl2ZS5jbG91ZCIsImVtYWlsIjoidGVzdDNAdGVzdC5kZSIsInN0LWV2IjpmYWxzZSwic3QtbWZhIjp7ImMiOnsiZW1haWxwYXNzd29yZCI6MTcyMzcyMjQxNTQzN30sInYiOmZhbHNlfSwiaXNFeGFtcGxlIjpmYWxzZX0.IMbcjLsi3xISvGu230-TDt41UkPIXOL5pdZ_20VMkNmeGLeorRKw0qE3KE-hW-7fs4fhGZMuksQwxnhQAfAm1nrcYoxtlemYn2cpYkrGIqkU6-o6yMmV-eFr0F9VYsd54wlvSIb4eRzW2y0YD0o6BuASRf3odX1m1haPPpPP-UhyYMLOsyBvmHqQy06OxHxax3IAmO9lIrcDNUE7Ve_OGmWaR_Lh1-xMfhHdlVgHdf-GHgyhoKJ_t1KukhKm49m2GiTpDKGazhks8H7iDt5lXcjR3heTSHIWrbt92iwT5dWrBwrLKoIClBQrFxkE9UieJba-SGQm2ymK_tM-sH3Few"

    const val EC_JWT_VALID = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6IldmR00wNkxJWXI1YWNqUkhNTDVHVndwUXg5SkJWOWlSNHJneU1INXFVVVU9In0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOmZhbHNlLCJpYXQiOjE3ODM3NjQwNTUsImV4cCI6MTc4Mzc2NzY1NX0.bOKwV1QgEZL758zq0mrggjbMMQMZgWvIldjHapZqXtRkPyiLSX2wqfEH2G0E8mdHiYsYx3fJqeC45zDcbfiISA"
    const val EC_JWT_TAMPERED = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6IldmR00wNkxJWXI1YWNqUkhNTDVHVndwUXg5SkJWOWlSNHJneU1INXFVVVU9In0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTc4Mzc2NDA1NSwiZXhwIjoxNzgzNzY3NjU1fQ.IqMD9sv08Ws6Ake2FJrfPvMivmWB5qC0L7pqbSUk7v9n08H1FfZMYa2GwhMUBDABfRXE8J1QxJzF18hfY1Rpaw"
  }
}
