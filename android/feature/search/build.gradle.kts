plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(project(":feature:search-api"))
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.compose.material3)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
