plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.soft77reload.iptv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.soft77reload.iptv"
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

    buildFeatures {
        viewBinding = true
    }

    viewBinding {
        enable = true
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

    //
    implementation("com.squareup.picasso:picasso:2.71828")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // gson converter

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // scalar

    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")

    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.3")


    //VLC

    // VLC
    implementation("org.videolan.android:libvlc-all:3.1.12")
}