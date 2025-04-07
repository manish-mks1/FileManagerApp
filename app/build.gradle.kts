plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.lufick.files"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lufick.files"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    viewBinding {
        enable = true
    }
}

dependencies {



    implementation ("com.mikepenz:fastadapter:5.6.0")
//    implementation ("com.mikepenz:fastadapter-extensions-select:5.6.0")
    implementation ("com.mikepenz:fastadapter-extensions-binding:5.6.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}