package com.appstractive.jwt

import com.appstractive.jwt.utils.instantOrNull
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

typealias Claims = JsonObject

@JwtDsl
class ClaimsBuilder {
  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.1">RFC7519, Section
   *   4.1.1</a>
   */
  var issuer: String? = null

  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.2">RFC7519, Section
   *   4.1.2</a>
   */
  var subject: String? = null

  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.3">RFC7519, Section
   *   4.1.3</a>
   */
  var audience: String? = null

  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.4">RFC7519, Section
   *   4.1.4</a>
   */
  var expiresAt: Instant? = null

  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.5">RFC7519, Section
   *   4.1.5</a>
   */
  var notBefore: Instant? = null

  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.6">RFC7519, Section
   *   4.1.6</a>
   */
  var issuedAt: Instant? = null

  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.7">RFC7519, Section
   *   4.1.7</a>
   */
  var id: String? = null

  private val additionalClaims: MutableMap<String, JsonElement> = mutableMapOf()

  fun issuedAt(now: Instant = Clock.System.now()) {
    issuedAt = now
  }

  fun notBefore(before: Instant = Clock.System.now()) {
    notBefore = before
  }

  fun expires(at: Instant = Clock.System.now() + 60.minutes) {
    expiresAt = at
  }

  fun claim(key: String, value: String) {
    additionalClaims[key] = JsonPrimitive(value)
  }

  fun claim(key: String, value: Double) {
    additionalClaims[key] = JsonPrimitive(value)
  }

  fun claim(key: String, value: Long) {
    additionalClaims[key] = JsonPrimitive(value)
  }

  fun claim(key: String, value: Boolean) {
    additionalClaims[key] = JsonPrimitive(value)
  }

  @ExperimentalSerializationApi
  fun claim(key: String, value: Nothing?) {
    additionalClaims[key] = JsonPrimitive(value)
  }

  fun claim(key: String, value: JsonElement) {
    additionalClaims[key] = value
  }

  fun claim(key: String, value: JsonObject) {
    additionalClaims[key] = value
  }

  fun objectClaim(key: String, builder: JsonObjectBuilder.() -> Unit) {
    additionalClaims[key] = buildJsonObject(builder)
  }

  fun arrayClaim(key: String, builder: JsonArrayBuilder.() -> Unit) {
    additionalClaims[key] = buildJsonArray(builder)
  }

  fun claim(key: String, value: JsonArray) {
    additionalClaims[key] = value
  }

  internal fun build(): JsonObject {
    return JsonObject(
        buildMap {
          issuer?.let { put("iss", JsonPrimitive(it)) }
          subject?.let { put("sub", JsonPrimitive(it)) }
          audience?.let { put("aud", JsonPrimitive(it)) }
          expiresAt?.let { put("exp", JsonPrimitive(it.epochSeconds)) }
          notBefore?.let { put("nbf", JsonPrimitive(it.epochSeconds)) }
          issuedAt?.let { put("iat", JsonPrimitive(it.epochSeconds)) }
          id?.let { put("jti", JsonPrimitive(it)) }
          putAll(additionalClaims)
        },
    )
  }
}

/**
 * The "iss" (issuer) claim identifies the principal that issued the JWT. The processing of this
 * claim is generally application specific. The "iss" value is a case-sensitive string containing a
 * StringOrURI value.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.1">RFC7519, Section
 *   4.1.1</a>
 */
val JWT.issuer: String?
  get() = claims.issuer

val Claims.issuer: String?
  get() = get("iss")?.jsonPrimitive?.contentOrNull

/**
 * The "sub" (subject) claim identifies the principal that is the subject of the JWT. The claims in
 * a JWT are normally statements about the subject. The subject value MUST either be scoped to be
 * locally unique in the context of the issuer or be globally unique. The processing of this claim
 * is generally application specific. The "sub" value is a case-sensitive string containing a
 * StringOrURI value.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.2">RFC7519, Section
 *   4.1.2</a>
 */
val JWT.subject: String?
  get() = claims.subject

val Claims.subject: String?
  get() = get("sub")?.jsonPrimitive?.contentOrNull

/**
 * The "aud" (audience) claim identifies the recipients that the JWT is intended for. Each principal
 * intended to process the JWT MUST identify itself with a value in the audience claim. If the
 * principal processing the claim does not identify itself with a value in the "aud" claim when this
 * claim is present, then the JWT MUST be rejected. In the general case, the "aud" value is an array
 * of case- sensitive strings, each containing a StringOrURI value. In the special case when the JWT
 * has one audience, the "aud" value MAY be a single case-sensitive string containing a StringOrURI
 * value. The interpretation of audience values is generally application specific.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.3">RFC7519, Section
 *   4.1.3</a>
 */
val JWT.audience: String?
  get() = claims.audience

val Claims.audience: String?
  get() = get("aud")?.jsonPrimitive?.contentOrNull

/**
 * The "exp" (expiration time) claim identifies the expiration time on or after which the JWT MUST
 * NOT be accepted for processing. The processing of the "exp" claim requires that the current
 * date/time MUST be before the expiration date/time listed in the "exp" claim. Implementers MAY
 * provide for some small leeway, usually no more than a few minutes, to account for clock skew. Its
 * value MUST be a number containing a NumericDate value.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.4">RFC7519, Section
 *   4.1.4</a>
 */
val JWT.expiresAt: Instant?
  get() = claims.expiresAt

val Claims.expiresAt: Instant?
  get() = instantOrNull("exp")

/**
 * The "nbf" (not before) claim identifies the time before which the JWT MUST NOT be accepted for
 * processing. The processing of the "nbf" claim requires that the current date/time MUST be after
 * or equal to the not-before date/time listed in the "nbf" claim. Implementers MAY provide for some
 * small leeway, usually no more than a few minutes, to account for clock skew. Its value MUST be a
 * number containing a NumericDate value.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.5">RFC7519, Section
 *   4.1.5</a>
 */
val JWT.notBefore: Instant?
  get() = claims.notBefore

val Claims.notBefore: Instant?
  get() = instantOrNull("nbf")

/**
 * The "iat" (issued at) claim identifies the time at which the JWT was issued. This claim can be
 * used to determine the age of the JWT. Its value MUST be a number containing a NumericDate value.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.6">RFC7519, Section
 *   4.1.6</a>
 */
val JWT.issuedAt: Instant?
  get() = claims.issuedAt

val Claims.issuedAt: Instant?
  get() = instantOrNull("iat")

/**
 * The "jti" (JWT ID) claim provides a unique identifier for the JWT. The identifier value MUST be
 * assigned in a manner that ensures that there is a negligible probability that the same value will
 * be accidentally assigned to a different data object; if the application uses multiple issuers,
 * collisions MUST be prevented among values produced by different issuers as well. The "jti" claim
 * can be used to prevent the JWT from being replayed. The "jti" value is a case- sensitive string.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html#section-4.1.7">RFC7519, Section
 *   4.1.7</a>
 */
val JWT.id: String?
  get() = claims.id

val Claims.id: String?
  get() = get("jti")?.jsonPrimitive?.contentOrNull
