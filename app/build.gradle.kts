plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
    id ("kotlin-parcelize")
}

android {
    namespace = "com.kosiso.lagosdevelopers"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kosiso.lagosdevelopers"
        minSdk = 24
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
        compose = true
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
    implementation(libs.androidx.constraintlayout)
//    implementation(libs.androidx.navigation.compose.jvmstubs)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



    // compose navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // compose constraint Layout
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.1.0")

// ViewModel support in Compose
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")

    // Kotlin Coroutines for ViewModel and StateFlow
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Hilt dependencies
    implementation ("com.google.dagger:hilt-android:2.56.2")
    kapt ("com.google.dagger:hilt-compiler:2.56.2")


    // lifecycle service
    implementation("androidx.lifecycle:lifecycle-service:2.9.2")


    // Room
    implementation ("androidx.room:room-runtime:2.7.2")
    kapt ("androidx.room:room-compiler:2.7.2")
    // Coroutines support for Room
    implementation ("androidx.room:room-ktx:2.7.2")

    implementation ("com.google.code.gson:gson:2.11.0")

    // coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")

    //paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")
    implementation("androidx.room:room-paging:2.7.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

//    implementation("org.jetbrains.kotlinx:kotlinx-parcelize:1.3.0")

}