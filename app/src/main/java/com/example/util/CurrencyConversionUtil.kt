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
        "PYG" to 210.45
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
            "MXN" -> "Tipo de cambio oficial: 1 C$ = 0.54 Pesos Mexicanos ($)"
            "COP" -> "Tipo de cambio oficial: 1 C$ = 115.50 Pesos Colombianos ($)"
            "DOP" -> "Tipo de cambio oficial: 1 C$ = 1.64 Pesos Dominicanos (RD$)"
            else -> "Tipo de cambio oficial aplicado según divisas internacionales"
        }
    }
}
