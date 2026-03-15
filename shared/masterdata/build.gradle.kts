plugins {
    id("cassy.kmp.shared")
    alias(libs.plugins.sqlDelight)
}

sqldelight {
    databases {
        create("MasterDataDatabase") {
            packageName.set("id.azureenterprise.cassy.masterdata.db")
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:kernel"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            api(libs.sqldelight.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
    }
}
