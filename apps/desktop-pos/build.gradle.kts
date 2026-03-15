plugins {
    alias(libs.plugins.cassy.kotlin.library)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

sourceSets {
    main {
        java.srcDir("src/jvmMain/kotlin")
        resources.srcDir("src/jvmMain/resources")
    }
}

dependencies {
    implementation(project(":shared:kernel"))
    implementation(project(":shared:masterdata"))
    implementation(project(":shared:sales"))
    implementation(project(":shared:inventory"))
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.datetime)
    testImplementation(libs.sqldelight.sqlite.driver)
}

compose.desktop {
    application {
        mainClass = "id.azureenterprise.cassy.desktop.MainKt"
        nativeDistributions {
            packageName = "Cassy"
            packageVersion = "0.1.0"
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
        }
    }
}
