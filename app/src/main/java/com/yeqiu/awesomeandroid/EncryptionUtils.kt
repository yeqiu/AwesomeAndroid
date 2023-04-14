package com.yeqiu.awesomeandroid

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object EncryptionUtils {

    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val SALT_LENGTH = 8
    private const val ITERATIONS = 1000
    private const val KEY_LENGTH = 256
    private const val PASSWORD = "bszh"


    fun encrypt(input: String): String {
        val salt = generateSalt()
        val secretKey: SecretKey = generateSecretKey(PASSWORD.toCharArray(), salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedTextBytes = cipher.doFinal(input.toByteArray())
        val result = ByteArray(salt.size + encryptedTextBytes.size)
        System.arraycopy(salt, 0, result, 0, salt.size)
        System.arraycopy(encryptedTextBytes, 0, result, salt.size, encryptedTextBytes.size)
        return Base64.encodeToString(result, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedText: String?): String? {
        val decodedEncryptedTextBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val salt = ByteArray(SALT_LENGTH)
        System.arraycopy(decodedEncryptedTextBytes, 0, salt, 0, SALT_LENGTH)
        val encryptedTextBytes = ByteArray(decodedEncryptedTextBytes.size - SALT_LENGTH)
        System.arraycopy(
            decodedEncryptedTextBytes,
            SALT_LENGTH,
            encryptedTextBytes,
            0,
            encryptedTextBytes.size
        )
        val secretKey = generateSecretKey(PASSWORD.toCharArray(), salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedTextBytes = cipher.doFinal(encryptedTextBytes)
        return String(decryptedTextBytes)
    }


    private fun generateSalt(): ByteArray {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return salt
    }

    private fun generateSecretKey(password: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
        val secretKey = secretKeyFactory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

}