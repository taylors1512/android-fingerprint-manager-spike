package com.example.biometricsonly2.user

import java.security.KeyStore
import java.security.KeyStoreException

class UserStatusManager {

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_NAME = "key_name"
    }

    private var keyStore: KeyStore? = null

    fun isRegistered() = run {
        initialiseKeystore()
        checkIsSecretKeyStored()
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

    private fun checkIsSecretKeyStored(): Boolean =
        requireNotNull(keyStore).let {
            it.load(null)
            it.getKey(KEY_NAME, null) != null
        }
}
