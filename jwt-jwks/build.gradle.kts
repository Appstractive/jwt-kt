import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.kotlinx.binary.compatibility)
  id("jwt.publication")
}

group = rootProject.group

version = rootProject.version

kotlin {
  jvmToolchain(17)

  android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    namespace = "com.appstractive.jwt.jwks"
  }

  jvm()

  mingwX64()
  linuxArm64()
  linuxX64()

  js {
    browser()
    nodejs()
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    nodejs()
  }

  listOf(
      iosX64(),
      iosArm64(),
      iosSimulatorArm64(),
      macosX64(),
      macosArm64(),
      tvosX64(),
      tvosArm64(),
      tvosSimulatorArm64(),
      watchosX64(),
      watchosArm64(),
      watchosSimulatorArm64(),
  )
      .forEach {
        it.binaries.framework {
          baseName = "JWT-JWKS-KT"
          isStatic = true
        }
      }

  sourceSets {
    all {
      languageSettings {
        optIn("kotlin.time.ExperimentalTime")
      }
    }

    commonMain.dependencies {
      implementation(projects.jwtKt)
      implementation(projects.jwtRsaKt)
      implementation(projects.jwtEcdsaKt)

      implementation(libs.kotlin.coroutines)
      implementation(libs.kotlin.datetime)

      implementation(libs.crypto.provider.base)

      implementation(libs.ktor.serialization.json)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.serialization)
      implementation(libs.ktor.client.json)
      implementation(libs.ktor.client.contentnegotiation)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.test.kotlin.coroutines)
      implementation(libs.test.ktor.client.mock)
    }

    androidMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    appleMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    linuxMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    mingwMain.dependencies {
      implementation(libs.ktor.client.win)
    }

    jsMain.dependencies {
      implementation(libs.ktor.client.js)
    }

    wasmJsTest.dependencies { implementation(libs.kotlinx.browser) }
  }
}
