package com.example.moodscribbles.data.security

enum class BiometricAvailability {

    /** Pode mostrar prompt (biometria e/ou bloqueio de tela). */
    AVAILABLE,

    /** Sem biometria nem PIN/padrão/senha no aparelho. */
    NOT_ENROLLED,

    /** Hardware/policy não permite (raro). */
    UNAVAILABLE,
    
    /** Status desconhecido / erro do BiometricManager. */
    UNKNOWN,
}