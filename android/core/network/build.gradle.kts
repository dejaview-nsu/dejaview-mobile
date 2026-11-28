plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    api(libs.okhttp)
    implementation(libs.serialization.json)
    implementation(project(":core:contracts"))
    implementation(project(":core:model"))
}
