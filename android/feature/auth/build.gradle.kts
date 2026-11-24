import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    buildFeatures.buildConfig = true
    val local = Properties()
    val settingsFile = rootProject.file("local.properties")
    if (settingsFile.isFile) settingsFile.inputStream().use(local::load)
    val origin = providers.gradleProperty("dejaviewApiOrigin").getOrElse(local.getProperty("dejaviewApiOrigin", ""))
    require(origin.isEmpty() || Regex("https://[a-zA-Z0-9.-]+(:[0-9]+)?").matches(origin)) {
        "dejaviewApiOrigin must be an HTTPS origin without a path, credentials or query"
    }
    defaultConfig.buildConfigField("String", "API_ORIGIN", "\"" + origin + "\"")
}

dependencies {
    implementation(libs.activity.compose)
    implementation(project(":core:security"))
    implementation(project(":core:network"))
    implementation(libs.serialization.json)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    add("testProdImplementation", libs.mockwebserver)
    add("testProdImplementation", libs.okhttp.tls)
}
