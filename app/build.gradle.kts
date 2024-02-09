plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.mikepenz.aboutlibraries.plugin") version "10.5.2"
}

android {
    namespace = "com.identic.fluentforge"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.identic.fluentforge"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }

        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            /*applicationVariants.all { variant ->
                variant.outputs.all { output ->
                    output.outputFile = "FluentForge-v${variant.versionName}.apk"
                }
            }*/
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Android core components.
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
   // implementation("com.android.tools:r8:8.2.42")

    // Jetpack compose.
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Testing components.
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    // Android testing components.
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // debug components
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // System UI bars
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.17.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.palette:palette-ktx:1.0.0")
    // Recycler View for reader.
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // Material theme for main activity.
    implementation("com.google.android.material:material:1.11.0")
    // Android 12+ splash API.
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Gson JSON library.
    implementation("com.google.code.gson:gson:2.10.1")
    // Moshi library
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.49")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    ksp("com.google.dagger:hilt-compiler:2.49")
    ksp("androidx.hilt:hilt-compiler:1.0.0")

    // Jsoup HTML Parser.
    implementation("org.jsoup:jsoup:1.16.1")

    // Lottie animations.
    implementation("com.airbnb.android:lottie-compose:4.1.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Open Source Libraries Screen.
    implementation("com.mikepenz:aboutlibraries-core:10.5.2")
    implementation("com.mikepenz:aboutlibraries-compose:10.5.2")
    // Swipe actions.
    implementation("me.saket.swipe:swipe:1.2.0")
    // Crash Handler.
    implementation("cat.ereza:customactivityoncrash:2.4.0")
    // Kotlin reflect API.
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")


    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coroutine Lifecycle Scopes
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Media3
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-session:1.2.0")

    // Glide
    //implementation("com.github.bumptech.glide:glide:4.15.1")

    // Access old API
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.25.1")

    implementation("androidx.legacy:legacy-support-v4:1.0.0") // Needed MediaSessionCompat.Token

    // Material icons
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
}