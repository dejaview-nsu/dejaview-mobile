plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    jacoco
}

dependencies {
    api(project(":core:model"))
    api(libs.serialization.json)
    testImplementation(libs.junit)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            // Kotlin serialization's generated serializer classes.
            exclude("**/*\$\$serializer.class")
        },
    )
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.85".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
