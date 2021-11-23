package com.example.biometricsonly.security

import android.util.Log
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

class FingerprintValidationManager {

    companion object {
        private const val ANY_OLD_STRING = "any_old_string"
    }

    /**Tries to encrypt some data, implicitly using the generated key from SetupManager::createSecretKey().
     * This only works if the user just authenticated via fingerprint.
     */
    fun performFinalCryptographicCheck(cipher: Cipher) {
        try {
            cipher.doFinal(ANY_OLD_STRING.toByteArray())
        } catch (exception: Exception) {
            when (exception) {
                is BadPaddingException,
                is IllegalBlockSizeException -> {
                    Log.e(
                        FingerprintValidationManager::javaClass.name,
                        "Failed to encrypt the data with the generated key. ${exception.message}"
                    )
                }
                else -> throw exception
            }
        }
    }
}
