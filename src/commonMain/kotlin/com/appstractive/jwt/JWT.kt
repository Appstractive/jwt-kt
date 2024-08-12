package com.appstractive.jwt

import kotlinx.serialization.json.JsonObject

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html">JSON Web Token (JWT)</a>
 */
data class JWT(
    val header: Header? = null,
    val claims: JsonObject,
    val signature: Signature? = null,
)

fun Jwt(builder: JwtBuilder.() -> Unit): JWT {
    val jwtBuilder = JwtBuilder().apply(builder)

    return jwtBuilder.build()
}

class JwtBuilder {

    private var header: HeaderBuilder? = null
    private var claims: ClaimsBuilder? = null

    fun header(header: HeaderBuilder.() -> Unit) {
        this.header = HeaderBuilder().apply(header)
    }

    fun claims(builder: ClaimsBuilder.() -> Unit) {
        this.claims = ClaimsBuilder().apply(builder)
    }

    internal fun build(): JWT {
        return JWT(
            header = header?.build(),
            claims = claims?.build() ?: JsonObject(emptyMap()),
        )
    }

}