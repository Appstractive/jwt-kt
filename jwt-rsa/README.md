# JWT Kotlin Multiplatform - RSA

[![Maven Central](https://img.shields.io/maven-central/v/com.appstractive/jwt-rsa-kt?label=Maven%20Central)](https://central.sonatype.com/artifact/com.appstractive/jwt-rsa-kt)

Sign and verify JWTs using RSA algorithms.

## Supported Algorithms

- RS256
- RS384
- RS512
- PS256
- PS384
- PS512

## Usage

### Installation

Gradle:

```
commonMain.dependencies { 
    implementation("com.appstractive:jwt-rsa-kt:1.0.0")
}
```

### Sign JWT

#### PKCS1

```kotlin
val provider = CryptographyProvider.Default
val pkcs1: RSA.PSS = rovider.get(RSA.PKCS1)

val jwt: UnsignedJWT = jwt {
    claims { issuer = "example.com" }
}
val keys = pkcs1.keyPairGenerator(digest = SHA256).generateKey()
val privateKey = keys.privateKey.encodeTo(RSA.PrivateKey.Format.PEM.Generic)

val signedJWT = jwt.sign {
    // PKCS 1
    rs256 { pem(privateKey) }
    // or with different hashing
    // rs384 { pem(privateKey) }
    // rs512 { pem(privateKey) }
}
```

#### PSS

```kotlin
val provider = CryptographyProvider.Default
val pss: RSA.PSS = rovider.get(RSA.PSS)

val jwt: UnsignedJWT = jwt {
    claims { issuer = "example.com" }
}
val keys = pss.keyPairGenerator(digest = SHA256).generateKey()
val privateKey = keys.privateKey.encodeTo(RSA.PrivateKey.Format.PEM.Generic)

val signedJWT = jwt.sign {
    // PSS
    ps256 { pem(privateKey) }
    // or with different hashing
    // ps384 { pem(privateKey) }
    // ps512 { pem(privateKey) }
}
```

### Verify JWT

```kotlin
val provider = CryptographyProvider.Default
val pkcs1: RSA.PSS = rovider.get(RSA.PKCS1)
val pss: RSA.PSS = rovider.get(RSA.PSS)

val jwt = JWT.from("eyJraWQiOiJzLWRmZmZkMDJlLTlhNDItNDQzMC1hNT...")
val pkcs1Keys = pss.keyPairGenerator(digest = SHA256).generateKey()
val pkcs1PublicKey = pkcs1Keys.publicKey.encodeTo(RSA.PublicKey.Format.PEM.Generic)
val pssKeys = pss.keyPairGenerator(digest = SHA256).generateKey()
val pssPublicKey = pssKeys.publicKey.encodeTo(RSA.PublicKey.Format.PEM.Generic)

val isValid = jwt.verify {
    // PKCS 1
    rs256 { pem(pkcs1PublicKey) }
    // or with different hashing
    // rs384 { pem(publicKey) }
    // rs512 { pem(publicKey) }

    // PSS
    ps256 { pem(pssPublicKey) }
    // or with different hashing
    // ps384 { pem(publicKey) }
    // ps512 { pem(publicKey) }

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
