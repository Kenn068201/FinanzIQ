package com.example.util

import com.example.data.local.SecurityPolicyEntity
import java.util.regex.Pattern


object Validators {
    // Matches standard email format: user@domain.com
    private val EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

    // First and last names must start with uppercase followed by lowercase letters (supports compound names)
    private val NAME_REGEX = Pattern.compile("^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+( [A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)*\$")

    // Username format: 3-30 alphanumeric characters or underscores
    private val USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{3,30}\$")

    // Phone must start with 5, 7, or 8 and have exactly 8 digits
    private val PHONE_REGEX = Pattern.compile("^[578]\\d{7}\$")

    // Special characters
    private val SPECIAL_CHAR_REGEX = Pattern.compile("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]")

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matcher(email.trim()).matches()
    }

    fun isValidName(name: String): Boolean {
        if (name.isBlank() || name.length > 70) return false
        return NAME_REGEX.matcher(name.trim()).matches()
    }

    /**
     * Valida el formato del nombre de usuario.
     * Permite letras, números y guiones bajos (3 a 30 caracteres).
     */
    fun validateUsername(username: String): Pair<Boolean, String?> {
        val u = username.trim()
        if (u.isBlank()) {
            return false to "Por favor ingresa un nombre de usuario."
        }
        if (u.length < 3) {
            return false to "El nombre de usuario debe tener al menos 3 caracteres."
        }
        if (u.length > 30) {
            return false to "El nombre de usuario no debe exceder 30 caracteres."
        }
        if (!USERNAME_REGEX.matcher(u).matches()) {
            return false to "El nombre de usuario solo puede contener letras, números y guiones bajos sin espacios."
        }
        return true to null
    }

    fun validateName(name: String, isLastName: Boolean): Pair<Boolean, String?> {
        val fieldName = if (isLastName) "apellidos" else "nombres"
        if (name.isBlank()) {
            return false to "Por favor ingresa los $fieldName."
        }
        if (name.length > 70) {
            return false to "Has alcanzado el límite máximo de 70 caracteres para los $fieldName."
        }
        if (!NAME_REGEX.matcher(name.trim()).matches()) {
            return false to "Cada nombre o apellido debe iniciar con letra Mayúscula (ej. Carlos Alberto)."
        }
        return true to null
    }

    /**
     * Valida el nombre o apellido de acuerdo con la directiva de seguridad vigente configurada por el admin.
     */
    fun validateNameWithPolicy(name: String, isLastName: Boolean, policy: SecurityPolicyEntity): Pair<Boolean, String?> {
        val fieldName = if (isLastName) "apellidos" else "nombres"
        if (name.isBlank()) {
            return false to "Por favor ingresa los $fieldName."
        }
        if (name.length > policy.maxNameLength) {
            return false to "Has alcanzado el límite máximo de ${policy.maxNameLength} caracteres para los $fieldName."
        }
        if (policy.requireCapitalizedNames && !NAME_REGEX.matcher(name.trim()).matches()) {
            return false to "Cada nombre o apellido debe iniciar con letra Mayúscula (ej. Carlos Alberto)."
        }
        return true to null
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && PHONE_REGEX.matcher(phone.trim()).matches()
    }

    fun validatePhoneWithCountry(country: CountryPhoneConfig, phone: String): Pair<Boolean, String?> {
        return CountryPhoneData.validatePhone(country, phone)
    }

    fun isValidPassword(password: String): Pair<Boolean, String?> {
        if (password.length < 8) {
            return false to "La contraseña debe tener al menos 8 caracteres."
        }
        if (password.length > 20) {
            return false to "La contraseña no debe superar los 20 caracteres."
        }
        if (!password.any { it.isUpperCase() }) {
            return false to "La contraseña debe incluir al menos una letra mayúscula."
        }
        if (!password.any { it.isDigit() }) {
            return false to "La contraseña debe incluir al menos un número."
        }
        if (!SPECIAL_CHAR_REGEX.matcher(password).find()) {
            return false to "La contraseña debe incluir al menos un carácter especial (ej. !@#\$%&*)."
        }
        return true to null
    }

    /**
     * Valida la contraseña aplicando dinámicamente las directivas de seguridad activas.
     */
    fun isValidPasswordWithPolicy(password: String, policy: SecurityPolicyEntity): Pair<Boolean, String?> {
        if (password.length < policy.passwordMinLength) {
            return false to "La contraseña debe tener al menos ${policy.passwordMinLength} caracteres."
        }
        if (password.length > policy.passwordMaxLength) {
            return false to "La contraseña no debe superar los ${policy.passwordMaxLength} caracteres."
        }
        if (policy.requireUppercase && !password.any { it.isUpperCase() }) {
            return false to "La contraseña debe incluir al menos una letra mayúscula."
        }
        if (policy.requireLowercase && !password.any { it.isLowerCase() }) {
            return false to "La contraseña debe incluir al menos una letra minúscula."
        }
        if (policy.requireNumber && !password.any { it.isDigit() }) {
            return false to "La contraseña debe incluir al menos un número."
        }
        if (policy.requireSpecialChar && !SPECIAL_CHAR_REGEX.matcher(password).find()) {
            return false to "La contraseña debe incluir al menos un carácter especial (ej. !@#\$%&*)."
        }
        return true to null
    }

    fun isValidAge(ageStr: String): Pair<Boolean, String?> {
        val age = ageStr.toIntOrNull()
        if (age == null) {
            return false to "Ingrese una edad válida en números."
        }
        if (age < 18) {
            return false to "Debes ser mayor de 18 años para registrarte."
        }
        if (age > 120) {
            return false to "Ingrese una edad válida."
        }
        return true to null
    }

    /**
     * Valida la edad requerida aplicando dinámicamente la directiva de seguridad vigente.
     */
    fun isValidAgeWithPolicy(ageStr: String, policy: SecurityPolicyEntity): Pair<Boolean, String?> {
        val age = ageStr.toIntOrNull()
        if (age == null) {
            return false to "Ingrese una edad válida en números."
        }
        if (age < policy.minAge) {
            return false to "Debes ser mayor o igual a ${policy.minAge} años para registrarte."
        }
        if (age > 120) {
            return false to "Ingrese una edad válida."
        }
        return true to null
    }

    /**
     * Valida los parámetros de configuración de seguridad ingresados por el administrador.
     * Retorna un mensaje de error si algún parámetro es negativo, cero o inconsistente.
     */
    fun validateSecurityPolicyInputs(
        maxLoginAttemptsStr: String,
        lockoutDurationSecondsStr: String,
        passwordMinLengthStr: String,
        passwordMaxLengthStr: String,
        minAgeStr: String,
        maxNameLengthStr: String
    ): Pair<Boolean, String?> {
        val maxAttempts = maxLoginAttemptsStr.toIntOrNull()
        if (maxAttempts == null || maxAttempts <= 0) {
            return false to "El número de intentos de inicio de sesión debe ser un valor positivo mayor a 0 (ej. 3)."
        }

        val lockoutSeconds = lockoutDurationSecondsStr.toIntOrNull()
        if (lockoutSeconds == null || lockoutSeconds <= 0) {
            return false to "El tiempo de bloqueo no puede ser negativo ni 0 (debe ser mayor a 0 segundos)."
        }

        val minPwd = passwordMinLengthStr.toIntOrNull()
        if (minPwd == null || minPwd < 4) {
            return false to "La longitud mínima de contraseña debe ser de al menos 4 caracteres."
        }

        val maxPwd = passwordMaxLengthStr.toIntOrNull()
        if (maxPwd == null || maxPwd <= 0) {
            return false to "La longitud máxima de contraseña no puede ser negativa ni 0."
        }

        if (maxPwd < minPwd) {
            return false to "La longitud máxima de contraseña no puede ser menor a la longitud mínima."
        }

        val minAge = minAgeStr.toIntOrNull()
        if (minAge == null || minAge <= 0) {
            return false to "La edad mínima de registro no puede ser negativa ni 0."
        }

        val maxName = maxNameLengthStr.toIntOrNull()
        if (maxName == null || maxName <= 0) {
            return false to "La longitud máxima de nombres no puede ser negativa ni 0."
        }

        return true to null
    }


    /**
     * Normaliza y extrae la entidad bancaria principal a partir del nombre comercial
     * de una cuenta o proveedor de billetera (ej. Banpro, BAC, LAFISE, BDF, Ficohsa, Avanz).
     * Garantiza que una billetera solo pueda vincularse a una cuenta del mismo banco.
     */
    fun normalizeBankBrand(name: String): String {
        val upper = name.uppercase(java.util.Locale.ROOT)
        return when {
            upper.contains("BANPRO") -> "BANPRO"
            upper.contains("BAC") || upper.contains("KASH") -> "BAC"
            upper.contains("LAFISE") -> "LAFISE"
            upper.contains("BDF") -> "BDF"
            upper.contains("FICOHSA") -> "FICOHSA"
            upper.contains("AVANZ") -> "AVANZ"
            else -> upper.trim()
        }
    }
}
