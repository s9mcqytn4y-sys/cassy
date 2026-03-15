plugins {
    id("cassy.kmp.shared")
    alias(libs.plugins.sqlDelight)
}

sqldelight {
    databases {
        create("InventoryDatabase") {
            packageName.set("id.azureenterprise.cassy.inventory.db")
            module(project(":tooling:sqlite-worker-init"))
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:kernel"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            api(libs.sqldelight.runtime)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
    }
}
