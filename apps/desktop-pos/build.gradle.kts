import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
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
    implementation(compose.material3)
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
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.datetime)
    testImplementation(libs.sqldelight.sqlite.driver)
}

val desktopJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher.set(desktopJavaLauncher)
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
        nativeDistributions {
            packageName = "Cassy"
            packageVersion = "0.1.0"
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            windows {
                iconFile.set(project.file("../../assets/icon/cassy-app-icon.ico"))
            }
        }
    }
}
