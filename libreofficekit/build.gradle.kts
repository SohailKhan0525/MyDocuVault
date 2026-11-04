import java.io.OutputStream
import java.io.FileNotFoundException
import java.security.DigestInputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.net.URI
import java.security.MessageDigest
import org.gradle.api.tasks.Sync

plugins {
    id("com.android.library")
}

android {
    namespace = "org.libreoffice"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { buildConfig = true }
}

// F-Droid builds all four APKs from the same LibreOffice 26.2.6.3 source.
// Import only the engine, its sibling libraries, and its matching configuration.
val engines = mapOf(
    "arm64-v8a" to (131 to "751ac3836890b79a56801bf6d9a2788cda3761ab5131a4f0b6abfbad822bb475"),
    "armeabi-v7a" to (130 to "1b8ede653604d7a2df9a090cd333942d115132f4ed19bbb84b3ee1d17b5dcf37"),
    "x86_64" to (129 to "cfaf9eea5fd695c449e92eb10cc0b1617ec51d068345068cea9b7e1c67968df5"),
    "x86" to (128 to "44d41200a07782da26e72a112cf0f4461a42d93c679c3e1a71b0e8bb4006fba6")
)

val runtimeDirectory = layout.buildDirectory.dir("generated/office")
val prepareOfficeRuntime = tasks.register<Sync>("prepareOfficeRuntime") {
    into(runtimeDirectory)
}
for ((abi, release) in engines) {
    val (code, sha256) = release
    val filename = "org.documentfoundation.libreoffice_${code}.apk"
    val apk = layout.buildDirectory.file("downloads/$filename")
    val download = tasks.register("fetchOfficeRuntime$code") {
        inputs.property("sha256", sha256)
        outputs.file(apk)
        doLast {
            val destination = apk.get().asFile
            fun checksum(file: File): String {
                val digest = MessageDigest.getInstance("SHA-256")
                DigestInputStream(file.inputStream(), digest).use { it.copyTo(OutputStream.nullOutputStream()) }
                return digest.digest().joinToString("") { "%02x".format(it) }
            }
            if (destination.isFile && checksum(destination) == sha256) return@doLast
            destination.parentFile.mkdirs()
            val part = File.createTempFile("download-", ".part", destination.parentFile)
            try {
                // F-Droid periodically moves older published versions to its archive.
                val urls = listOf("repo", "archive").map { URI("https://f-droid.org/$it/$filename").toURL() }
                val input = try { urls.first().openStream() } catch (_: FileNotFoundException) {
                    urls.last().openStream()
                }
                input.use { source -> part.outputStream().use { source.copyTo(it) } }
                check(checksum(part) == sha256) {
                    "Unexpected checksum for $filename"
                }
                Files.move(part.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } finally {
                part.delete()
            }
        }
    }
    prepareOfficeRuntime.configure {
        dependsOn(download)
        from({ zipTree(apk) }) {
            include("lib/$abi/*.so")
            if (abi == "arm64-v8a") include("assets/program/**", "assets/share/**", "assets/unpack/**")
        }
    }
}

android.sourceSets.getByName("main") {
    assets.setSrcDirs(listOf(runtimeDirectory.map { it.dir("assets") }))
    jniLibs.setSrcDirs(listOf(runtimeDirectory.map { it.dir("lib") }))
}
tasks.named("preBuild").configure { dependsOn(prepareOfficeRuntime) }

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.libreoffice:unoloader:24.2.3")
    implementation("org.libreoffice:libreoffice:24.2.3")
}
