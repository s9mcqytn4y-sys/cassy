plugins {
    id("cassy.kmp.shared")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:kernel"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
    }
}
