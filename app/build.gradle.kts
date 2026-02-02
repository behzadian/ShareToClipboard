plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "no1.share.to.clipboard"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        create("DefaultSingingKey") {
            if (
                !project.hasProperty("STC_KEY_STORE_FILE_PATH") ||
                !project.hasProperty("STC_KEY_STORE_FILE_PASS") ||
                !project.hasProperty("STC_KEY_STORE_ALIAS_NAME") ||
                !project.hasProperty("STC_KEY_STORE_ALIAS_PASS")
            ) {
                throw GradleException(
                    """
                            Please define singing properties in ~/.gradle/gradle.properties like below:
                            STC_KEY_STORE_FILE_PATH=/path/to/key/store/file
                            STC_KEY_STORE_FILE_PASS=key-store-password
                            STC_KEY_STORE_ALIAS_NAME=key-alias-name
                            STC_KEY_STORE_ALIAS_PASS=key-alias-password
                            """.trimIndent()
                )
            }

            storeFile = file(project.property("STC_KEY_STORE_FILE_PATH") as String)
            storePassword = project.property("STC_KEY_STORE_FILE_PASS") as String
            keyAlias = project.property("STC_KEY_STORE_ALIAS_NAME") as String
            keyPassword = project.property("STC_KEY_STORE_ALIAS_PASS") as String
        }

    }

    defaultConfig {
        applicationId = "no1.share.to.clipboard"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs["DefaultSingingKey"]
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}