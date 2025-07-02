import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
}

group = "com.appstractive.jwt"

version = "1.2.0"

kotlin {
  jvm {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    binaries {
      executable {
        mainClass.set("com.appstractive.ApplicationKt")
      }
    }
  }

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
    all {
      languageSettings {
        optIn("kotlin.time.ExperimentalTime")
      }
    }

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
