package com.example.biometricsonly.security.credentials

class CredentialsChecker {

    companion object {
        private const val USERNAME = "abc"
        private const val PASSWORD = "xyz"
        private const val SECURITY_PIN = "123"
    }

    fun areValid(securityChecks: SecurityChecks) = with(securityChecks) {
        username == USERNAME && password == PASSWORD && securityPin == SECURITY_PIN
    }
}
