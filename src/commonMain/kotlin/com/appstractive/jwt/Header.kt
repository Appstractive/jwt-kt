package com.appstractive.jwt

class Header

class HeaderBuilder {
    var keyId: String? = null

    internal fun build(): Header {
        return Header()
    }
}