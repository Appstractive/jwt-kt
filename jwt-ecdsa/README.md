# JWT Kotlin Multiplatform - ECDSA

[![Maven Central](https://img.shields.io/maven-central/v/com.appstractive/jwt-ecdsa-kt?label=Maven%20Central)](https://central.sonatype.com/artifact/com.appstractive/jwt-ecdsa-kt)

![badge][badge-android]
![badge][badge-apple]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-win]
![badge][badge-linux]

Sign and verify JWTs using ECDSA algorithm.

## Supported Algorithms

- EC256
- EC384
- EC512

## Usage

### Installation

Gradle:

```
commonMain.dependencies { 
    implementation("com.appstractive:jwt-ecdsa-kt:1.0.0")
}
```

### Sign JWT

```kotlin
val jwt: UnsignedJWT = jwt {
    claims { issuer = "example.com" }
}
val keys = CryptographyProvider.Default.get(ECDSA).keyPairGenerator(curve).generateKey()
val privateKey = keys.privateKey.encodeTo(EC.PrivateKey.Format.PEM)

val signedJWT = jwt.sign {
    ec256 { secret = pem(privateKey) }
    // or with different hashing
    // ec384 { secret = pem(privateKey) }
    // ec512 { secret = pem(privateKey) }
}
```

### Verify JWT

```kotlin
val jwt = JWT.from("eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNT...")
val keys = CryptographyProvider.Default.get(ECDSA).keyPairGenerator(curve).generateKey()
val publicKey = keys.publicKey.encodeTo(EC.PublicKey.Format.PEM)

val isValid = jwt.verify {
    ec256 { secret = pem(publicKey) }
    // or with different hashing
    // ec384 { secret = pem(publicKey) }
    // ec512 { secret = pem(publicKey) }

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
