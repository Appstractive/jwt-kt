﻿# JWT Kotlin Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/com.appstractive/jwt-kt?label=Maven%20Central)](https://central.sonatype.com/artifact/com.appstractive/jwt-kt)

![badge][badge-android]
![badge][badge-apple]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-win]
![badge][badge-linux]

A kotlin multiplatform library for creating, parsing, signing and verifying JWTs.

## Supported Algorithms

- HS256
- HS384
- HS512
- RS256
- RS384
- RS512
- PS256
- PS384
- PS512
- ES256
- ES384
- ES512

## Usage

### Installation

Gradle:

```
commonMain.dependencies { 
    implementation("com.appstractive:jwt-kt:1.2.0")
}
```

### Create JWT

```kotlin
val jwt: UnsignedJWT = jwt {
    claims {
        issuer = "example.com"
        subject = "me"
        audience = "api.example.com"
        expires(Clock.System.now() + 24.hours)
        notBefore()
        issuedAt()
        id = "123456"

        claim("double", 1.1)
        claim("long", 1L)
        claim("bool", true)
        claim("string", "test")
        claim("null", null)
        objectClaim("object") { put("key", JsonPrimitive("value")) }
        arrayClaim("list") {
            add(JsonPrimitive(0))
            add(JsonPrimitive(1))
            add(JsonPrimitive(2))
        }
    }
}
```

### Parse JWT

```kotlin
val jwt = JWT.from("eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNT...")

val userId = jwt.subject
```

### Sign JWT

For specific algorithms see:

- [HMAC](jwt-hmac/README.md)
- [RSA](jwt-rsa/README.md)
- [ECDSA](jwt-ecdsa/README.md)

### Verify JWT

```kotlin
val jwt = JWT.from("eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNT...")

val isValid = jwt.verify {
    // verify issuer
    issuer("example.com")
    // verify audience
    audience("api.example.com")
    // verify expiration
    expiresAt()
    // verify not before time
    notBefore()
}
```

For signature verification see:

- [HMAC](jwt-hmac/README.md)
- [RSA](jwt-rsa/README.md)
- [ECDSA](jwt-ecdsa/README.md)

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

[badge-js]: http://img.shields.io/badge/platform-js-f7df1e.svg?style=flat

[badge-win]: http://img.shields.io/badge/platform-win-357EC7.svg?style=flat

[badge-linux]: http://img.shields.io/badge/platform-linux-CDCDCD.svg?style=flat