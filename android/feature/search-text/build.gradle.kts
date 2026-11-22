plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(project(":feature:search-api"))
    val demoImplementation by configurations
    demoImplementation(libs.lifecycle.runtime.compose)
    demoImplementation(libs.lifecycle.viewmodel.compose)
}
