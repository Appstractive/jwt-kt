/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.auth.jwt

import com.appstractive.jwt.JWT
import com.appstractive.jwt.Verifier
import com.appstractive.jwt.from
import com.appstractive.jwt.verify
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.request.ApplicationRequest

internal fun AuthenticationContext.bearerChallenge(
    cause: AuthenticationFailedCause,
    realm: String,
    schemes: JWTAuthSchemes,
    challengeFunction: JWTAuthChallengeFunction
) {
  challenge(JWTAuthKey, cause) { challenge, call ->
    challengeFunction(JWTChallengeContext(call), schemes.defaultScheme, realm)
    if (!challenge.completed && call.response.status() != null) {
      challenge.complete()
    }
  }
}

internal suspend fun verifyAndValidate(
    call: ApplicationCall,
    jwtVerifier: Verifier?,
    token: HttpAuthHeader,
    schemes: JWTAuthSchemes,
    validate: suspend ApplicationCall.(JWTCredential) -> Any?
): Any? {
  val jwt = token.getBlob(schemes)?.let { JWT.from(it) } ?: return null

  jwtVerifier?.let { verifier ->
    if (!jwt.verify(verifier)) {
      return null
    }
  }

  val credentials = JWTCredential(jwt.claims)
  return validate(call, credentials)
}

internal fun HttpAuthHeader.getBlob(schemes: JWTAuthSchemes) =
    when {
      this is HttpAuthHeader.Single && authScheme in schemes -> blob
      else -> null
    }

internal fun ApplicationRequest.parseAuthorizationHeaderOrNull() =
    try {
      parseAuthorizationHeader()
    } catch (cause: IllegalArgumentException) {
      null
    }
