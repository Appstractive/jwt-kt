package jwt

import com.appstractive.jwt.JWT
import com.appstractive.jwt.from
import com.appstractive.jwt.jwks
import com.appstractive.jwt.verifier
import com.appstractive.jwt.verify
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class JwksVerifierTests {

  val mockEngine = MockEngine { request ->
    respond(
        content = ByteReadChannel(KEY_SET_RSA),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
  }

  val mockClient = HttpClient(mockEngine)

  @Test
  fun testVerifyRSA() = runTest {
    val verifier = verifier {
      jwks {
        client = mockClient
        endpoint = "http://example.com/.well-known/jwt/jwks.json"
        cacheDuration = 1.minutes
      }
    }

    val jwt = JWT.from(RSA_JWT_VALID)

    jwt.verify(verifier)
  }

  companion object {
    const val KEY_SET_RSA = "{\n" +
        "    \"keys\": [\n" +
        "        {\n" +
        "            \"kty\": \"RSA\",\n" +
        "            \"kid\": \"d-1723431926928\",\n" +
        "            \"n\": \"qTLDwMJWj2upWCmnIRW9K6rfK_VI6PlNbF_P9tgWL5lwvksvphUa2lYGexbo7Sv0nU-ndQOEcGq9GELmg_H02BqwqRFZLmv9Gh3rgJWFUsx7KqD4Cy2IxJ55MYz_ZK0ucLhoX0Bur6tMkMcU2tySKBjm7ZupN141B4phOB0U6mhfswmKOdlEs5-sQnPz4akkcTw9RdK8-egGJqgUsTyT31NHGu8Szl-X87Z5vjP9Nd8jHfHJT4zDVSlI8O6LQFTPoZP-vs0GPvCAcypkNaHa7gjkNyCVRQ6tg0i2z24mINoczTC4jtR4UeqDPWqbb1xepHGWDgrhEpJqmVEq7Mh3wQ\",\n" +
        "            \"e\": \"AQAB\",\n" +
        "            \"alg\": \"RS256\",\n" +
        "            \"use\": \"sig\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"kty\": \"RSA\",\n" +
        "            \"kid\": \"s-dfffd02e-9a42-4430-a502-2d16253d7640\",\n" +
        "            \"n\": \"guCueWspilZxxFfv3G60dZ8F6AEtDvM4CcrOJv1dkwYSXWWhBTAZCbq3GhIe1yPHsg2vnGaPP-QscBXIjZWs4_GwnohO1TENoi4Xehz3tsy6Dd-4upucaqpAvYNXqDRJ2STiv_JsISkQDMxjon5xFp61ipUreX1chz_HiIDirtr6YW_6HM_YyFOLj9rfr48l5rkxOR6s637787gkDrXikreFGEMOkk4ANLmojOzQBTfCeLOi2MaYsWs8xugZNdy3musrzWjjfJePytc4OvVM2FYoPhe0z9NuK5Q-p9QsqlvNGpNgjedz8HSIY0KUMtZTc9ojYUd7kMSIINAeSz2Z7Q\",\n" +
        "            \"e\": \"AQAB\",\n" +
        "            \"alg\": \"RS256\",\n" +
        "            \"use\": \"sig\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"

    const val RSA_JWT_VALID = "eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNTAyLTJkMTYyNTNkNzY0MCIsInR5cCI6IkpXVCIsInZlcnNpb24iOiI1IiwiYWxnIjoiUlMyNTYifQ.eyJpYXQiOjE3MjM3MjI0MTUsImV4cCI6MTcyMzcyNjAxNSwic3ViIjoiZmE0NDhhYWItZjA3MS00MTRhLTliNzItMjQ4ZTEyM2JiNWIyIiwidElkIjoicHVibGljIiwicnN1YiI6ImZhNDQ4YWFiLWYwNzEtNDE0YS05YjcyLTI0OGUxMjNiYjViMiIsInNlc3Npb25IYW5kbGUiOiJmNDJjYWI2Mi03NmNjLTRlZTUtOTkyYy05YTNhNzhlYmFjNjIiLCJyZWZyZXNoVG9rZW5IYXNoMSI6IjlkNDZkMThkNGNiZDEwZGEzOTAyMTk0NDU3ZWNmOWU4NDlkNjdhYjJkN2I1ZGIyZjgwYjc5MmRmNGQyZjhjMGMiLCJwYXJlbnRSZWZyZXNoVG9rZW5IYXNoMSI6bnVsbCwiYW50aUNzcmZUb2tlbiI6bnVsbCwiaXNzIjoiYXV0aC5hcHBzdHJhY3RpdmUuY2xvdWQiLCJhdWQiOiJhdXRoLmFwcHN0cmFjdGl2ZS5jbG91ZCIsImVtYWlsIjoidGVzdDNAdGVzdC5kZSIsInN0LWV2IjpmYWxzZSwic3QtbWZhIjp7ImMiOnsiZW1haWxwYXNzd29yZCI6MTcyMzcyMjQxNTQzN30sInYiOmZhbHNlfSwiaXNFeGFtcGxlIjp0cnVlfQ.IMbcjLsi3xISvGu230-TDt41UkPIXOL5pdZ_20VMkNmeGLeorRKw0qE3KE-hW-7fs4fhGZMuksQwxnhQAfAm1nrcYoxtlemYn2cpYkrGIqkU6-o6yMmV-eFr0F9VYsd54wlvSIb4eRzW2y0YD0o6BuASRf3odX1m1haPPpPP-UhyYMLOsyBvmHqQy06OxHxax3IAmO9lIrcDNUE7Ve_OGmWaR_Lh1-xMfhHdlVgHdf-GHgyhoKJ_t1KukhKm49m2GiTpDKGazhks8H7iDt5lXcjR3heTSHIWrbt92iwT5dWrBwrLKoIClBQrFxkE9UieJba-SGQm2ymK_tM-sH3Few"
  }
}
