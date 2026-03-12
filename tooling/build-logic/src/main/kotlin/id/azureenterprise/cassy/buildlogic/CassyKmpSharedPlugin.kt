package id.azureenterprise.cassy.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension

class CassyKmpSharedPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                (this as org.gradle.api.plugins.ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("android") {
                    namespace = "id.azureenterprise.cassy.shared"
                    compileSdk = 35
                    minSdk = 24
                }

                jvm("desktop")
            }
        }
    }
}
