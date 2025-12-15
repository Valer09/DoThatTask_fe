package homeaq.dothattask.dothattask_fe.dothattask_fe

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoHelper
{
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    fun encrypt(data: String, key: SecretKey): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Pair(encrypted, iv)
    }

    fun decrypt(encryptedData: ByteArray, iv: ByteArray, key: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(encryptedData)
        return decrypted.toString(Charsets.UTF_8)
    }

}