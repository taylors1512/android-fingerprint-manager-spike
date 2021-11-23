package com.example.biometricsonly2.activity

import android.util.Log
import com.example.biometricsonly2.architecuture.BaseViewModel
import com.example.biometricsonly2.security.FingerprintValidationManager
import com.example.biometricsonly2.security.PreSetupChecker
import com.example.biometricsonly2.security.SecurityManager
import com.example.biometricsonly2.security.credentials.CredentialsChecker
import com.example.biometricsonly2.security.credentials.SecurityChecks
import com.example.biometricsonly2.security.isReadyToRegister
import com.example.biometricsonly2.user.UserStatusManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.crypto.Cipher

@ExperimentalCoroutinesApi
class MainViewModel : BaseViewModel() {

    private val userStatusManager = UserStatusManager()
    var preSetupChecker: PreSetupChecker? = null
    private val credentialsChecker = CredentialsChecker()
    private val securityManager = SecurityManager()
    private val fingerprintValidationManager = FingerprintValidationManager()

    override fun sendIntent(intent: ViewModelIntent) {
        when (intent) {
            ViewModelIntent.CheckIsUserAlreadyRegistered -> checkUserRegistrationStatus()
            ViewModelIntent.UndertakeStartupChecks -> undertakeStartupChecks()
            ViewModelIntent.CheckIsUserReadyToRegister -> checkIsUserReadyToRegister()
            is ViewModelIntent.SubmitSecurityChecks -> submitSecurityChecks(intent.securityChecks)
            ViewModelIntent.AttemptEnableBiometricLogin -> onEnableBiometricLogin()
            is ViewModelIntent.ConfirmBiometricPromptEnabledSuccess -> onConfirmBiometricPromptEnabled(
                intent.cipher
            )
            ViewModelIntent.FirstLogIn -> firstLogIn()
            is ViewModelIntent.ShowAuthenticatedContent -> onShowAuthenticatedContent(intent.cipher)
        }
    }

    private fun checkUserRegistrationStatus() {
        try {
            userStatusManager.isRegistered().takeIf { it }?.also {
                securityManager.initialiseCipherForLogin()
                ViewModelState.PresentingGeneralLogin(
                    requireNotNull(securityManager.cipher)
                ).updateState()
            } ?: run {
                ViewModelState.PendingRegistrationSetupChecks.updateState()
            }
        } catch (exception: Exception) {
            Log.e(MainViewModel::javaClass.name, exception.toString())
            ViewModelState.BiometricValidationFailed(exception).updateState()
        }
    }

    private fun undertakeStartupChecks() {
        val checks = requireNotNull(preSetupChecker).getPreSetupChecks()
        ViewModelState.StartupChecksRetrieved(checks).updateState()
    }

    private fun checkIsUserReadyToRegister() {
        val checks = requireNotNull(preSetupChecker).getPreSetupChecks()
        if (checks.isReadyToRegister()) {
            ViewModelState.AppReadyToRegister.updateState()
        }
    }

    private fun submitSecurityChecks(securityChecks: SecurityChecks) {
        credentialsChecker.areValid(securityChecks).takeIf { it }?.let {
            ViewModelState.SecurityChecksPassed.updateState()
        } ?: ViewModelState.SecurityChecksFailed.updateState()
    }

    private fun onEnableBiometricLogin() {
        securityManager.initialiseSecretKeyAndSetupCipher()
        requireNotNull(securityManager.cipher).run {
            ViewModelState.ShowingPromptUserForRegistrationBiometrics(this).updateState()
        }
    }

    private fun onConfirmBiometricPromptEnabled(cipher: Cipher) {
        try {
            fingerprintValidationManager.performFinalCryptographicCheck(cipher)
            ViewModelState.RegistrationSucceeded.updateState()
        } catch (exception: Exception) {
            Log.e(MainViewModel::javaClass.name, exception.toString())
            ViewModelState.BiometricValidationFailed(exception).updateState()
        }
    }

    private fun firstLogIn() {
        try {
            with(securityManager) {
                reinitialiseCipher()
                ViewModelState.ShowingPromptUserForLoginBiometrics(this.cipher!!).updateState()
            }
        } catch (exception: Exception) {
            Log.e(MainViewModel::javaClass.name, exception.toString())
            ViewModelState.BiometricValidationFailed(exception).updateState()
        }
    }

    private fun onShowAuthenticatedContent(cipher: Cipher) {
        try {
            fingerprintValidationManager.performFinalCryptographicCheck(cipher)
            ViewModelState.ShowAuthenticatedContent.updateState()
        } catch (exception: Exception) {
            Log.e(MainViewModel::javaClass.name, exception.toString())
            ViewModelState.BiometricValidationFailed(exception).updateState()
        }
    }
}