plugins {
    id("cassy.kmp.shared")
    alias(libs.plugins.sqlDelight)
}

sqldelight {
    databases {
        create("CassyDatabase") {
            packageName.set("id.azureenterprise.cassy.db")
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            api(libs.koin.core)
            api(libs.sqldelight.runtime)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
