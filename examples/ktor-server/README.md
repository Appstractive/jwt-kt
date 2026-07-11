# JWT Kotlin Multiplatform - KTOR Server Example

This module contains a simple example for how to use the jwt-kt library for signing and verifying JWTs using an ECDSA key pair and JSON Web Keys.

## Run

Start the server:
```shell
.\gradlew :examples:ktor-server:jvmRun
```

### Test

#### JWKS Endpoint:
```shell
curl -v http://localhost:8080/.well-known/jwt/jwks.json
```

#### Login:
```shell
curl -v -X POST -H "Content-Type: application/json" http://localhost:8080/login -d '{"username":"username", "password":"password"}'
```

This will return an access token.

#### Authenticated Route

Replace `<TOKEN>` with the access token from the previous step:
```shell
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/hello
```

This should return an HTTPS 200 response. During the lifetime of the token (5 minutes) and an http 401 after expiry.
