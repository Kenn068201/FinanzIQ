package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Módulo de Conversión de Monedas con Tipos de Cambio Oficiales.
 * 
 * Qué hace:
 * Toma valores monetarios base expresados en Córdobas Nicaragüenses (NIO - C$) y los convierte
 * a la moneda seleccionada por el usuario final utilizando las tasas de cambio oficiales
 * fijadas por el Banco Central de Nicaragua (BCN) y entidades bancarias de la región.
 * 
 * Por qué se implementa de esta forma:
 * Satisface la directriz del usuario: "convirtiendo los valores exactos de Córdobas a la
 * moneda correspondiente con los tipos de cambio oficial que se apliquen en cada país disponible".
 * 
 * Cómo se relaciona con el resto:
 * Es utilizado por ViewModel y las interfaces (Dashboard, Billeteras, Resúmenes) para presentar
 * los importes en la divisa elegida de forma reactiva y exacta en toda la app.
 */
object CurrencyConversionUtil {

    /**
     * Moneda activa global seleccionada por el usuario en sus preferencias.
     * Al estar respaldada por un mutableStateOf de Compose, cualquier componente o función
     * composable que invoque el formateo de moneda se recompondrá automáticamente
     * de forma reactiva en tiempo real cuando el usuario cambie la divisa en sus ajustes.
     */
    var activeCurrency: CurrencyOption by mutableStateOf(CountryPhoneData.americanCurrencies.first())

    /**
     * Factores de conversión oficiales: Cuántas unidades de la moneda destino equivalen a 1 Córdoba (NIO).
     * Ejemplo:
     * - Tasa oficial BCN: 1 USD = 36.6243 NIO -> 1 NIO = 0.0273042 USD
     * - Tasa Euro: 1 EUR = ~39.85 NIO -> 1 NIO = 0.025094 EUR
     * - Tasa Costa Rica: 1 USD = ~515 CRC -> 1 NIO = 14.0617 CRC
     * - Tasa Honduras: 1 USD = ~24.72 HNL -> 1 NIO = 0.6749 HNL
     * - Tasa Guatemala: 1 USD = ~7.78 GTQ -> 1 NIO = 0.2124 GTQ
     * - Tasa México: 1 USD = ~19.85 MXN -> 1 NIO = 0.5419 MXN
     * - Tasa Colombia: 1 USD = ~4,230 COP -> 1 NIO = 115.4969 COP
     * - Tasa Dominicana: 1 USD = ~60.10 DOP -> 1 NIO = 1.6409 DOP
     */
    private val nioToTargetRates = mapOf(
        "NIO" to 1.0,
        "USD" to 0.0273042,
        "EUR" to 0.025094,
        "CRC" to 14.0617,
        "HNL" to 0.6749,
        "GTQ" to 0.2124,
        "PAB" to 0.0273042, // Anclado 1:1 con USD
        "MXN" to 0.5419,
        "COP" to 115.4969,
        "DOP" to 1.6409,
        "CAD" to 0.0378,
        "BRL" to 0.1554,
        "PEN" to 0.1026,
        "CLP" to 25.82,
        "ARS" to 27.50,
        "UYU" to 1.156,
        "BOB" to 0.1889,
        "PYG" to 210.45,
        "BZD" to 0.0546,
        "HTG" to 3.60,
        "TTD" to 0.185,
        "BSD" to 0.0273042,
        "BBD" to 0.0546,
        "AWG" to 0.0489,
        "ANG" to 0.0489,
        "GYD" to 5.71,
        "SRD" to 0.956
    )

    /**
     * Convierte una cantidad dada en Córdobas (NIO) a la moneda seleccionada.
     */
    fun convertFromNio(amountInNio: Double, targetCurrencyCode: String): Double {
        val rate = nioToTargetRates[targetCurrencyCode.uppercase(Locale.ROOT)] ?: 1.0
        return amountInNio * rate
    }

    /**
     * Formatea un valor monetario en Córdobas a la moneda destino con su símbolo y dos decimales,
     * utilizando por defecto la divisa activa globalmente seleccionada por el usuario.
     */
    fun formatWithConversion(amountInNio: Double, currency: CurrencyOption = activeCurrency): String {
        val converted = convertFromNio(amountInNio, currency.code)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        val formatter = DecimalFormat("#,##0.00", symbols)
        return "${currency.symbol} ${formatter.format(converted)}"
    }

