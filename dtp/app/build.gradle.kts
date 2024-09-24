plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.mpower.dtp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mpower.dtp"
        minSdk = 29
        targetSdk = 34
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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

    // Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)

    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Feature module support for Fragments
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Android Gif Drawable
    implementation(libs.android.gif.drawable)

    // Ok Http
    implementation(libs.okhttp)

    // WaveFormSeekBar | Amplitude
    implementation(libs.waveformseekbar)
    implementation(libs.amplituda)

    // JTransforms
    implementation(libs.jtransforms)
    implementation(libs.jlargearrays)
    implementation(libs.commons.math3)
}