package com.example.biometricsonly.user

import android.content.Context
import androidx.preference.PreferenceManager

class UserStatusManager(context: Context) {

    private companion object {
        const val IS_USER_REGISTERED = "key_is_user_registered"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var isRegistered: Boolean
        set(value) {
            sharedPreferences.edit().putBoolean(IS_USER_REGISTERED, value).apply()
        }
        get() = sharedPreferences.getBoolean(IS_USER_REGISTERED, false)
}