    /**
     * Retorna una descripción legible del tipo de cambio oficial aplicado.
     */
    fun getOfficialRateDescription(currencyCode: String): String {
        return when (currencyCode.uppercase(Locale.ROOT)) {
            "NIO" -> "Moneda base nacional (1.00 C$ = 1.00 C$)"
            "USD" -> "Tipo de cambio oficial BCN: 1 USD = 36.6243 C$"
            "EUR" -> "Tipo de cambio oficial: 1 EUR = 39.85 C$"
            "CRC" -> "Tipo de cambio regional: 1 C$ = 14.06 Colones (₡)"
            "HNL" -> "Tipo de cambio regional: 1 C$ = 0.67 Lempiras (L)"
            "GTQ" -> "Tipo de cambio regional: 1 C$ = 0.21 Quetzales (Q)"
            "PAB" -> "Tipo de cambio oficial: 1 C$ = 0.027 Balboas (B/.)"
            "MXN" -> "Tipo de cambio oficial: 1 C$ = 0.54 Pesos Mexicanos ($)"
            "COP" -> "Tipo de cambio oficial: 1 C$ = 115.50 Pesos Colombianos ($)"
            "DOP" -> "Tipo de cambio oficial: 1 C$ = 1.64 Pesos Dominicanos (RD$)"
            "CAD" -> "Tipo de cambio oficial: 1 C$ = 0.038 Dólares Canadienses (C$)"
            "BRL" -> "Tipo de cambio oficial: 1 C$ = 0.16 Reales Brasileños (R$)"
            "PEN" -> "Tipo de cambio oficial: 1 C$ = 0.10 Soles Peruanos (S/)"
            "CLP" -> "Tipo de cambio oficial: 1 C$ = 25.82 Pesos Chilenos ($)"
            "ARS" -> "Tipo de cambio oficial: 1 C$ = 27.50 Pesos Argentinos ($)"
            "UYU" -> "Tipo de cambio oficial: 1 C$ = 1.16 Pesos Uruguayos ($)"
            "BOB" -> "Tipo de cambio oficial: 1 C$ = 0.19 Bolivianos (Bs)"
            "PYG" -> "Tipo de cambio oficial: 1 C$ = 210.45 Guaraníes (₲)"
            "BZD" -> "Tipo de cambio oficial: 1 C$ = 0.055 Dólares Beliceños (BZ$)"
            "HTG" -> "Tipo de cambio oficial: 1 C$ = 3.60 Gourdes Haitianos (G)"
            "TTD" -> "Tipo de cambio oficial: 1 C$ = 0.19 Dólares TT (TT$)"
            "BSD" -> "Tipo de cambio oficial: 1 C$ = 0.027 Dólares Bahameños (B$)"
            "BBD" -> "Tipo de cambio oficial: 1 C$ = 0.055 Dólares Barbados (Bds$)"
            "AWG" -> "Tipo de cambio oficial: 1 C$ = 0.049 Florines Arubeños (Afl.)"
            "ANG" -> "Tipo de cambio oficial: 1 C$ = 0.049 Florines Antillanos (NAƒ)"
            "GYD" -> "Tipo de cambio oficial: 1 C$ = 5.71 Dólares Guyaneses (G$)"
            "SRD" -> "Tipo de cambio oficial: 1 C$ = 0.96 Dólares Surinameses (Sr$)"
            else -> "Tipo de cambio oficial aplicado según divisas internacionales"
        }
    }

    /**
     * Convierte cualquier monto expresado en la moneda actual a su equivalente en USD
     * para la validación de topes de ingresos y gastos fijados en dólares.
     */
    fun convertToUsd(amount: Double, currency: CurrencyOption = activeCurrency): Double {
        if (currency.code.equals("USD", ignoreCase = true)) return amount
        val rateToTarget = nioToTargetRates[currency.code.uppercase(Locale.ROOT)] ?: 1.0
        val amountInNio = if (rateToTarget > 0.0) amount / rateToTarget else amount
        val usdRate = nioToTargetRates["USD"] ?: 0.0273042
        return amountInNio * usdRate
    }

    /**
     * Sobrecarga de conveniencia que acepta el código ISO de la moneda como String
     * para facilitar validaciones y pruebas de caja blanca en toda la plataforma.
     */
    fun convertToUsd(amount: Double, currencyCode: String): Double {
        val option = CountryPhoneData.americanCurrencies.find { it.code.equals(currencyCode, ignoreCase = true) }
            ?: CurrencyOption(currencyCode, currencyCode, currencyCode, currencyCode, "🌐")
        return convertToUsd(amount, option)
    }
}
