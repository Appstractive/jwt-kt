# JWT Kotlin Multiplatform - HMAC

[![Maven Central](https://img.shields.io/maven-central/v/com.appstractive/jwt-hmac-kt?label=Maven%20Central)](https://central.sonatype.com/artifact/com.appstractive/jwt-hmac-kt)

![badge][badge-android]
![badge][badge-apple]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-win]
![badge][badge-linux]

Sign and verify JWTs using HMAC algorithm.

## Supported Algorithms

- HS256
- HS384
- HS512

## Usage

### Installation

Gradle:

```
commonMain.dependencies { 
    implementation("com.appstractive:jwt-hmac-kt:1.0.0")
}
```

### Sign JWT

```kotlin
val jwt: UnsignedJWT = jwt {
    claims { issuer = "example.com" }
}
val mySecret = CryptographyRandom.nextBytes(64)

val signedJWT = jwt.sign {
    hs256 { secret = mySecret }
    // or with different hashing
    // hs384 { secret = mySecret }
    // hs512 { secret = mySecret }
}
```

### Verify JWT

```kotlin
val jwt = JWT.from("eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNT...")
val mySecret = CryptographyRandom.nextBytes(64)

val isValid = jwt.verify {
    hs256 { secret = mySecret }
    // or with different hashing
    // hs384 { secret = mySecret }
    // hs512 { secret = mySecret }
    
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
