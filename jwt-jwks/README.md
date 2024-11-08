# JWT Kotlin Multiplatform - JWKS (TODO)

[![Maven Central](https://img.shields.io/maven-central/v/com.appstractive/jwt-jwks-kt?label=Maven%20Central)](https://central.sonatype.com/artifact/com.appstractive/jwt-jwks-kt)

![badge][badge-android]
![badge][badge-apple]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-win]
![badge][badge-linux]

Verify JWTs using JSONWebKeySets.

## Usage

### Installation

Gradle:

TODO: Not yet published, requires JWK support from kotlin crypto library

```
commonMain.dependencies {
    implementation("com.appstractive:jwt-kt:1.0.2")
    implementation("com.appstractive:jwt-jwks-kt:1.0.2")
}
```

### Verify JWT

```kotlin
val jwt = JWT.from("eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNT...")

val isValid = jwt.verify {
    jwks {
        endpoint = "http://example.com/.well-known/jwt/jwks.json"
        cacheDuration = 1.minutes
    }
    
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
