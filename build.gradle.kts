import java.util.Properties

plugins {
  alias(libs.plugins.multiplatform) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlinx.serialization) apply false
  alias(libs.plugins.kotlinx.binary.compatibility) apply false
}

group = "com.appstractive"

version = "1.2.0"

val localProps = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
  localPropertiesFile.inputStream().use { localProps.load(it) }
}

val signingKey by
    extra(
        localProps["signing.keyFile"]?.let { rootDir.resolve(it.toString()).readText() }
            ?: System.getenv("SIGNING_KEY")
            ?: "",
    )
val signingPassword by
    extra(
        localProps.getOrDefault(
            "signing.password",
            System.getenv("SIGNING_PASSWORD") ?: "",
        ),
    )
