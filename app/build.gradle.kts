import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val calendarificApiKey: String = (localProperties.getProperty("CALENDARIFIC_API_KEY")
    ?: (project.findProperty("CALENDARIFIC_API_KEY") as? String)
    ?: "YOUR_API_KEY").trim('"')

android {
    namespace = "com.ateflaw.legaldeadlines"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ateflaw.legaldeadlines"
        minSdk = 24
        targetSdk = 37
        versionCode = 12
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // No third-party API credential is packaged into a production APK/AAB.
        buildConfigField("String", "CALENDARIFIC_API_KEY", "\"YOUR_API_KEY\"")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    // A signed release must use a stable, OWNER-HELD key. Debug CI builds never load it.
    // Do not place the keystore or credentials in Git or in a public artifact.
    val keystorePath = System.getenv("MEEAD_KEYSTORE_PATH")
    val storePasswordSecret = System.getenv("MEEAD_STORE_PASSWORD")
    val keyAliasSecret = System.getenv("MEEAD_KEY_ALIAS")
    val keyPasswordSecret = System.getenv("MEEAD_KEY_PASSWORD")
    val hasReleaseKey = listOf(keystorePath, storePasswordSecret, keyAliasSecret, keyPasswordSecret)
        .all { !it.isNullOrBlank() } && file(keystorePath ?: "").isFile

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = storePasswordSecret!!
                keyAlias = keyAliasSecret!!
                keyPassword = keyPasswordSecret!!
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            buildConfigField("String", "CALENDARIFIC_API_KEY", "\"$calendarificApiKey\"")
            isMinifyEnabled = false
            // A distinct package protects existing user records when CI runner debug keys rotate.
            applicationIdSuffix = ".legalreview" 
            versionNameSuffix = "-legalreview" 
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }

    lint {
        disable.add("MissingTranslation")
        disable.add("ExtraTranslation")
        disable.add("UnusedResources")
    }
}

// Fail closed for local/manual release tasks too: never accept an unsigned APK as a release.
tasks.configureEach {
    if (name == "assembleRelease" || name == "bundleRelease") {
        doFirst {
            val path = System.getenv("MEEAD_KEYSTORE_PATH")
            val credentials = listOf(
                System.getenv("MEEAD_STORE_PASSWORD"),
                System.getenv("MEEAD_KEY_ALIAS"),
                System.getenv("MEEAD_KEY_PASSWORD")
            )
            check(!path.isNullOrBlank() && file(path).isFile && credentials.all { !it.isNullOrBlank() }) {
                "Missing owner-held release signing credentials (MEEAD_*). No unsigned release is permitted."
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.compose.foundation.layout)
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    // AndroidX & Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    // Required by the JVM-only Room migration test fixture (DriverManager jdbc:sqlite:).
    testImplementation("org.xerial:sqlite-jdbc:3.46.1.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    add("androidTestImplementation", platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}