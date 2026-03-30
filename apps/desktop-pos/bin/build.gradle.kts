import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.cassy.kotlin.library)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

sourceSets {
    main {
        java.srcDir("src/jvmMain/kotlin")
        resources.srcDir("src/jvmMain/resources")
        resources.srcDir("../../assets")
    }
}

dependencies {
    implementation(project(":shared:kernel"))
    implementation(project(":shared:masterdata"))
    implementation(project(":shared:sales"))
    implementation(project(":shared:inventory"))
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material) // Required for some Icons.Default and ambiguity resolution
    implementation(compose.material3)
    implementation(compose.materialIconsExtended) // Full icon set
    implementation(compose.ui)
    implementation(libs.koin.core)
    implementation("io.insert-koin:koin-compose:${libs.versions.koin.get()}") {
        exclude(group = "org.jetbrains.compose.animation")
        exclude(group = "org.jetbrains.compose.foundation")
        exclude(group = "org.jetbrains.compose.material")
        exclude(group = "org.jetbrains.compose.runtime")
        exclude(group = "org.jetbrains.compose.ui")
    }
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.commons.csv)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.datetime)
    testImplementation(libs.sqldelight.sqlite.driver)
}

val desktopJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(17))
}

val desktopReleaseVersion = providers.gradleProperty("cassy.release.version").orElse("0.2.0-beta.1").get()
val desktopPackageVersion = providers.gradleProperty("cassy.package.version").orElse("0.2.0").get()
val desktopReleaseChannel = providers.gradleProperty("cassy.release.channel").orElse("beta").get()
val desktopDisplayName = providers.gradleProperty("cassy.release.displayName").orElse("Cassy Beta").get()
val desktopUpgradeUuid = "3d66aafe-2e8f-4f6e-a9cb-48d5564752d4"

tasks.withType<JavaExec>().configureEach {
    javaLauncher.set(desktopJavaLauncher)
    jvmArgs(
        "-Dcassy.release.version=$desktopReleaseVersion",
        "-Dcassy.runtime.channel=$desktopReleaseChannel",
        "-Dcassy.display.name=$desktopDisplayName"
    )
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
}

tasks.register<JavaExec>("smokeRun") {
    group = "compose desktop"
    description = "Runs the desktop app in smoke mode with JDK 17 and exits automatically."
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("id.azureenterprise.cassy.desktop.MainKt")
    javaLauncher.set(desktopJavaLauncher)
    args("--smoke-run")
}

compose.desktop {
    application {
        mainClass = "id.azureenterprise.cassy.desktop.MainKt"
        jvmArgs += listOf(
            "-Dcassy.release.version=$desktopReleaseVersion",
            "-Dcassy.runtime.channel=$desktopReleaseChannel",
            "-Dcassy.display.name=$desktopDisplayName"
        )
        nativeDistributions {
            packageName = "Cassy"
            packageVersion = desktopPackageVersion
            description = "Cassy Desktop POS untuk single-outlet retail dengan posture local-first."
            vendor = "Cassy"
            copyright = "Copyright (c) 2026 Cassy"
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            modules("java.sql")
            windows {
                iconFile.set(project.file("../../assets/icon/cassy-app-icon.ico"))
                perUserInstall = true
                dirChooser = true
                menuGroup = "Cassy"
                upgradeUuid = desktopUpgradeUuid
            }
        }
    }
}
