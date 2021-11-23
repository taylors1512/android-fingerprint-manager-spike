package com.example.biometricsonly2.activity

import com.example.biometricsonly2.security.PreSetupChecks
import com.example.biometricsonly2.security.credentials.SecurityChecks
import javax.crypto.Cipher

sealed class ViewModelIntent {
    object CheckIsUserAlreadyRegistered: ViewModelIntent()
    object UndertakeStartupChecks : ViewModelIntent()
    object CheckIsUserReadyToRegister: ViewModelIntent()
    class SubmitSecurityChecks(val securityChecks: SecurityChecks) : ViewModelIntent()
    object AttemptEnableBiometricLogin : ViewModelIntent()
    object FirstLogIn : ViewModelIntent()

    class ConfirmBiometricPromptEnabledSuccess(val cipher: Cipher) : ViewModelIntent()
    class ShowAuthenticatedContent(val cipher: Cipher) : ViewModelIntent()
}

sealed class ViewModelState {
    object PendingRegistrationSetupChecks: ViewModelState()
    data class StartupChecksRetrieved(val checks: PreSetupChecks) : ViewModelState()
    object AppReadyToRegister: ViewModelState()
    object RegistrationSucceeded: ViewModelState()
    object SecurityChecksPassed: ViewModelState()

    object SecurityChecksFailed: ViewModelState()
    /**
     * I'm not sure if we would pass the cipher and other non-data types with the state in the real app,
     * but I did this just to make example work.
     */
    data class ShowingPromptUserForRegistrationBiometrics(val cipher: Cipher): ViewModelState()
    data class ShowingPromptUserForLoginBiometrics(val cipher: Cipher): ViewModelState()
    data class BiometricValidationFailed(val exception: Exception): ViewModelState()
    data class PresentingGeneralLogin(val cipher: Cipher): ViewModelState()
    object ShowAuthenticatedContent: ViewModelState()
}