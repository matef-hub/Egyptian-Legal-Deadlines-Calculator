import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

// A signed release must use a stable, OWNER-HELD key. Debug CI builds never load it.
// Do not place the keystore or credentials in Git or in a public artifact.
val keystorePath = System.getenv("MEEAD_KEYSTORE_PATH")
val storePasswordSecret = System.getenv("MEEAD_STORE_PASSWORD")
val keyAliasSecret = System.getenv("MEEAD_KEY_ALIAS")
val keyPasswordSecret = System.getenv("MEEAD_KEY_PASSWORD")
val hasReleaseKey = listOf(keystorePath, storePasswordSecret, keyAliasSecret, keyPasswordSecret)
    .all { !it.isNullOrBlank() } && file(keystorePath ?: "").isFile

android {
    namespace = "com.ateflaw.legaldeadlines"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ateflaw.legaldeadlines"
        minSdk = 24
        targetSdk = 37
        versionCode = 16
        versionName = "1.1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

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

    sourceSets.getByName("androidTest").assets.srcDir(file("schemas/com.ateflaw.legaldeadlines.data.database.AppDatabase"))
    sourceSets.getByName("main").assets.srcDir(rootProject.file("calendar"))

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

// Android Studio passes the owner's keystore to AGP through its signing wizard.
val studioStore = providers.gradleProperty("android.injected.signing.store.file").orNull
val hasStudioKey = studioStore?.let { it.isNotBlank() && file(it).isFile } == true &&
    listOf("store.password", "key.alias", "key.password").all {
        !providers.gradleProperty("android.injected.signing.$it").orNull.isNullOrBlank()
    }
val signingReady = hasReleaseKey || hasStudioKey
tasks.configureEach {
    if (name == "assembleRelease" || name == "bundleRelease") {
        doFirst {
            check(signingReady) {
                "Use Android Studio's Generate Signed App Bundle or APK wizard, or configure the MEEAD_* signing credentials."
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
    implementation(libs.okhttp)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
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