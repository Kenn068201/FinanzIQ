package com.example.util

import java.util.regex.Pattern

object Validators {
    // Matches standard email format: user@domain.com
    private val EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

    // First and last names must start with uppercase followed by lowercase letters (supports compound names)
    private val NAME_REGEX = Pattern.compile("^[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+( [A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)*\$")

    // Phone must start with 5, 7, or 8 and have exactly 8 digits
    private val PHONE_REGEX = Pattern.compile("^[578]\\d{7}\$")

    // Special characters
    private val SPECIAL_CHAR_REGEX = Pattern.compile("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]")

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matcher(email.trim()).matches()
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && NAME_REGEX.matcher(name.trim()).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && PHONE_REGEX.matcher(phone.trim()).matches()
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
}
