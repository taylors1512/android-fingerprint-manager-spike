package com.example.biometricsonly.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class SecurityManager {

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_NAME = "key_name"
    }

    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    var cipher: Cipher? = null

    fun initialiseSecretKeyAndSetupCipher() {
        initialiseKeystore()
        initialiseKeyGenerator()
        setupCipher()
        createSecretKey()
        initCipher()
    }

    fun reinitialiseCipher() {
        initCipher()
    }

    fun initialiseCipherForLogin() {
        initialiseKeystore()
        initialiseKeyGenerator()
        setupCipher()
        initCipher()
    }

    private fun initialiseKeystore() {
        /**
         * From the KeyStore.getInstance(type: String) JavaDocs.
         * This method traverses the list of registered security Providers, starting with the most preferred Provider.
         *
         * Returns a keystore of the specified type.
         *
         */
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        } catch (exception: KeyStoreException) {
            throw exception
        }
    }

    private fun initialiseKeyGenerator() {
        /**
         * From the KeyGenerator.getInstance(algorithm: String, provider: String) JavaDocs.
         * Returns a KeyGenerator that generates secret keys for the specified algorithm.
         *
         * algorithm: Advanced Encryption Standard (AES) key.
         * provider: ANDROID_KEY_STORE, as initialised in above function.
         */
        try {
            keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        } catch (exception: Exception) {
            when (exception) {
                is NoSuchAlgorithmException,
                is NoSuchProviderException ->
                    throw exception
                else -> throw exception
            }
        }
    }

    private fun checkIsSecretKeyStored(): Boolean =
        requireNotNull(keyStore).let {
            it.load(null)
            it.getKey(KEY_NAME, null) != null
        }

    /**
     * From the Cipher.getInstance(transformation) JavaDocs
     * Returns a Cipher that implements the specified [encryption/decryption] transformation.
     *
     * /** Advanced Encryption Standard (AES) key. */
     * /** Cipher Block Chaining (CBC) block mode. */
     * /** PKCS#7 encryption padding scheme. */
     */
    private fun setupCipher() {
        try {
            val cipherString =
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            cipher = Cipher.getInstance(cipherString)
        } catch (exception: Exception) {
            when (exception) {
                is NoSuchAlgorithmException,
                is NoSuchPaddingException ->
                    throw exception
                else -> throw exception
            }
        }
    }

    private fun createSecretKey() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of enrolled
        // fingerprints has changed.
        try {
            requireNotNull(keyStore).load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val builder = KeyGenParameterSpec.Builder(KEY_NAME, keyProperties)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setInvalidatedByBiometricEnrollment(true)

            requireNotNull(keyGenerator).run {
                init(builder.build())
                // Note - no local reference to the key is stored
                generateKey()
            }
        } catch (exception: Exception) {
            when (exception) {
                is NoSuchAlgorithmException,
                is InvalidAlgorithmParameterException,
                is CertificateException,
                is IOException -> throw exception
                else -> throw exception
            }
        }
    }

    /**
     * Initialize the [Cipher] instance with the created key in the [createSecretKey] method.
     *
     * @param keyName the key name to init the cipher
     * @return `true` if initialization succeeded, `false` if the lock screen has been disabled or
     * reset after key generation, or if a fingerprint was enrolled after key generation.
     */
    private fun initCipher() {
        try {
            requireNotNull(keyStore).load(null)
            requireNotNull(cipher).init(
                Cipher.ENCRYPT_MODE, requireNotNull(keyStore).getKey(
                    KEY_NAME, null
                ) as SecretKey
            )
        } catch (exception: Exception) {
            when (exception) {
                is KeyPermanentlyInvalidatedException,
                is KeyStoreException,
                is CertificateException,
                is UnrecoverableKeyException,
                is IOException,
                is NoSuchAlgorithmException,
                is InvalidKeyException -> throw exception
                else -> throw exception
            }
        }
    }
}
