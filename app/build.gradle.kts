import java.util.Properties

val secretsProperties = Properties()
val secretsPropertiesFile = rootProject.file("secrets.properties")
if (secretsPropertiesFile.exists()) {
    secretsPropertiesFile.inputStream().use { secretsProperties.load(it) }
}

val mapsApiKey: String = secretsProperties.getProperty("MAPS_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.topmortar.topmortarsales"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.topmortar.topmortarsales"
        minSdk = 24
        targetSdk = 36
        versionCode = 166
        versionName = "3.4.260227166"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
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
}

dependencies {

    // Default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Add On
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    // Bluetooth Print
    implementation(libs.escpos.thermalprinter.android)
    // Picasso
    implementation(libs.picasso)
    // Photoview
    implementation(libs.photoview)
    // Maps Resource
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.places)
    implementation(libs.google.maps.services)
    // Event Bus
    implementation(libs.eventbus)
    // MpAndroidChart
    implementation(libs.mpandroidchart)
    // Flexbox
    implementation(libs.flexbox)
    // Generate & Print PDF
    implementation(libs.io)
    implementation(libs.kernel)
    implementation(libs.layout)
    implementation(libs.dexter)
    // Firebase
    implementation(libs.firebase.database)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    // Gson
    implementation(libs.gson)
    // Swipe Refresh
    implementation(libs.androidx.swiperefreshlayout)
    // Chart
    implementation(libs.mpandroidchart)
    // Expand FAB
    implementation(libs.expandable.fab)
    // Qr Scanner
    implementation(libs.code.scanner)
    // Image Loader
    implementation(libs.glide)
    // True Time from Internet
    implementation(libs.commons.net)
    // Media 3 Exo Player
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}