rootProject.name = "Cassy"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

includeBuild("tooling/build-logic")

include(":apps:android-pos")
include(":apps:desktop-pos")
include(":shared")
include(":shared:kernel")
include(":shared:masterdata")
include(":shared:sales")
include(":shared:inventory")
include(":tooling:sqlite-worker-init")
