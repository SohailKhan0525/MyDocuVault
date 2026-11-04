import java.net.URI
import java.security.MessageDigest

plugins {
    id("com.android.library")
}

android {
    namespace = "org.libreoffice"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

val libreOfficeNative = file("src/main/jniLibs/arm64-v8a/liblo-native-code.so")
val downloadLibreOfficeNative = tasks.register("downloadLibreOfficeNative") {
    outputs.file(libreOfficeNative)
    doLast {
        if (!libreOfficeNative.exists()) {
            libreOfficeNative.parentFile.mkdirs()
            URI("https://github.com/h4de5ing/LibreOfficeViewer/releases/download/v7.6.7.2/liblo-native-code.so")
                .toURL()
                .openStream()
                .use { input -> libreOfficeNative.outputStream().use { output -> input.copyTo(output) } }
        }
        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(libreOfficeNative.readBytes())
            .joinToString("") { "%02x".format(it) }
        check(checksum == "25f2a13cbbf897b06b7e833519bf8ff8999762e4356bf97c3c2180cb373bdd5d") {
            "LibreOfficeKit native binary checksum mismatch."
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(downloadLibreOfficeNative)
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.libreoffice:unoloader:24.2.3")
    implementation("org.libreoffice:libreoffice:24.2.3")
}
