import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleGmsGoogleServices)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}

android {
    val secretsPropertiesFile = rootProject.file("secrets.properties")
    val secrets = Properties()
    if (secretsPropertiesFile.exists()) {
        secrets.load(secretsPropertiesFile.inputStream())
    }
    val mapsApiKey = secrets["MAPS_API_KEY"]?.toString() ?: ""

    buildFeatures {
        buildConfig = true
    }
    namespace = "com.example.deces"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.deces"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey}\"")
        }
        debug {
            isDebuggable = true  // Ovdje možete omogućiti generiranje BuildConfig
            buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey}\"")
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
dependencies {
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.activity.compose.v172)
    implementation(platform(libs.androidx.compose.bom.v20241000))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
