package com.example.biometricsonly.activity

import android.app.Activity
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.biometricsonly.architecuture.BaseActivity
import com.example.biometricsonly.architecuture.observe
import com.example.biometricsonly.databinding.ActivityMainBinding
import com.example.biometricsonly.fingerprintPrompt.FingerprintAuthenticationDialogFragment
import com.example.biometricsonly.security.PreSetupChecker
import com.example.biometricsonly.security.PreSetupChecks
import com.example.biometricsonly.security.credentials.SecurityChecks
import com.example.biometricsonly.user.UserStatusManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.crypto.Cipher

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.apply {
            preSetupChecker = PreSetupChecker(this@MainActivity.applicationContext)
            userStatusManager = UserStatusManager(this@MainActivity.applicationContext)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        observeStates()
        initClickListeners()

        viewModel.sendIntent(ViewModelIntent.CheckIsUserAlreadyRegistered)
    }

    private fun observeStates() {
        observe(viewModel.getStateFlow()) { state ->
            when (state) {
                ViewModelState.PendingRegistrationSetupChecks -> prepareRegistrationJourneyScreen()
                is ViewModelState.StartupChecksRetrieved -> updateStartupChecks(state.checks)
                ViewModelState.AppReadyToRegister -> prepareRegistrationState()
                ViewModelState.SecurityChecksPassed -> prepareForAllowBiometrics()
                ViewModelState.SecurityChecksFailed -> showSecurityCheckFail()
                is ViewModelState.ShowingPromptUserForRegistrationBiometrics -> showBiometricPromptForRegistration(state.cipher)
                is ViewModelState.ShowingPromptUserForLoginBiometrics -> showBiometricPromptForLogin(state.cipher)
                ViewModelState.RegistrationSucceeded -> prepareForFirstLogin()
                is ViewModelState.PresentingGeneralLogin -> showingGeneralLoginScreen(state.cipher)
                is ViewModelState.BiometricValidationFailed -> showBiometricsFailure(state.exception)
                ViewModelState.ShowAuthenticatedContent -> showAuthenticatedContent()
            }
        }
    }

    private fun initClickListeners() {
        with(binding) {
            buttonSubmitSecurityChecks.setOnClickListener {
                hideKeyboard()
                val securityChecks = SecurityChecks(
                    username = editableEnterUsername.text.toString(),
                    password = editableEnterPassword.text.toString(),
                    securityPin = editableEnterSecurityPin.text.toString(),
                )
                viewModel.sendIntent(ViewModelIntent.SubmitSecurityChecks(securityChecks))
            }
            buttonAllowBiometricLogin.setOnClickListener {
                viewModel.sendIntent(ViewModelIntent.AttemptEnableBiometricLogin)
            }
            buttonLogin.setOnClickListener {
                viewModel.sendIntent(ViewModelIntent.FirstLogIn)
            }
        }
    }

    private fun prepareRegistrationJourneyScreen() {
        with(binding) {
            sectionSetup.isVisible = true
            sectionRegister.isVisible = false
            sectionSecurityChecks.isVisible = false
            sectionAllowBiometrics.isVisible = false
            sectionFirstLogin.isVisible = false
            textGeneralLogin.isVisible = false
            animSpinningLogo.isVisible = false
        }
        viewModel.sendIntent(ViewModelIntent.UndertakeStartupChecks)
    }

    private fun updateStartupChecks(checks: PreSetupChecks) {
        with(binding) {
            valueIsDeviceProtectedByPin.setFlag(checks.isDeviceProtectedByPin)
            valueHasHardware.setFlag(checks.hasFingerprintHardware)
            valueDeviceHasAtLeastOneFingerprintRegistered.setFlag(checks.deviceHasAtLeastOneFingerprint)
        }
        viewModel.sendIntent(ViewModelIntent.CheckIsUserReadyToRegister)
    }

    private fun prepareRegistrationState() {
        with(binding) {
            sectionSetup.isVisible = true
            sectionRegister.isVisible = true
            sectionSecurityChecks.isVisible = true
            sectionAllowBiometrics.isVisible = false
            sectionFirstLogin.isVisible = false
            textGeneralLogin.isVisible = false
            animSpinningLogo.isVisible = false
        }
    }

    private fun prepareForAllowBiometrics() {
        with(binding) {
            sectionSetup.isVisible = false
            sectionRegister.isVisible = true
            sectionSecurityChecks.isVisible = false
            sectionAllowBiometrics.isVisible = true
            sectionFirstLogin.isVisible = false
            textGeneralLogin.isVisible = false
            animSpinningLogo.isVisible = false
            scrollView.fullScroll(ScrollView.FOCUS_UP)
        }
    }

    private fun prepareForFirstLogin() {
        with(binding) {
            sectionSetup.isVisible = false
            sectionRegister.isVisible = false
            sectionSecurityChecks.isVisible = false
            sectionAllowBiometrics.isVisible = false
            sectionFirstLogin.isVisible = true
            textGeneralLogin.isVisible = false
            animSpinningLogo.isVisible = false
            scrollView.fullScroll(ScrollView.FOCUS_UP)
        }
    }

    private fun showSecurityCheckFail() {
        Toast.makeText(this, "Incorrect credentials, try again \uD83D\uDCA9", Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Much of the code in the fragment and associated resources
     * have been lifted from the google sample, com.example.android.fingerprintdialog
     */
    private fun showBiometricPromptForRegistration(cipher: Cipher) {
        FingerprintAuthenticationDialogFragment().apply {
            setCryptoObject(FingerprintManager.CryptoObject(cipher))
            setCallback(object : FingerprintAuthenticationDialogFragment.Callback {
                override fun onAuthenticated(crypto: FingerprintManager.CryptoObject) {
                    viewModel.sendIntent(ViewModelIntent.ConfirmBiometricPromptEnabledSuccess(crypto.cipher))
                }
            })
            show(supportFragmentManager, FingerprintAuthenticationDialogFragment::javaClass.name)
        }
    }

    private fun showBiometricPromptForLogin(cipher: Cipher) {
        FingerprintAuthenticationDialogFragment().apply {
            setCryptoObject(FingerprintManager.CryptoObject(cipher))
            setCallback(object : FingerprintAuthenticationDialogFragment.Callback {
                override fun onAuthenticated(crypto: FingerprintManager.CryptoObject) {
                    viewModel.sendIntent(ViewModelIntent.ShowAuthenticatedContent(crypto.cipher))
                }
            })
            show(supportFragmentManager, FingerprintAuthenticationDialogFragment::javaClass.name)
        }
    }

    private fun showingGeneralLoginScreen(cipher: Cipher) {
        with(binding) {
            sectionSetup.isVisible = false
            sectionRegister.isVisible = false
            sectionSecurityChecks.isVisible = false
            sectionAllowBiometrics.isVisible = false
            sectionFirstLogin.isVisible = false
            textGeneralLogin.isVisible = true
            animSpinningLogo.isVisible = false
        }
        FingerprintAuthenticationDialogFragment().apply {
            setCryptoObject(FingerprintManager.CryptoObject(cipher))
            setCallback(object : FingerprintAuthenticationDialogFragment.Callback {
                override fun onAuthenticated(crypto: FingerprintManager.CryptoObject) {
                    viewModel.sendIntent(ViewModelIntent.ShowAuthenticatedContent(crypto.cipher))
                }
            })
            show(supportFragmentManager, FingerprintAuthenticationDialogFragment::javaClass.name)
        }
    }

    private fun showBiometricsFailure(exception: Exception) {
        Toast.makeText(this, "Biometrics Fail - exception: ${exception.message}", Toast.LENGTH_LONG).show()
        viewModel.sendIntent(ViewModelIntent.UndertakeStartupChecks)
    }

    private fun showAuthenticatedContent() {
        Toast.makeText(this, "Login Success!", Toast.LENGTH_LONG).show()
        with(binding) {
            sectionSetup.isVisible = false
            sectionRegister.isVisible = false
            sectionSecurityChecks.isVisible = false
            sectionAllowBiometrics.isVisible = false
            sectionFirstLogin.isVisible = false
            textGeneralLogin.isVisible = false
            animSpinningLogo.isVisible = true
        }
    }
}

private fun Activity.hideKeyboard() {
    with(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }
}

private fun TextView.setFlag(isTrue: Boolean) {
    text = isTrue.toString()
}