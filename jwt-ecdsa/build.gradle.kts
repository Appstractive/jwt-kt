import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
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
    namespace = "com.appstractive.jwt.ecdsa"
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
          baseName = "JWT-ECDSA-KT"
          isStatic = true
        }
      }

  sourceSets {
    commonMain.dependencies { implementation(projects.jwtKt) }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.test.kotlin.coroutines)
    }

    wasmJsTest.dependencies { implementation(libs.kotlinx.browser) }
  }
}
