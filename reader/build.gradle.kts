import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Android grants the vault access to this reader only when both packages have the same signer.
evaluationDependsOn(":app")
val vaultSigning = project(":app").extensions.getByType<ApplicationExtension>().signingConfigs

android {
    namespace = "com.mydocvault.reader"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.mydocvault.reader"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = false
        }
    }
    signingConfigs {
        getByName("debug") { initWith(vaultSigning.getByName("debug")) }
        create("release") { initWith(vaultSigning.getByName("release")) }
    }
    buildTypes { release { signingConfig = signingConfigs.getByName("release") } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    packaging.jniLibs.useLegacyPackaging = true
}

dependencies {
    implementation(project(":libreofficekit"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
