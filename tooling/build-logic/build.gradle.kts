plugins {
    `kotlin-dsl`
}

group = "id.azureenterprise.cassy.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("cassyKmpShared") {
            id = "cassy.kmp.shared"
            implementationClass = "id.azureenterprise.cassy.buildlogic.CassyKmpSharedPlugin"
        }
        register("cassyKotlinLibrary") {
            id = "cassy.kotlin.library"
            implementationClass = "id.azureenterprise.cassy.buildlogic.CassyKotlinLibraryPlugin"
        }
    }
}
