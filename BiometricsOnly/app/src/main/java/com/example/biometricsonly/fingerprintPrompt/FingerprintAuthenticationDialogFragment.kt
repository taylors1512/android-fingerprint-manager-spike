package com.example.biometricsonly.fingerprintPrompt

import android.content.Context
import android.content.SharedPreferences
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import com.example.biometricsonly.R

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
class FingerprintAuthenticationDialogFragment : androidx.fragment.app.DialogFragment(),
    FingerprintUiHelper.Callback {

    enum class Stage { FINGERPRINT }

    private lateinit var cancelButton: Button
    private lateinit var fingerprintContainer: View
    private lateinit var secondDialogButton: Button

    private lateinit var callback: Callback
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var fingerprintUiHelper: FingerprintUiHelper
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        retainInstance = true
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setTitle("Tap your fingerprint")
        return inflater.inflate(R.layout.fingerprint_dialog_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelButton = view.findViewById(R.id.cancel_button)
        fingerprintContainer = view.findViewById(R.id.fingerprint_container)
        secondDialogButton = view.findViewById(R.id.second_dialog_button)

        cancelButton.setOnClickListener { dismiss() }

        fingerprintUiHelper = FingerprintUiHelper(
            requireActivity().getSystemService(FingerprintManager::class.java),
            view.findViewById(R.id.fingerprint_icon),
            view.findViewById(R.id.fingerprint_status),
            this
        )
        updateContent()
    }

    override fun onResume() {
        super.onResume()
        fingerprintUiHelper.startListening(cryptoObject)
    }

    override fun onPause() {
        super.onPause()
        fingerprintUiHelper.stopListening()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inputMethodManager = context.getSystemService(InputMethodManager::class.java)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setCryptoObject(cryptoObject: FingerprintManager.CryptoObject) {
        this.cryptoObject = cryptoObject
    }

    private fun updateContent() {
        cancelButton.setText(R.string.cancel)
        fingerprintContainer.visibility = View.VISIBLE
    }

    override fun onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication succeeded.
        callback.onAuthenticated(crypto = cryptoObject)
        dismiss()
    }

    override fun onError() {
        Toast.makeText(context, "Fingerprint error", Toast.LENGTH_SHORT).show()
    }

    interface Callback {
        fun onAuthenticated(crypto: FingerprintManager.CryptoObject)
    }
}
