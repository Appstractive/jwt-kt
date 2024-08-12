package com.appstractive.jwt

class Signature {
}

class SignatureBuilder {

    fun sign(jwt: JWT): Signature {
        return Signature()
    }

}

fun JWT.sign(builder: SignatureBuilder.() -> Unit): JWT {
    val signatureBuilder = SignatureBuilder().apply(builder)

    return copy(
        signature = signatureBuilder.sign(this),
    )
}
