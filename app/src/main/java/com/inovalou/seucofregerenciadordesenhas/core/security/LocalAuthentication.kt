package com.inovalou.seucofregerenciadordesenhas.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val ALLOWED_AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

sealed interface LocalAuthenticationResult {
    data object Succeeded : LocalAuthenticationResult
    data object Cancelled : LocalAuthenticationResult
    data object Failed : LocalAuthenticationResult
    data object Unavailable : LocalAuthenticationResult
}

data class LocalAuthenticationPromptText(
    val title: String,
    val subtitle: String
)

fun FragmentActivity.requestLocalAuthentication(
    promptText: LocalAuthenticationPromptText,
    onResult: (LocalAuthenticationResult) -> Unit
) {
    val availability = BiometricManager.from(this).canAuthenticate(ALLOWED_AUTHENTICATORS)
    if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
        onResult(LocalAuthenticationResult.Unavailable)
        return
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(promptText.title)
        .setSubtitle(promptText.subtitle)
        .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
        .build()

    BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(LocalAuthenticationResult.Succeeded)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode.isCancellationError()) {
                    onResult(LocalAuthenticationResult.Cancelled)
                } else {
                    onResult(LocalAuthenticationResult.Failed)
                }
            }

            override fun onAuthenticationFailed() {
                onResult(LocalAuthenticationResult.Failed)
            }
        }
    ).authenticate(promptInfo)
}

fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is android.content.ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

private fun Int.isCancellationError(): Boolean = this == BiometricPrompt.ERROR_USER_CANCELED ||
    this == BiometricPrompt.ERROR_CANCELED ||
    this == BiometricPrompt.ERROR_NEGATIVE_BUTTON
