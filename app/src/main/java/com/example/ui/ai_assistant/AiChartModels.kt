package com.example.ui.ai_assistant

/**
 * Modelos de datos para el motor de generación de gráficos y tablas del Asistente IA.
 * 
 * Qué hace:
 * Define las estructuras de datos que representan barras comparativas de ingresos vs gastos,
 * agrupaciones por categorías, semanas o meses, tablas ordenadas del año en curso
 * y listas estructuradas de recomendaciones financieras.
 * 
 * Por qué se implementa de esa forma:
 * Permite separar el modelo de datos analítico del renderizado en Compose y de la lógica de base de datos,
 * asegurando consistencia y tipado estricto en cada mensaje generado por el asistente virtual.
 * 
 * Cómo se relaciona con el resto del código:
 * Es consumido por AiFinancialAnalyticsEngine para estructurar las respuestas analíticas,
 * por FinanceViewModel en los ChatMessage, y por la interfaz de AiAssistantScreen para dibujar
 * los gráficos interactivos, tablas mensuales y tarjetas de recomendaciones.
 */

data class AiChartItem(
    val label: String,
    val income: Double,
    val expense: Double,
    val balance: Double = income - expense
)

data class AiChartData(
    val type: String, // "CATEGORIES", "WEEKS", "MONTHS", "CURRENT_YEAR_TABLE"
    val title: String,
    val subtitle: String = "",
    val items: List<AiChartItem>,
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val isTableOnly: Boolean = false
)
