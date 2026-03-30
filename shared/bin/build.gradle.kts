plugins {
    alias(libs.plugins.cassy.kmp.shared)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":shared:kernel"))
            api(project(":shared:masterdata"))
            api(project(":shared:sales"))
            api(project(":shared:inventory"))

            implementation(libs.koin.core)
            implementation("io.insert-koin:koin-compose:${libs.versions.koin.get()}") {
                exclude(group = "org.jetbrains.compose.animation")
                exclude(group = "org.jetbrains.compose.foundation")
                exclude(group = "org.jetbrains.compose.material")
                exclude(group = "org.jetbrains.compose.runtime")
                exclude(group = "org.jetbrains.compose.ui")
            }

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
