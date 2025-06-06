plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"

}
android {
    namespace = "com.example.chatapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chatapp"
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
        compose = true
        viewBinding = true
        dataBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation (libs.androidx.constraintlayout)


    //noinspection UseTomlInstead
    implementation ("androidx.recyclerview:recyclerview:1.4.0")
    //noinspection UseTomlInstead
    implementation ("com.google.android.material:material:1.12.0")
    // Retrofit
    //noinspection UseTomlInstead
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    //noinspection UseTomlInstead
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    //noinspection UseTomlInstead
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //noinspection UseTomlInstead
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    // Converter cho JSON (Gson)
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    //noinspection UseTomlInstead
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    // OkHttp (Thường được Retrofit sử dụng nội bộ)
    //noinspection UseTomlInstead
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    //noinspection UseTomlInstead
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1") // hoặc phiên bản mới nhất
    //noinspection UseTomlInstead
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    //noinspection UseTomlInstead
    implementation("androidx.fragment:fragment-ktx:1.8.7")
    //noinspection UseTomlInstead
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")// Thay bằng phiên bản mới nhất

    //noinspection GradleDependency,UseTomlInstead
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    //noinspection GradleDependency,UseTomlInstead
    implementation("androidx.navigation:navigation-ui-ktx:2.7.")
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

}

