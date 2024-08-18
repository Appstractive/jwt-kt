# JWT Kotlin Multiplatform - JWKS (TODO)

[![Maven Central](https://img.shields.io/maven-central/v/com.appstractive/jwt-jwks-kt?label=Maven%20Central)](https://central.sonatype.com/artifact/com.appstractive/jwt-jwks-kt)

Verify JWTs using JSONWebKeySets.

## Usage

### Installation

Gradle:

TODO: Not yet published, requires JWK support from kotlin crypto library

```
commonMain.dependencies { 
    implementation("com.appstractive:jwt-jwks-kt:1.0.0")
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
