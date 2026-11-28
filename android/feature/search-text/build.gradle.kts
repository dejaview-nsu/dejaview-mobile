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
    implementation(libs.activity.compose)
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

fun verifyTextSearchCoverage(
    flavor: String,
    includes: List<String>,
) {
    val unitTest = "test${flavor}DebugUnitTest"
    val variant = flavor.lowercase() + "Debug"
    tasks.register<JacocoCoverageVerification>("verify${flavor}TextSearchCoverage") {
        dependsOn(unitTest)
        val coverage = layout.buildDirectory.file("jacoco/$unitTest.exec")
        executionData(coverage)
        classDirectories.setFrom(
            layout.buildDirectory.dir("tmp/kotlin-classes/$variant").map { output ->
                output.asFileTree.matching { include(includes) }
            },
        )
        doFirst {
            check(coverage.get().asFile.isFile) { "The $variant text search coverage data was not produced" }
            check(!classDirectories.isEmpty) { "The $variant text search classes were not found" }
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
}

verifyTextSearchCoverage(
    "Demo",
    listOf(
        "**/FakeTextSearch*.class",
        "**/text/domain/**",
        "**/TextSearchViewModel*.class",
        "**/TextSearchState.class",
    ),
)
verifyTextSearchCoverage("Prod", listOf("**/HttpTextSearch*.class"))
