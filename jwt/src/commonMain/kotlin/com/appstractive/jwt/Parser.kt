package com.appstractive.jwt

import com.appstractive.jwt.utils.json
import com.appstractive.jwt.utils.urlDecodedBytes
import com.appstractive.jwt.utils.urlDecodedString

fun JWT.Companion.from(jwt: String): JWT {
    val parts = jwt.split(".")
    check(parts.size == 3) { "JWT expects 3 parts but got ${parts.size}" }
    val header = json.decodeFromString<Header>(parts[0].urlDecodedString())
    val claims = json.decodeFromString<Claims>(parts[1].urlDecodedString())
    val signature = parts[2].urlDecodedBytes()

    return JWT(
        header = header,
        claims = claims,
        signature = signature,
    )
}