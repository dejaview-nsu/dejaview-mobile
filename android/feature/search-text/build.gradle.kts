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
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    add("prodImplementation", project(":core:network"))
    add("prodImplementation", libs.serialization.json)
    add("testProdImplementation", libs.junit)
    add("testProdImplementation", libs.coroutines.test)
    add("testProdImplementation", libs.mockwebserver)
    add("testProdImplementation", libs.okhttp.tls)
    val testDemoImplementation by configurations
    testDemoImplementation(libs.junit)
    testDemoImplementation(libs.coroutines.test)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.register<JacocoCoverageVerification>("verifyReferenceSearchCoverage") {
    dependsOn("testDemoDebugUnitTest")
    val demoCoverage = layout.buildDirectory.file("jacoco/testDemoDebugUnitTest.exec")
    executionData(demoCoverage)
    classDirectories.setFrom(
        layout.buildDirectory.dir("tmp/kotlin-classes/demoDebug").map { output ->
            output.asFileTree.matching {
                include(
                    "**/reference/data/**",
                    "**/reference/domain/**",
                    "**/reference/presentation/ReferenceSearchViewModel*.class",
                    "**/reference/presentation/ReferenceSearchState.class",
                )
            }
        },
    )
    doFirst {
        check(demoCoverage.get().asFile.isFile) { "The demo reference search coverage data was not produced" }
        check(!classDirectories.isEmpty) { "The demo reference search classes were not found" }
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
