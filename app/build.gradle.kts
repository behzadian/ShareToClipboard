plugins {
    alias(libs.plugins.android.application)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "no1.share.to.clipboard"
    compileSdk = 37

    val keystorePath = findProperty("STC_KEY_STORE_FILE_PATH") as String?
    if (keystorePath != null) {
        signingConfigs {
            create("DefaultSigningKey") {
                storeFile = file(signingProperty("STC_KEY_STORE_FILE_PATH"))
                storePassword = signingProperty("STC_KEY_STORE_FILE_PASS")
                keyAlias = signingProperty("STC_KEY_STORE_ALIAS_NAME")
                keyPassword = signingProperty("STC_KEY_STORE_ALIAS_PASS")
            }

        }
    } else {
        print("STC_KEY_STORE_FILE_PATH is not defined.")
    }

    defaultConfig {
        applicationId = "no1.share.to.clipboard"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (keystorePath != null) {
            signingConfig = signingConfigs["DefaultSigningKey"]
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Werror")
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

var signingKeysExceptionMessage = """
    Please define signing properties in ~/.gradle/gradle.properties like below:
    STC_KEY_STORE_FILE_PATH=/path/to/key/store/file
    STC_KEY_STORE_FILE_PASS=key-store-password
    STC_KEY_STORE_ALIAS_NAME=key-alias-name
    STC_KEY_STORE_ALIAS_PASS=key-alias-password
    """.trimIndent()
fun Project.signingProperty(propertyName: String): String {
    if (!hasProperty(propertyName)) {
        throw GradleException(
            signingKeysExceptionMessage.replace(propertyName, "this line -> $propertyName")
        )
    }
    return this.property(propertyName) as String
}