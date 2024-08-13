package com.appstractive.jwt

import com.appstractive.jwt.utils.urlEncoded
import kotlinx.serialization.json.JsonObject

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7519.html">JSON Web Token (JWT)</a>
 */
data class JWT(
    val header: Header,
    val claims: JsonObject,
    val signature: ByteArray,
) {

    override fun toString(): String {
        return listOf(
            header.urlEncoded(),
            claims.urlEncoded(),
            urlEncoded(signature),
        ).joinToString(".")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JWT

        if (header != other.header) return false
        if (claims != other.claims) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + claims.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}

suspend fun jwt(builder: JwtBuilder.() -> Unit): JWT {
    val jwtBuilder = JwtBuilder().apply(builder)

    return jwtBuilder.build()
}

class JwtBuilder {

    private var header: HeaderBuilder = HeaderBuilder()
    private var claims: ClaimsBuilder? = null
    private var signature: SignatureBuilder? = null

    fun header(header: HeaderBuilder.() -> Unit) {
        this.header = HeaderBuilder().apply(header)
    }

    fun claims(builder: ClaimsBuilder.() -> Unit) {
        this.claims = ClaimsBuilder().apply(builder)
    }

    fun signature(signature: SignatureBuilder.() -> Unit) {
        this.signature = SignatureBuilder().apply(signature)
    }

    internal suspend fun build(): JWT {
        return signature?.let {
            val header = header.build(
                alg = it.type,
            )

            val claims = claims?.build() ?: JsonObject(emptyMap())

            JWT(
                header = header,
                claims = claims,
                signature = it.sign(
                    header = header,
                    claims = claims,
                ),
            )
        } ?: throw IllegalStateException("no signature provided")
    }

}