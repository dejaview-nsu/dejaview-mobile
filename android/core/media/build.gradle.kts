plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    testImplementation(libs.junit)
    implementation(project(":core:contracts"))
    implementation(project(":core:model"))
}

android.defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
