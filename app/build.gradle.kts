plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.afinal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.afinal"
        minSdk = 34
        targetSdk = 36
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
        compose = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    //GOOGLE MAP
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation(libs.androidx.activity)

    //Navigation
    val nav_version = "2.9.5"
    implementation("androidx.navigation:navigation-fragment:${nav_version}")
    implementation("androidx.navigation:navigation-ui:${nav_version}")

    //Material Design
    implementation("com.google.android.material:material:1.12.0")

    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))

    // Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Authentication (nếu cần đăng nhập)
    implementation("com.google.firebase:firebase-auth")

    // Analytics (tuỳ chọn)
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //CUA NGUYEN TRI THANH
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.json:json:20231013") // để dùng JSONObject
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
}
