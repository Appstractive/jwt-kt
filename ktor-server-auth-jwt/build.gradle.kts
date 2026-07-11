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

  linuxArm64()
  linuxX64()

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
      api(projects.jwtKt)
      implementation(libs.ktor.server.auth)
    }
  }
}
