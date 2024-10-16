/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.auth.jwt

import com.appstractive.jwt.Claims
import com.appstractive.jwt.Verifier
import com.appstractive.jwt.jwtVerifier
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlin.reflect.*

internal val JWTAuthKey: Any = "JWTAuthNative"

/**
 * A JWT credential that consists of the specified [claims].
 *
 * @param claims JWT
 * @see Claims
 */
public class JWTCredential(val claims: Claims) : Credential

/**
 * A JWT principal that consists of the specified [claims].
 *
 * @param payload JWT
 * @see Payload
 */
public class JWTPrincipal(val claims: Claims) : Principal

/**
 * A JWT [Authentication] provider.
 *
 * @see [jwt]
 */
public class JWTAuthenticationProvider internal constructor(config: Config) :
    AuthenticationProvider(config) {

  private val realm: String = config.realm
  private val schemes: JWTAuthSchemes = config.schemes
  private val authHeader: (ApplicationCall) -> HttpAuthHeader? = config.authHeader
  private val verifier: ((HttpAuthHeader) -> Verifier?) = config.verifier
  private val authenticationFunction = config.authenticationFunction
  private val challengeFunction: JWTAuthChallengeFunction = config.challenge

  override suspend fun onAuthenticate(context: AuthenticationContext) {
    val call = context.call
    val token = authHeader(call)
    if (token == null) {
      context.bearerChallenge(
          AuthenticationFailedCause.NoCredentials, realm, schemes, challengeFunction)
      return
    }

    try {
      val principal =
          verifyAndValidate(
              call = call,
              jwtVerifier = verifier(token),
              token = token,
              schemes = schemes,
              validate = authenticationFunction,
          )
      if (principal != null) {
        context.principal(name, principal)
        return
      }

      context.bearerChallenge(
          AuthenticationFailedCause.InvalidCredentials, realm, schemes, challengeFunction)
    } catch (cause: Throwable) {
      val message = cause.message ?: cause.toString()
      context.error(JWTAuthKey, AuthenticationFailedCause.Error(message))
    }
  }

  /** A configuration for the [jwt] authentication provider. */
  public class Config internal constructor(name: String?) : AuthenticationProvider.Config(name) {
    internal var authenticationFunction: AuthenticationFunction<JWTCredential> = {
      throw NotImplementedError(
          "JWT auth validate function is not specified. Use jwt { validate { ... } } to fix.")
    }

    internal var schemes = JWTAuthSchemes("Bearer")

    internal var authHeader: (ApplicationCall) -> HttpAuthHeader? = { call ->
      call.request.parseAuthorizationHeaderOrNull()
    }

    internal var verifier: ((HttpAuthHeader) -> Verifier?) = { null }

    internal var challenge: JWTAuthChallengeFunction = { scheme, realm ->
      call.respond(
          UnauthorizedResponse(
              HttpAuthHeader.Parameterized(
                  scheme, mapOf(HttpAuthHeader.Parameters.Realm to realm))))
    }

    /** Specifies a JWT realm to be passed in `WWW-Authenticate` header. */
    public var realm: String = "Ktor Server"

    /**
     * Retrieves an HTTP authentication header. By default, it parses the `Authorization` header
     * content.
     */
    public fun authHeader(block: (ApplicationCall) -> HttpAuthHeader?) {
      authHeader = block
    }

    /**
     * @param [defaultScheme] default scheme used to challenge the client when no valid
     *   authentication is provided
     * @param [additionalSchemes] additional schemes that are accepted when validating the
     *   authentication
     */
    public fun authSchemes(defaultScheme: String = "Bearer", vararg additionalSchemes: String) {
      schemes = JWTAuthSchemes(defaultScheme, *additionalSchemes)
    }

    /**
     * Provides a [Verifier] used to verify a token format and signature.
     *
     * @param [verifier] verifies token format and signature
     */
    public fun verifier(verifier: Verifier) {
      this.verifier = { verifier }
    }

    /** Provides a [JWTVerifier] used to verify a token format and signature. */
    public fun verifier(verifier: (HttpAuthHeader) -> Verifier?) {
      this.verifier = verifier
    }

    /**
     * Provides a [Verifier] used to verify a token format and signature.
     *
     * @param [issuer] of the JSON Web Token
     * @param [audience] restriction
     */
    public fun verifier(issuer: String, audience: String, block: Verifier.() -> Unit = {}) {
      val verification: Verifier = jwtVerifier {
        audience(audience)
        issuer(issuer)
        block()
      }

      verifier(verification)
    }

    /**
     * Allows you to perform additional validations on the JWT payload.
     *
     * @return a principal (usually an instance of [JWTPrincipal]) or `null`
     */
    public fun validate(validate: suspend ApplicationCall.(JWTCredential) -> Principal?) {
      authenticationFunction = validate
    }

    /** Specifies what to send back if JWT authentication fails. */
    public fun challenge(block: JWTAuthChallengeFunction) {
      challenge = block
    }

    internal fun build() = JWTAuthenticationProvider(this)
  }
}

/**
 * Installs the JWT [Authentication] provider. JWT (JSON Web Token) is an open standard that defines
 * a way for securely transmitting information between parties as a JSON object. To learn how to
 * configure it, see [JSON Web Tokens](https://ktor.io/docs/jwt.html).
 */
public fun AuthenticationConfig.jwt(
    name: String? = null,
    configure: JWTAuthenticationProvider.Config.() -> Unit
) {
  val provider = JWTAuthenticationProvider.Config(name).apply(configure).build()
  register(provider)
}

/** A context for [JWTAuthChallengeFunction]. */
public class JWTChallengeContext(public val call: ApplicationCall)

/** Specifies what to send back if JWT authentication fails. */
public typealias JWTAuthChallengeFunction =
    suspend JWTChallengeContext.(defaultScheme: String, realm: String) -> Unit
