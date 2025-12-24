plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
}

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "com.muratcangzm.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder { }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val xcfName = "sharedKit"
    iosX64 { binaries.framework { baseName = xcfName; isStatic = true } }
    iosArm64 { binaries.framework { baseName = xcfName; isStatic = true } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName; isStatic = true } }

    sourceSets {
        commonMain.dependencies {
            // Re-export (composeApp sadece :shared görsün diye api kullan)
            api(project(":shared:core:common"))
            api(project(":shared:core:network"))
            api(project(":shared:core:database"))
            api(project(":shared:core:data"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.runner)
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.test.core)
        }
    }
}
