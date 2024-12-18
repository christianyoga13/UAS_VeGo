plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.uts_vego"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.uts_vego"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
}
dependencies {
    // Core AndroidX libraries
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")

    // Firebase dependencies (with Firebase BOM for version management)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth:21.1.0") // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore:24.5.0") // Firestore
    implementation("com.google.firebase:firebase-storage:20.3.0") // Firebase Storage
    implementation("com.google.firebase:firebase-analytics:21.1.0") // Firebase Analytics (optional)
    implementation("com.google.firebase:firebase-database-ktx:20.0.5")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Compose dependencies (ensure version consistency)
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.material3:material3:1.0.1")  // Stable Material3 version
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.4.0")
    implementation("androidx.compose.foundation:foundation-layout:1.4.0")
    implementation("androidx.compose.foundation:foundation:1.4.0")

    // Lifecycle and ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.junit:junit:1.1.5")
    androidTestImplementation("androidx.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:1.4.0"))
    androidTestImplementation("androidx.compose.ui:test-junit4:1.4.0")

    // Debugging dependencies
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")
    debugImplementation("androidx.compose.ui:test-manifest:1.4.0")

    // Miscellaneous dependencies
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.accompanist:accompanist-pager:0.30.1")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.30.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("androidx.activity:activity-ktx:1.6.1")
}
