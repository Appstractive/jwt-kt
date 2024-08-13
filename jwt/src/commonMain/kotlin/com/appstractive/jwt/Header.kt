package com.appstractive.jwt

import kotlinx.serialization.Serializable

@Serializable
data class Header(
    val alg: Algorithm,
    val typ: String,
)

class HeaderBuilder {
    var keyId: String? = null
    var typ: String = "JWT"

    internal fun build(alg: Algorithm): Header {
        return Header(
            typ = typ,
            alg = alg,
        )
    }
}