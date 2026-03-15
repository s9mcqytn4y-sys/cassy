import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.jetbrainsCompose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.sqlDelight).apply(false)
    alias(libs.plugins.detekt)
    alias(libs.plugins.cassy.kmp.shared).apply(false)
}

rootProject.file(".gradle/sqlite-native").mkdirs()
System.setProperty("org.sqlite.tmpdir", rootProject.file(".gradle/sqlite-native").absolutePath)

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
}
