package com.example.biometricsonly4.security

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.FINGERPRINT_SERVICE
import android.content.Context.KEYGUARD_SERVICE
import android.hardware.fingerprint.FingerprintManager
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class PreSetupChecks(
    val isDeviceProtectedByPin: Boolean,
    val hasFingerprintHardware: Boolean,
    val deviceHasAtLeastOneFingerprint: Boolean,
)

@ExperimentalCoroutinesApi
class PreSetupChecker(context: Context) {

    private var fingerprintManager: FingerprintManager =
        context.getSystemService(FINGERPRINT_SERVICE) as FingerprintManager

    private var keyguardManager: KeyguardManager =
        context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

    // Fingerprint changed check here?
        // If changed, re-register
        // If not changed, login
        // If none found, then just register?
    fun getPreSetupChecks() = PreSetupChecks(
        isDeviceProtectedByPin(),
        hasFingerprintHardware(),
        deviceHasAtLeastOneFingerprintRegistered(),
    )

    private fun isDeviceProtectedByPin() = keyguardManager.isKeyguardSecure
        .andLog("isDeviceProtectedByPin()")

    private fun hasFingerprintHardware() = fingerprintManager.isHardwareDetected
        .andLog("hasFingerprintHardware()")

    private fun deviceHasAtLeastOneFingerprintRegistered() =
        fingerprintManager.hasEnrolledFingerprints()
            .andLog("deviceHasAtLeastOneFingerprintRegistered()")
}

fun PreSetupChecks.isReadyToRegister() = isDeviceProtectedByPin &&
        hasFingerprintHardware &&
        deviceHasAtLeastOneFingerprint

private fun Boolean.andLog(check: String) =
    this.apply { Log.d(PreSetupChecker::class.java.simpleName, "***** $check: $this") }