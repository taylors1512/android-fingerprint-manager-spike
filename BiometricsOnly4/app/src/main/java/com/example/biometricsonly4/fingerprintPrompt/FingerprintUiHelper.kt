package com.example.biometricsonly4.fingerprintPrompt

import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.CancellationSignal
import com.example.biometricsonly4.R

/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
class FingerprintUiHelper

/**
 * Constructor for [FingerprintUiHelper].
 */
internal constructor(private val fingerprintMgr: FingerprintManagerCompat,
                     private val icon: ImageView,
                     private val errorTextView: TextView,
                     private val callback: Callback
) : FingerprintManagerCompat.AuthenticationCallback() {

    private var cancellationSignal: CancellationSignal? = null
    private var selfCancelled = false

    private val resetErrorTextRunnable = Runnable {
        icon.setImageResource(R.drawable.ic_fp_40px)
        errorTextView.run {
            setTextColor(errorTextView.resources.getColor(R.color.hint_color, null))
            text = errorTextView.resources.getString(R.string.fingerprint_hint)
        }
    }

    fun startListening(cryptoObject: FingerprintManagerCompat.CryptoObject) {
        cancellationSignal = CancellationSignal()
        selfCancelled = false
        fingerprintMgr.authenticate(cryptoObject, 0, cancellationSignal, this, null)
        icon.setImageResource(R.drawable.ic_fp_40px)
    }

    fun stopListening() {
        cancellationSignal?.also {
            selfCancelled = true
            it.cancel()
        }
        cancellationSignal = null
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        if (!selfCancelled) {
            showError(errString)
            icon.postDelayed({ callback.onError() }, ERROR_TIMEOUT_MILLIS)
        }
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) =
            showError(helpString)

    override fun onAuthenticationFailed() =
            showError(icon.resources.getString(R.string.fingerprint_not_recognized))

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult) {
        errorTextView.run {
            removeCallbacks(resetErrorTextRunnable)
            setTextColor(errorTextView.resources.getColor(R.color.success_color, null))
            text = errorTextView.resources.getString(R.string.fingerprint_success)
        }
        icon.run {
            setImageResource(R.drawable.ic_fingerprint_success)
            postDelayed({ callback.onAuthenticated() }, SUCCESS_DELAY_MILLIS)
        }
    }

    private fun showError(error: CharSequence) {
        icon.setImageResource(R.drawable.ic_fingerprint_error)
        errorTextView.run {
            text = error
            setTextColor(errorTextView.resources.getColor(R.color.warning_color, null))
            removeCallbacks(resetErrorTextRunnable)
            postDelayed(resetErrorTextRunnable, ERROR_TIMEOUT_MILLIS)
        }
    }

    interface Callback {
        fun onAuthenticated()
        fun onError()
    }

    companion object {
        const val ERROR_TIMEOUT_MILLIS: Long = 1600
        const val SUCCESS_DELAY_MILLIS: Long = 1300
    }
}
