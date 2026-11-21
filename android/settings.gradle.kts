pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DejaView"
include(
    ":app",
    ":core:contracts",
    ":core:media",
    ":core:model",
    ":core:navigation",
    ":core:network",
    ":core:security",
    ":core:storage",
    ":core:testing",
    ":core:ui",
    ":feature:auth",
    ":feature:history",
    ":feature:library",
    ":feature:movies",
    ":feature:profile",
    ":feature:search",
    ":feature:search-api",
    ":feature:search-audio",
    ":feature:search-image",
    ":feature:search-text",
    ":feature:search-video",
)
