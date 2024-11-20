import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  application
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
}

group = "com.appstractive.jwt"

version = "1.0.3"

kotlin {
  jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

  linuxX64().apply {
    binaries {
      executable {
        entryPoint = "com.appstractive.main"
      }
    }
  }

  listOf(
          macosX64(),
          macosArm64(),
      )
      .forEach {
        it.binaries {
          framework {
            baseName = "JWT-KT"
            isStatic = true
          }

          executable {
            entryPoint = "com.appstractive.main"
          }
        }
      }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.jwtHmacKt)
      implementation(projects.ktorServerAuthJwt)

      implementation(libs.kotlin.datetime)

      implementation(libs.ktor.serialization.json)
      implementation(libs.ktor.server.core)
      implementation(libs.ktor.server.cio)
      implementation(libs.ktor.server.contentnegotiation)
      implementation(libs.ktor.server.auth)
    }

    commonTest.dependencies {}

    jvmMain.dependencies {}

    appleMain.dependencies {}

    linuxMain.dependencies {}

    mingwMain.dependencies {}
  }
}

application { mainClass.set("com.appstractive.ApplicationKt") }
