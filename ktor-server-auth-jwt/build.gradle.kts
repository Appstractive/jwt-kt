import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlinx.serialization)
  id("jwt.publication")
}

group = rootProject.group

version = rootProject.version

kotlin {
  androidTarget {
    publishAllLibraryVariants()
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
  }

  jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

  linuxArm64()
  linuxX64()

  mingwX64()

  js()
  @OptIn(ExperimentalWasmDsl::class) wasmJs()

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
          baseName = "Ktor Server Auth JWT"
          isStatic = true
        }
      }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.jwtKt)
      implementation(libs.ktor.server.auth)
    }

    commonTest.dependencies { }

    androidMain.dependencies {  }

    jvmMain.dependencies {  }

    appleMain.dependencies {  }

    linuxMain.dependencies {  }

    jsMain.dependencies {  }
  }
}

android {
  namespace = group.toString()
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
  sourceSets["main"].apply { manifest.srcFile("src/androidMain/AndroidManifest.xml") }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
