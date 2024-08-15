import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlinx.serialization)
}

group = "com.appstractive.jwt-jwks"

version = rootProject.version

kotlin {
  androidTarget {
    publishAllLibraryVariants()
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
  }

  jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

  mingwX64()
  linuxArm64()
  linuxX64()

  js()

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
    commonMain.dependencies {
      implementation(projects.jwt)
      implementation(projects.jwtRsa)
      implementation(projects.jwtEcdsa)

      implementation(libs.kotlin.coroutines)
      implementation(libs.kotlin.datetime)

      implementation(libs.ktor.serialization.json)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.serialization)
      implementation(libs.ktor.client.json)
      implementation(libs.ktor.client.contentnegotiation)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.test.kotlin.coroutines)
    }

    androidMain.dependencies { implementation(libs.crypto.jdk) }

    jvmMain.dependencies { implementation(libs.crypto.jdk) }

    appleMain.dependencies { implementation(libs.crypto.openssl3) }

    linuxMain.dependencies { implementation(libs.crypto.openssl3) }

    mingwMain.dependencies { implementation(libs.crypto.openssl3) }

    jsMain.dependencies { implementation(libs.crypto.webcrypto) }
  }
}

android {
  namespace = "com.appstractive.jwt.jwks"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
  sourceSets["main"].apply { manifest.srcFile("src/androidMain/AndroidManifest.xml") }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
