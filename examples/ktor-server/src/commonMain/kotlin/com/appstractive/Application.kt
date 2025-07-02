package com.appstractive

import com.appstractive.jwt.expiresAt
import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.signatures.hs256
import dev.whyoleg.cryptography.random.CryptographyRandom
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive

@Serializable data class UserDTO(val username: String, val password: String)

fun main() {
  embeddedServer(CIO, port = 8080) {
    install(ContentNegotiation) { json() }
    val secret = CryptographyRandom.nextBytes(64)
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
          hs256 { this.secret = secret }
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
                .sign { hs256 { this.secret = secret } }

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
    }
  }.start(wait = true)
}
