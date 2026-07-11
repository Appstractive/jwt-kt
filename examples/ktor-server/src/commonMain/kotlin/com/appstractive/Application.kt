package com.appstractive

import com.appstractive.jwt.expiresAt
import com.appstractive.jwt.jwks
import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.signatures.es256
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class UserDTO(val username: String, val password: String)

internal val provider by lazy { CryptographyProvider.Default }
internal val ecdsa: ECDSA by lazy { provider.get(ECDSA) }
internal val curve = EC.Curve.P256

fun main() {
  val keys = ecdsa.keyPairGenerator(curve).generateKeyBlocking()

  embeddedServer(CIO, port = 8080) {
    install(ContentNegotiation) { json() }
    val issuer = "http://0.0.0.0:8080/"
    val audience = "http://0.0.0.0:8080/hello"
    val myRealm = "Access to 'hello'"
    install(Authentication) {
      jwt("auth-jwt") {
        realm = myRealm
        verifier(
            issuer = issuer,
            audience = audience,
        ) {
          jwks {
            endpoint = "http://localhost:8080/.well-known/jwt/jwks.json"
          }
        }

        validate { credential ->
          if (credential.claims["username"]?.jsonPrimitive?.content != "") {
            JWTPrincipal(credential.claims)
          } else {
            null
          }
        }

        challenge { defaultScheme, realm ->
          call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
        }
      }
    }

    routing {
      post("/login") {
        val user = call.receive<UserDTO>()
        // Check username and password
        // ...
        val token =
            jwt {
              claims {
                this.audience = audience
                this.issuer = issuer
                claim("username", user.username)
                expires()
              }
            }
                .sign {
                  es256 {
                    key(keys.privateKey)
                  }
                }

        call.respond(hashMapOf("token" to token.toString()))
      }

      authenticate("auth-jwt") {
        get("/hello") {
          val principal =
              call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
          val username = principal.claims["username"]?.jsonPrimitive?.content
          val expiresIn = principal.claims.expiresAt?.minus(Clock.System.now())
          call.respondText("Hello, $username! Token is expired in ${expiresIn?.inWholeSeconds} s.")
        }
      }

      get("/.well-known/jwt/jwks.json") {
        val jwkString = keys.publicKey.encodeToByteArray(EC.PublicKey.Format.JWK).decodeToString()
        val jwk = Json.decodeFromString<JsonObject>(jwkString)

        val jwks = buildJsonObject {
          put("keys", JsonArray(listOf(jwk)))
        }
        call.respond(jwks)
      }
    }
  }.start(wait = true)
}
