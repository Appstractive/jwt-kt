# KTOR Server Auth JWT Kotlin Multiplatform

![badge][badge-android]
![badge][badge-apple]
![badge][badge-jvm]
![badge][badge-linux]

A fork of the [KTOR Server Auth JWT Plugin](https://ktor.io/docs/server-jwt.html) supporting more platforms than just
JVM.

## Usage

### Installation

Gradle:

```
commonMain.dependencies { 
    implementation("com.appstractive:ktor-server-auth-jwt:1.0.1")
    implementation("com.appstractive:jwt-hmac-kt:1.0.1")
    // or
    // implementation("com.appstractive:jwt-rsa-kt:1.0.1")
    // implementation("com.appstractive:jwt-ecdsa-kt:1.0.1")
}
```

### Install

```kotlin
val mySecret = CryptographyRandom.nextBytes(64)

install(Authentication) {
    jwt("auth-jwt") {
        realm = "myRealm"
        verifier(
            issuer = "example.com",
            audience = "api.example.com",
        ) {
            hs256 { secret = mySecret }
        }

        validate { credential ->
            if (credential.claims["username"]?.jsonPrimitive?.content != "") {
                JWTPrincipal(credential.claims)
            } else {
                null
            }
        }

        challenge { defaultScheme, realm ->
            call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
        }
    }
}

post("/login") {
    val user = call.receive<UserDTO>()
    // Check username and password
    // ...
    val token =
        jwt {
            claims {
                this.audience = audience
                this.issuer = issuer
                claim("username", user.username)
                expires()
            }
        }
            .sign { hs256 { secret = mySecret } }

    call.respond(hashMapOf("token" to token.toString()))
}

```

## License

```
Copyright 2024 Andreas Schulz.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat

[badge-apple]: http://img.shields.io/badge/platform-apple-111111.svg?style=flat

[badge-jvm]: http://img.shields.io/badge/platform-jvm-CDCDCD.svg?style=flat

[badge-linux]: http://img.shields.io/badge/platform-linux-CDCDCD.svg?style=flat