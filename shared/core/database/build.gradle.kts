plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.androidLint)
}

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "com.muratcangzm.shared.core.database"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:core:common"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutinesExtensions)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.androidDriver)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

sqldelight {
    databases {
        create("NervaDatabase") {
            packageName.set("com.muratcangzm.database")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/com/muratcangzm/database/schema"))
        }
    }
}
