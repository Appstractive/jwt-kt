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
    namespace = group.toString()
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
          baseName = "JWT-KT"
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
      implementation(libs.kotlin.serialization)
      implementation(libs.kotlin.serialization.json)
      implementation(libs.kotlin.datetime)
      api(libs.crypto)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.test.kotlin.coroutines)
    }

    androidMain.dependencies { implementation(libs.crypto.provider.optimal) }

    jvmMain.dependencies { implementation(libs.crypto.provider.optimal) }

    appleMain.dependencies { implementation(libs.crypto.provider.optimal) }

    linuxMain.dependencies { implementation(libs.crypto.provider.optimal) }

    mingwMain.dependencies { implementation(libs.crypto.provider.optimal) }

    jsMain.dependencies { implementation(libs.crypto.provider.optimal) }

    wasmJsMain.dependencies { implementation(libs.crypto.provider.optimal) }
  }
}
