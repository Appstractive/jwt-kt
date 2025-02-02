package com.appstractive.jwt

import com.appstractive.jwt.utils.urlEncoded

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class JwtDsl

/** @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html">JSON Web Token (JWT)</a> */
data class UnsignedJWT(
  val header: Header,
  val claims: Claims,
) {
  override fun toString(): String {
    return listOf(
        header.urlEncoded(),
        claims.urlEncoded(),
        "",
    )
        .joinToString(".")
  }
}

/** @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html">JSON Web Token (JWT)</a> */
data class JWT(
  val header: Header,
  val claims: Claims,
  val signedData: String,
  val signature: ByteArray,
) {

  override fun toString(): String {
    return listOf(
        signedData,
        urlEncoded(signature),
    )
        .joinToString(".")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as JWT

    if (signedData != other.signedData) return false
    if (header != other.header) return false
    if (claims != other.claims) return false
    if (!signature.contentEquals(other.signature)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = header.hashCode()
    result = 31 * result + claims.hashCode()
    result = 31 * result + signedData.hashCode()
    result = 31 * result + signature.contentHashCode()
    return result
  }

  companion object
}

fun jwt(builder: JwtBuilder.() -> Unit): UnsignedJWT {
  val jwtBuilder = JwtBuilder().apply(builder)

  return jwtBuilder.build()
}

@JwtDsl
class JwtBuilder {

  private var header: HeaderBuilder = HeaderBuilder()
  private var claims: ClaimsBuilder? = null

  fun header(header: HeaderBuilder.() -> Unit) {
    this.header = HeaderBuilder().apply(header)
  }

  fun claims(builder: ClaimsBuilder.() -> Unit) {
    this.claims = ClaimsBuilder().apply(builder)
  }

  internal fun build(): UnsignedJWT {
    val header = header.build()

    val claims = claims?.build() ?: Claims(emptyMap())

    return UnsignedJWT(
        header = header,
        claims = claims,
    )
  }
}
