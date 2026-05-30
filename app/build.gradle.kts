plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.pab.deucepadelapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pab.deucepadelapp"
        minSdk = 28
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ==========================================
    // MANTRA RETROFIT UNTUK KONEKSI KE SPRING BOOT
    // ==========================================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 🔥 TAMBAHKAN BARIS INI JIRR (Wajib biar RequestBody versi baru terbaca)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ==========================================
    // MANTRA GLIDE STANDAR (Sangat Aman & Ringan)
    // ==========================================
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ==========================================
    // LIBRARY LOKASI UNTUK FITUR NEAR ME RIIL
    // ==========================================
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}