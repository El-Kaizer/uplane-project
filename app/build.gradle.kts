plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.uilover.project2262"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.uilover.project2262"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    composeOptions {
        // Set manual versi compiler yang kompatibel dengan Kotlin 1.9.22
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    buildFeatures {
        compose = true
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.compose.runtime:runtime-livedata:1.6.7")


    implementation(libs.firebase.database)
    implementation(libs.firebase.auth.ktx)
    implementation("com.google.firebase:firebase-firestore")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.accompanist:accompanist-pager:0.34.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.34.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}