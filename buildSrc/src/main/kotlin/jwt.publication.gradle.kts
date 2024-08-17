plugins {
  signing
  `maven-publish`
  id("org.jetbrains.dokka")
}

val javadocJar by
    tasks.registering(Jar::class) {
      archiveClassifier.set("javadoc")
      from(tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml"))
    }

val publishing = extensions.getByName("publishing") as PublishingExtension

extensions.configure<PublishingExtension>("publishing") {
  publications.withType<MavenPublication> {
    artifact(javadocJar)

    pom {
      name.set(project.name)
      description.set("JWT creating, parsing, signing and verifying implementation for Kotlin Multiplatform")
      url.set("https://github.com/Appstractive/jwt-kt")

      scm {
        url.set("https://github.com/Appstractive/jwt-kt")
        connection.set("scm:git:https://github.com/Appstractive/jwt-kt.git")
        developerConnection.set("scm:git:https://github.com/Appstractive/jwt-kt.git")
        tag.set("HEAD")
      }

      issueManagement {
        system.set("GitHub Issues")
        url.set("https://github.com/Appstractive/jwt-kt/issues")
      }

      developers {
        developer {
          name.set("Andreas Schulz")
          email.set("dev@appstractive.com")
        }
      }

      licenses {
        license {
          name.set("The Apache Software License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          distribution.set("repo")
          comments.set("A business-friendly OSS license")
        }
      }
    }
  }
}

val signingTasks = tasks.withType<Sign>()

tasks.withType<AbstractPublishToMaven>().configureEach { dependsOn(signingTasks) }

extensions.configure<SigningExtension>("signing") {
  val signingKey: String by rootProject.extra
  val signingPassword: String by rootProject.extra

  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications)
}
