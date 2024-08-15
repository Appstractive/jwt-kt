# WIP
![badge][badge-android]
![badge][badge-ios]
![badge][badge-macos]
![badge][badge-tvos]
![badge][badge-watchos]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-win]
![badge][badge-linux]

A kotlin multiplatform library for creating, parsing, signing and verifying JWTs.

## Supported Algorithms

- HS256
- HS384
- HS512
- PS256
- PS384
- PS512
- RS256
- RS384
- RS512
- ES256
- ES384
- ES512

## Usage

### Installation

Gradle (TODO not yet published):

```
implementation("com.appstractive:jwt-kt:1.0.0")
```

### Create JWT

```kotlin
val jwt: UnsignedJWT = jwt {
    claims {
        issuer = "example.com"
        subject = "me"
        audience = "api.example.com"
        expiresAt = Clock.System.now() + 24.hours
        notBeforeNow()
        issuedNow()
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

```kotlin
val jwt: UnsignedJWT = jwt {
    claims { issuer = "example.com" }
}
val mySecret = CryptographyRandom.nextBytes(64)

jwt.sign {
    hs256 { secret = mySecret }
}
```

### Verify JWT

```kotlin
val jwt = JWT.from("eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNT...")
val mySecret = CryptographyRandom.nextBytes(64)

val isValid = jwt.verify {
    hs256 { secret = mySecret }
    // verify specific issuer
    issuer = "example.com"
    // verify specific audience
    audience = "api.example.com"
    // verify expiration
    expiresAt()
    // verify not before time
    notBefore()
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
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-macos]: http://img.shields.io/badge/platform-macos-111111.svg?style=flat
[badge-tvos]: http://img.shields.io/badge/platform-tvos-808080.svg?style=flat
[badge-watchos]: http://img.shields.io/badge/platform-watchos-808080.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-CDCDCD.svg?style=flat
[badge-win]: http://img.shields.io/badge/platform-win-CDCDCD.svg?style=flat
[badge-linux]: http://img.shields.io/badge/platform-linux-CDCDCD.svg?style=flat