import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.spotless)
}

val javaTarget = libs.versions.java.get()
val compileApi =
    libs.versions.compile.sdk
        .get()
        .toInt()
val minApi =
    libs.versions.min.sdk
        .get()
        .toInt()
val hiltRuntime = libs.hilt.android
val hiltCompiler = libs.hilt.compiler

subprojects {
    val featureImplementation = path.startsWith(":feature:") && path != ":feature:search-api"
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            namespace = "ru.dejaview" + project.path.replace(':', '.').replace("-", "")
            compileSdk = compileApi
            defaultConfig.minSdk = minApi
            if (featureImplementation) {
                buildFeatures.compose = true
                flavorDimensions += "backend"
                productFlavors {
                    create("demo") { dimension = "backend" }
                    create("prod") { dimension = "backend" }
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.toVersion(javaTarget)
                targetCompatibility = JavaVersion.toVersion(javaTarget)
            }
        }
    }
    if (featureImplementation) {
        plugins.withId("com.android.library") {
            dependencies {
                add("implementation", project(":core:contracts"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:navigation"))
                add("implementation", project(":core:ui"))
            }
        }
        plugins.withId("com.google.devtools.ksp") {
            dependencies.add("ksp", hiltCompiler)
        }
        plugins.withId("com.google.dagger.hilt.android") {
            dependencies.add("implementation", hiltRuntime)
        }
    }
    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaTarget))
        }
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.toVersion(javaTarget)
            targetCompatibility = JavaVersion.toVersion(javaTarget)
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaTarget))
        }
    }
}

spotless {
    kotlin {
        target("app/src/**/*.kt", "core/*/src/**/*.kt", "feature/*/src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target("*.gradle.kts", "app/*.gradle.kts", "core/*/*.gradle.kts", "feature/*/*.gradle.kts", "build-logic/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

apply(from = "build-logic/architecture.gradle.kts")
