package com.example.biometricsonly.security.credentials

data class SecurityChecks(
    val username: String,
    val password: String,
    val securityPin: String
)