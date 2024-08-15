package com.appstractive.jwt

import kotlinx.serialization.Serializable

@Serializable
data class Header(
    val alg: Algorithm = Algorithm.HS256,
    val typ: String,
    val kid: String? = null,
)

@JwtDsl
class HeaderBuilder {
  var keyId: String? = null
  var typ: String = "JWT"

  internal fun build(): Header {
    return Header(
        typ = typ,
        kid = keyId,
    )
  }
}
