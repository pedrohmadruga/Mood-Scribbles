package com.example.moodscribbles.data.security

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import com.example.moodscribbles.R

class BiometricAuthManager(
    private val context: Context,
) {

    fun getAvailability(): BiometricAvailability {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return when (
                BiometricManager.from(context).canAuthenticate(allowedAuthenticatorsForApi30())
            ) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    BiometricAvailability.NOT_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
                -> BiometricAvailability.UNAVAILABLE
                else -> BiometricAvailability.UNKNOWN
            }
        }

        // API 24–29: biometria OU bloqueio de tela seguro
        val biometricManager = BiometricManager.from(context)
        @Suppress("DEPRECATION")
        val biometricOk = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS

        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val deviceSecure = keyguard.isDeviceSecure

        return when {
            biometricOk || deviceSecure -> BiometricAvailability.AVAILABLE
            else -> BiometricAvailability.NOT_ENROLLED
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: CharSequence,
        subtitle: CharSequence,
        onSuccess: () -> Unit,
        onError: (message: CharSequence) -> Unit,
        onCancel: () -> Unit,
    ) {
        if (getAvailability() != BiometricAvailability.AVAILABLE) {
            onError(context.getString(R.string.biometric_error_not_available))
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED,
                    -> onCancel()
                    else -> onError(errString)
                }
            }

            override fun onAuthenticationFailed() {
                // Tentativa inválida (digital errada); o usuário pode tentar de novo.
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = buildPromptInfo(title, subtitle)
        prompt.authenticate(promptInfo)
    }

    private fun buildPromptInfo(
        title: CharSequence,
        subtitle: CharSequence,
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(allowedAuthenticatorsForApi30())
            // Com DEVICE_CREDENTIAL no API 30+, não use setNegativeButtonText.
        } else {
            @Suppress("DEPRECATION")
            builder.setDeviceCredentialAllowed(true)
        }

        return builder.build()
    }

    private fun allowedAuthenticatorsForApi30(): Int =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
}