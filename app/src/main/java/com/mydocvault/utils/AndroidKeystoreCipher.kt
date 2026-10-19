package com.mydocvault.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts small local secrets with a device-bound AES-GCM key in Android Keystore.
 * This key is separate from the app-signing keystore used by Gradle/release builds.
 */
object AndroidKeystoreCipher {
    private const val KEY_ALIAS = "mydocvault.local-preferences.v1"
    private const val PREFIX = "enc:v1:"
    const val KEY_UNAVAILABLE_SENTINEL = "!keystore-key-unavailable!"

    fun isEncrypted(value: String): Boolean = value.startsWith(PREFIX)

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encrypted
        return PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    /**
     * Returns the legacy plaintext unchanged for one-time migration.
     * If an encrypted value cannot be decrypted, returns a non-PIN sentinel (fail closed).
     */
    fun decryptStoredValue(value: String): String {
        if (!isEncrypted(value)) return value

        return try {
            val payload = Base64.decode(value.removePrefix(PREFIX), Base64.NO_WRAP)
            require(payload.size > IV_LENGTH) { "Invalid encrypted preference" }
            val iv = payload.copyOfRange(0, IV_LENGTH)
            val ciphertext = payload.copyOfRange(IV_LENGTH, payload.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (_: Exception) {
            KEY_UNAVAILABLE_SENTINEL
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private const val IV_LENGTH = 12
    private const val TAG_BITS = 128
}
