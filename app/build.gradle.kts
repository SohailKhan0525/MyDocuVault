import java.util.Properties
import java.io.File
import org.gradle.api.GradleException

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

// Load signing credentials from local.properties (local dev) or environment variables (CI).
// Preserve the existing signing setup used by previously published releases.
val localProps = Properties().also { props ->
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) props.load(localPropsFile.inputStream())
}
fun signingProp(key: String, envKey: String = key): String =
    System.getenv(envKey) ?: localProps.getProperty(key) ?: ""

/** Resolves keystore path from env/local props, supporting absolute, module-relative, and root-relative paths. */
fun resolveStoreFile(configuredPath: String, defaultRelativePath: String): File {
    if (configuredPath.isBlank()) return file(defaultRelativePath)
    val provided = File(configuredPath)
    if (provided.isAbsolute) return provided
    val moduleRelative = file(configuredPath)
    if (moduleRelative.exists()) return moduleRelative
    val rootRelative = rootProject.file(configuredPath)
    if (rootRelative.exists()) return rootRelative
    throw GradleException(
        "Keystore file '$configuredPath' not found. Checked '${moduleRelative.path}' and '${rootRelative.path}'."
    )
}

val sharedKeystorePath = signingProp("KEYSTORE_FILE")
val useSharedKeystore = sharedKeystorePath.isNotBlank()

android {
    namespace = "com.mydocvault"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mydocvault"
        minSdk = 26
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 39
        versionName = "1.15.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    signingConfigs {
        getByName("debug") {
            storeFile = resolveStoreFile(sharedKeystorePath, "debug.keystore")
            storePassword = signingProp("KEYSTORE_PASSWORD").ifEmpty {
                if (useSharedKeystore) "mydocvault2024" else "android"
            }
            keyAlias = signingProp("KEY_ALIAS").ifEmpty {
                if (useSharedKeystore) "mydocvault" else "androiddebugkey"
            }
            keyPassword = signingProp("KEY_PASSWORD").ifEmpty {
                if (useSharedKeystore) "mydocvault2024" else "android"
            }
        }
        create("release") {
            storeFile = resolveStoreFile(sharedKeystorePath, "keystore/mydocvault.keystore")
            storePassword = signingProp("KEYSTORE_PASSWORD").ifEmpty { "mydocvault2024" }
            keyAlias = signingProp("KEY_ALIAS").ifEmpty { "mydocvault" }
            keyPassword = signingProp("KEY_PASSWORD").ifEmpty { "mydocvault2024" }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += "lib/*/libc++_shared.so"
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.0")
    implementation("org.tukaani:xz:1.10")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation(project(":libreofficekit"))
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.foundation:foundation")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")

    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.compose.animation:animation:1.7.8")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("org.apache.commons:commons-compress:1.27.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Use maintained fork available from configured repositories; it preserves com.github.barteksc.* APIs.
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
}
