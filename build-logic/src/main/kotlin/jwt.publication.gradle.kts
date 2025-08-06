plugins {
    signing
    id("com.vanniktech.maven.publish.base")
    id("org.jetbrains.dokka")
}

val javadocJar = tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = rootProject.group.toString(),
        artifactId = project.name,
        version = rootProject.version.toString()
    )

    pom {
        name.set(project.name)
        description.set(
            "JWT creating, parsing, signing and verifying implementation for Kotlin Multiplatform"
        )
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

// javadocJar setup
// we have a single javadoc artifact which is used for all publications,
// and so we need to manually create task dependencies to make Gradle happy
tasks.withType<Sign>().configureEach { dependsOn(javadocJar) }
tasks.withType<AbstractPublishToMaven>().configureEach { mustRunAfter(tasks.withType<Sign>()) }
publishing.publications.withType<MavenPublication>().configureEach { artifact(javadocJar) }

extensions.configure<SigningExtension>("signing") {
    val signingKey: String by rootProject.extra
    val signingPassword: String by rootProject.extra

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
