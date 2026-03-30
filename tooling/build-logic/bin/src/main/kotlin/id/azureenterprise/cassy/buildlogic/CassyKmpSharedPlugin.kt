package id.azureenterprise.cassy.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension

class CassyKmpSharedPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(17)
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                // In AGP 8.11+, KotlinMultiplatformAndroidLibraryExtension is the correct type.
                // We use "androidLibrary" as the extension name as it is the one currently registered.
                (this as org.gradle.api.plugins.ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
                    val projectPath = path.replace(":", ".").removePrefix(".")
                    namespace = if (projectPath == "shared") {
                        "id.azureenterprise.cassy.shared"
                    } else {
                        "id.azureenterprise.cassy.${projectPath.substringAfter("shared.")}"
                    }

                    compileSdk = 35
                    minSdk = 24
                }

                jvm("desktop")

                targets.withType(KotlinJvmTarget::class.java).configureEach {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                        freeCompilerArgs.add("-Xexpect-actual-classes")
                    }
                }
            }
        }
    }
}
