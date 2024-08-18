enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "jwt-kotlin-multiplatform"

include(":jwt", ":jwt-hmac", ":jwt-rsa", ":jwt-ecdsa", ":jwt-jwks", ":ktor-server-auth-jwt", ":examples:ktor-server")

project(":jwt").name = "jwt-kt"
project(":jwt-hmac").name = "jwt-hmac-kt"
project(":jwt-rsa").name = "jwt-rsa-kt"
project(":jwt-ecdsa").name = "jwt-ecdsa-kt"
project(":jwt-jwks").name = "jwt-jwks-kt"
