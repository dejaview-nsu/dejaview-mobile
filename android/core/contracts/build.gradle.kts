plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.coroutines.core)
    api(project(":core:model"))
}
