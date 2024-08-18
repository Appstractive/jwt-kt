package com.appstractive

import com.appstractive.jwt.expiresAt
import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.signatures.hs256
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock
import kotlinx.serialization.*
import kotlinx.serialization.json.jsonPrimitive

@Serializable data class UserDTO(val username: String, val password: String)

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress(
    "unused") // application.conf references the main function. This annotation prevents the IDE
// from marking it as unused.
fun Application.module() {
  install(ContentNegotiation) { json() }
  val secret = environment.config.property("jwt.secret").getString()
  val issuer = environment.config.property("jwt.issuer").getString()
  val audience = environment.config.property("jwt.audience").getString()
  val myRealm = environment.config.property("jwt.realm").getString()
  install(Authentication) {
    jwt("auth-jwt") {
      realm = myRealm
      verifier(
          issuer = issuer,
          audience = audience,
      ) {
        hs256 {
          this.secret = secret.encodeToByteArray()
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
                  expiresAt = Clock.System.now() + 60.minutes
                }
              }
              .sign { hs256 { this.secret = secret.encodeToByteArray() } }

      call.respond(hashMapOf("token" to token.toString()))
    }

    authenticate("auth-jwt") {
      get("/hello") {
        val principal = call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
        val username = principal.claims["username"]?.jsonPrimitive?.content
        val expiresIn = principal.claims.expiresAt?.minus(Clock.System.now())
        call.respondText("Hello, $username! Token is expired in ${expiresIn?.inWholeSeconds} s.")
      }
    }
  }
}
