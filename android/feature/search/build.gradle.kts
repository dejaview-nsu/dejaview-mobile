plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    jacoco
}

dependencies {
    implementation(project(":feature:search-api"))
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.compose.material3)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.register<JacocoCoverageVerification>("verifySearchCoverage") {
    dependsOn("testDemoDebugUnitTest")
    val demoCoverage = layout.buildDirectory.file("jacoco/testDemoDebugUnitTest.exec")
    executionData(demoCoverage)
    classDirectories.setFrom(
        layout.buildDirectory.dir("tmp/kotlin-classes/demoDebug").map { output ->
            output.asFileTree.matching {
                include("**/SearchViewModel*.class", "**/PendingActionStore.class", "**/SearchResultStore.class")
            }
        },
    )
    doFirst {
        check(demoCoverage.get().asFile.isFile) { "The demo search coverage data was not produced" }
        check(!classDirectories.isEmpty) { "The demo search classes were not found" }
    }
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
