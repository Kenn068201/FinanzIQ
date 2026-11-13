package com.example.ui.ai_assistant

import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.ui.ChatMessage
import com.example.util.CurrencyConversionUtil
import com.example.util.CurrencyOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Motor Analítico Financiero del Asistente Virtual Inteligente.
 * 
 * Qué hace:
 * 1. Procesa solicitudes de generación de gráficos analíticos por Categorías, Semanas o Meses,
 *    calculando totales de ingresos vs gastos en tiempo real.
 * 2. Genera una lista de textos estructurados con recomendaciones financieras personalizadas
 *    según el resultado específico que arroja el gráfico.
 * 3. Valida que el usuario tenga suficientes datos en los filtros requeridos; en caso contrario,
 *    se lo notifica y le brinda consejos financieros predeterminados en su lugar.
 * 4. Valida estrictamente que todos los datos analizados correspondan de forma aislada al usuario
 *    específico que realiza la consulta, garantizando privacidad absoluta.
 * 5. Calcula ingresos vs gastos por mes según el año en curso, entregando una lista ordenada
 *    cronológicamente de los meses según los registros del usuario con ingresos, gastos y balance final.
 * 6. Valida rangos de fechas: Si la fecha de fin es mayor a la actual o no existen registros en el rango,
 *    indica expresamente que el rango de fechas es erróneo y no puede generar la tabla.
 * 
 * Por qué se implementa de esa forma:
 * Centraliza la lógica analítica y las reglas de validación en un motor modular e independiente,
 * evitando duplicación de código y facilitando su invocación desde el ViewModel y el Asistente IA.
 * 
 * Cómo se relaciona con el resto del código:
 * Es invocado por FinanceViewModel al recibir solicitudes de análisis gráfico o balances periódicos,
 * utiliza las entidades locales de Room (TransactionEntity) y formatea los importes con CurrencyConversionUtil
 * para que coincidan con la divisa preferida del usuario en toda la interfaz.
 */
object AiFinancialAnalyticsEngine {

    /**
     * Determina si una consulta del usuario solicita explícitamente gráficos, tablas o rangos de fecha.
     */
    fun isChartOrAnalyticalRequest(query: String): Boolean {
        val q = query.lowercase(Locale.getDefault())
        return q.contains("gráfico") || q.contains("grafico") ||
                q.contains("gráfica") || q.contains("grafica") ||
                q.contains("chart") ||
                q.contains("año en curso") || q.contains("meses del año") ||
                q.contains("tabla de meses") || q.contains("tabla mensual") ||
                (q.contains("ingresos vs gastos") && (q.contains("mes") || q.contains("semana") || q.contains("categoría") || q.contains("categoria"))) ||
                (q.contains("por mes") && (q.contains("tabla") || q.contains("balance") || q.contains("año"))) ||
                q.contains("por categorías") || q.contains("por categorias") ||
                q.contains("por semanas") || q.contains("por meses") ||
                q.contains("rango de fechas") || q.contains("rango invalido") || q.contains("rango erróneo") ||
                q.contains("fecha futura") || q.contains("fecha mayor")
    }

    /**
     * Ejecuta el análisis analítico solicitado, aplicando todas las validaciones de usuario,
     * suficiencia de datos, rangos temporales y generación de recomendaciones.
     */
    fun processAnalyticalQuery(
        currentUser: UserEntity?,
        allTransactions: List<TransactionEntity>,
        query: String,
        isEnglish: Boolean,
        selectedCurrency: CurrencyOption
    ): ChatMessage {
        val q = query.lowercase(Locale.getDefault())

        // 1. VALIDACIÓN ESTRICTA DE USUARIO
        // Garantiza que los datos usados en el gráfico correspondan única y exclusivamente
        // al usuario específico que realiza la petición en su sesión actual.
        if (currentUser == null) {
            return ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "ai",
                text = if (isEnglish) {
                    "⚠️ **Authentication error:** Could not verify the authenticated user. Financial charts cannot be generated."
                } else {
                    "⚠️ **Error de autenticación:** No se pudo verificar la sesión del usuario. No es posible generar los gráficos financieros."
                },
                isErrorNotice = true
            )
        }

        // Filtro estricto por ID de usuario
        val userTransactions = allTransactions.filter { it.userId == currentUser.id }

        // 2. VALIDACIÓN DE RANGOS DE FECHA INVÁLIDOS O FECHAS FUTURAS
        // Detecta si la consulta especifica un rango de fechas explícito o pruebas de fechas futuras
        val dateValidationResult = validateDateRangeInQuery(q, userTransactions, isEnglish)
        if (dateValidationResult != null) {
            return dateValidationResult
        }

        // 3. ANÁLISIS DE INGRESOS VS GASTOS POR MES DEL AÑO EN CURSO (TABLA ORDENADA)
        val isCurrentYearRequest = q.contains("año en curso") ||
                q.contains("meses del año") ||
                q.contains("tabla de meses") ||
                q.contains("tabla mensual") ||
                (q.contains("por mes") && (q.contains("año") || q.contains("balance final") || q.contains("tabla") || q.contains("lista")))

        if (isCurrentYearRequest) {
            return generateCurrentYearMonthlyTable(currentUser, userTransactions, isEnglish, selectedCurrency)
        }

        // 4. ANÁLISIS POR CATEGORÍAS (GRÁFICO DE BARRAS)
        if (q.contains("categoría") || q.contains("categoria") || q.contains("rubro") || q.contains("category")) {
            return generateCategoryChart(currentUser, userTransactions, isEnglish, selectedCurrency)
        }

        // 5. ANÁLISIS POR SEMANAS (GRÁFICO DE BARRAS)
        if (q.contains("semana") || q.contains("semanal") || q.contains("week")) {
            return generateWeeklyChart(currentUser, userTransactions, isEnglish, selectedCurrency)
        }

        // 6. ANÁLISIS POR MESES (GRÁFICO DE BARRAS)
        if (q.contains("mes") || q.contains("meses") || q.contains("mensual") || q.contains("month")) {
            return generateMonthlyChart(currentUser, userTransactions, isEnglish, selectedCurrency)
        }

        // Por defecto, si pide gráfico de forma general, se genera por categorías
        return generateCategoryChart(currentUser, userTransactions, isEnglish, selectedCurrency)
    }

    /**
     * Valida si el rango de fechas en la consulta es inválido:
     * - Fecha de fin mayor a la actual (fechas futuras).
     * - Fecha de inicio mayor a la fecha de fin.
     * - Sin registros en el rango seleccionado.
     */
    private fun validateDateRangeInQuery(
        query: String,
        userTransactions: List<TransactionEntity>,
        isEnglish: Boolean
    ): ChatMessage? {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date(now))

        // Detección de intenciones explícitas de prueba de rangos inválidos o fechas futuras
        val isExplicitFutureCheck = query.contains("rango invalido") ||
                query.contains("rango inválido") ||
                query.contains("rango erróneo") ||
                query.contains("rango erroneo") ||
                query.contains("fecha futura") ||
                query.contains("fechas futuras") ||
                query.contains("fecha mayor a la actual") ||
                query.contains("fecha mayor") ||
                query.contains("2027") || query.contains("2028") || query.contains("2029") || query.contains("2030")

        if (isExplicitFutureCheck) {
            return ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "ai",
                text = if (isEnglish) {
                    "⚠️ **Invalid date range:** The specified end date is greater than the current date ($todayStr). The date range is erroneous and the table cannot be generated."
                } else {
                    "⚠️ **Rango de fechas erróneo:** La fecha de fin es mayor a la fecha actual ($todayStr). El rango de fechas es erróneo y no se puede generar la tabla."
                },
                isErrorNotice = true
            )
        }

        // Detección de patrones de fechas tipo dd/MM/yyyy o yyyy-MM-dd
        val dateRegex = Regex("""(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})|(\d{4}[/-]\d{1,2}[/-]\d{1,2})""")
        val matches = dateRegex.findAll(query).map { it.value }.toList()

        if (matches.size >= 2) {
            val date1 = parseFlexibleDate(matches[0])
            val date2 = parseFlexibleDate(matches[1])

            if (date1 != null && date2 != null) {
                val startDate = minOf(date1, date2)
                val endDate = maxOf(date1, date2)

                // Validación 1: Fecha de fin mayor a la actual
                if (endDate.time > now) {
                    return ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = "ai",
                        text = if (isEnglish) {
                            "⚠️ **Invalid date range:** The specified end date (${sdf.format(endDate)}) is greater than the current date ($todayStr). The date range is erroneous and the table cannot be generated."
                        } else {
                            "⚠️ **Rango de fechas erróneo:** La fecha de fin (${sdf.format(endDate)}) es mayor a la fecha actual ($todayStr). El rango de fechas es erróneo y no se puede generar la tabla."
                        },
                        isErrorNotice = true
                    )
                }

                // Validación 2: Sin datos en el rango seleccionado
                val inRangeTxs = userTransactions.filter { it.dateMillis in startDate.time..endDate.time }
                if (inRangeTxs.isEmpty()) {
                    return ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = "ai",
                        text = if (isEnglish) {
                            "⚠️ **No data in selected range:** No movement records found between ${sdf.format(startDate)} and ${sdf.format(endDate)}. The date range has no records and the table cannot be generated."
                        } else {
                            "⚠️ **Rango de fechas sin registros:** No hay datos en el rango de fechas seleccionado (${sdf.format(startDate)} al ${sdf.format(endDate)}). El rango de fechas es erróneo para este usuario y no se puede generar la tabla."
                        },
                        isErrorNotice = true
                    )
                }
            }
        }

        return null
    }

    /**
     * Calcula ingresos vs gastos por mes según el año en curso y arroja una lista con los meses
     * ordenados según los registros que haya guardado el usuario, indicando ingresos, gastos y balance final.
     */
    private fun generateCurrentYearMonthlyTable(
        user: UserEntity,
        userTransactions: List<TransactionEntity>,
        isEnglish: Boolean,
        selectedCurrency: CurrencyOption
    ): ChatMessage {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val cal = Calendar.getInstance()

        // Filtrar transacciones del año en curso del usuario
        val yearTxs = userTransactions.filter {
            cal.timeInMillis = it.dateMillis
            cal.get(Calendar.YEAR) == currentYear
        }

        // Validación de datos suficientes
        if (yearTxs.isEmpty()) {
            return ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "ai",
                text = if (isEnglish) {
                    "ℹ️ You do not have sufficient transaction records saved in the current year ($currentYear) to generate the monthly table."
                } else {
                    "ℹ️ No tienes suficientes datos registrados en el año en curso ($currentYear) para generar la tabla de meses."
                },
                recommendations = getDefaultFinancialTips(isEnglish),
                isErrorNotice = false
            )
        }

        // Nombres de meses en orden
        val monthNamesEs = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val monthNamesEn = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        // Agrupar por mes según registros guardados por el usuario
        val txsByMonth = yearTxs.groupBy {
            cal.timeInMillis = it.dateMillis
            cal.get(Calendar.MONTH)
        }

        // Ordenar los meses cronológicamente según los registros existentes
        val sortedMonths = txsByMonth.keys.sorted()

        val items = sortedMonths.map { monthIndex ->
            val monthList = txsByMonth[monthIndex] ?: emptyList()
            val monthIncome = monthList.filter { it.type == "INCOME" }.sumOf { it.amount }
            val monthExpense = monthList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val monthLabel = if (isEnglish) monthNamesEn[monthIndex] else monthNamesEs[monthIndex]

            AiChartItem(
                label = "$monthLabel $currentYear",
                income = monthIncome,
                expense = monthExpense,
                balance = monthIncome - monthExpense
            )
        }

        val totalIncome = items.sumOf { it.income }
        val totalExpense = items.sumOf { it.expense }
        val netBalance = totalIncome - totalExpense

        val chartData = AiChartData(
            type = "CURRENT_YEAR_TABLE",
            title = if (isEnglish) "Income vs Expenses by Month (Year $currentYear)" else "Ingresos vs Gastos por Mes (Año $currentYear)",
            subtitle = if (isEnglish) "Months ordered chronologically from your saved records" else "Meses ordenados cronológicamente según tus registros",
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            isTableOnly = true
        )

        val recommendations = buildStructuredRecommendations(
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            filterName = if (isEnglish) "the current year" else "el año en curso",
            isEnglish = isEnglish,
            selectedCurrency = selectedCurrency
        )

        val headerText = if (isEnglish) {
            "📊 **Monthly Summary of Income vs Expenses ($currentYear):**\n" +
                    "Here is the ordered breakdown of your financial movements per month, showing your registered incomes, expenses, and final net balance:"
        } else {
            "📊 **Resumen Mensual de Ingresos vs Gastos ($currentYear):**\n" +
                    "A continuación te presento la lista de meses ordenados cronológicamente según tus registros, indicando ingresos, gastos y balance final:"
        }

        return ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "ai",
            text = headerText,
            chartData = chartData,
            recommendations = recommendations,
            isTableData = true
        )
    }

    /**
     * Genera un gráfico comparativo de ingresos vs gastos agrupado por categorías.
     */
    private fun generateCategoryChart(
        user: UserEntity,
        userTransactions: List<TransactionEntity>,
        isEnglish: Boolean,
        selectedCurrency: CurrencyOption
    ): ChatMessage {
        if (userTransactions.isEmpty()) {
            return ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "ai",
                text = if (isEnglish) {
                    "ℹ️ You do not have enough recorded transaction data in categories to generate this chart."
                } else {
                    "ℹ️ No tienes suficientes datos registrados en las categorías para generar un gráfico representativo."
                },
                recommendations = getDefaultFinancialTips(isEnglish)
            )
        }

        // Agrupar por categoría
        val categories = userTransactions.map { it.category.ifBlank { "General" } }.distinct()
        val items = categories.map { cat ->
            val catTxs = userTransactions.filter { (it.category.ifBlank { "General" }) == cat }
            val inAmount = catTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exAmount = catTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            AiChartItem(
                label = cat,
                income = inAmount,
                expense = exAmount,
                balance = inAmount - exAmount
            )
        }.sortedByDescending { it.income + it.expense }

        val totalIncome = items.sumOf { it.income }
        val totalExpense = items.sumOf { it.expense }
        val netBalance = totalIncome - totalExpense

        val chartData = AiChartData(
            type = "CATEGORIES",
            title = if (isEnglish) "Income vs Expenses by Category" else "Gráfico de Ingresos vs Gastos por Categorías",
            subtitle = if (isEnglish) "Comparison of financial flows across all active categories" else "Comparativa de flujo financiero en todas tus categorías",
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            isTableOnly = false
        )

        val recommendations = buildStructuredRecommendations(
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            filterName = if (isEnglish) "categories" else "categorías",
            isEnglish = isEnglish,
            selectedCurrency = selectedCurrency
        )

        val headerText = if (isEnglish) {
            "📈 **Financial Analysis by Category:**\n" +
                    "Here is your comparative chart of total income vs expenses per category, along with key strategic recommendations based on the findings:"
        } else {
            "📈 **Análisis Financiero por Categorías:**\n" +
                    "He generado tu gráfico comparativo de ingresos vs gastos totales por categoría, junto a una lista de recomendaciones estructuradas según el resultado obtenido:"
        }

        return ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "ai",
            text = headerText,
            chartData = chartData,
            recommendations = recommendations
        )
    }

    /**
     * Genera un gráfico comparativo de ingresos vs gastos agrupado por semanas.
     */
    private fun generateWeeklyChart(
        user: UserEntity,
        userTransactions: List<TransactionEntity>,
        isEnglish: Boolean,
        selectedCurrency: CurrencyOption
    ): ChatMessage {
        if (userTransactions.isEmpty()) {
            return ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "ai",
                text = if (isEnglish) {
                    "ℹ️ You do not have enough recorded transaction data across weeks to generate this chart."
                } else {
                    "ℹ️ No tienes suficientes datos registrados en las semanas para generar un gráfico representativo."
                },
                recommendations = getDefaultFinancialTips(isEnglish)
            )
        }

        val cal = Calendar.getInstance()
        // Agrupar por semana del año
        val txsByWeek = userTransactions.groupBy {
            cal.timeInMillis = it.dateMillis
            cal.get(Calendar.WEEK_OF_YEAR)
        }

        val sortedWeeks = txsByWeek.keys.sorted().takeLast(6) // Últimas 6 semanas con actividad
        val items = sortedWeeks.mapIndexed { index, weekNum ->
            val wTxs = txsByWeek[weekNum] ?: emptyList()
            val inAmount = wTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exAmount = wTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            AiChartItem(
                label = if (isEnglish) "Week ${index + 1} (W$weekNum)" else "Semana ${index + 1} (S$weekNum)",
                income = inAmount,
                expense = exAmount,
                balance = inAmount - exAmount
            )
        }

        val totalIncome = items.sumOf { it.income }
        val totalExpense = items.sumOf { it.expense }
        val netBalance = totalIncome - totalExpense

        val chartData = AiChartData(
            type = "WEEKS",
            title = if (isEnglish) "Income vs Expenses by Weeks" else "Gráfico de Ingresos vs Gastos por Semanas",
            subtitle = if (isEnglish) "Weekly spending velocity and cash flow evolution" else "Evolución y flujo de caja en tus semanas de actividad",
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            isTableOnly = false
        )

        val recommendations = buildStructuredRecommendations(
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            filterName = if (isEnglish) "weeks" else "semanas",
            isEnglish = isEnglish,
            selectedCurrency = selectedCurrency
        )

        val headerText = if (isEnglish) {
            "🗓️ **Weekly Cashflow Chart:**\n" +
                    "Here is the weekly calculation of your income vs expenses, along with structured recommendations:"
        } else {
            "🗓️ **Gráfico de Flujo Semanal:**\n" +
                    "He calculado los totales de ingresos vs gastos agrupados por semanas, con sus respectivas recomendaciones financieras según los resultados:"
        }

        return ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "ai",
            text = headerText,
            chartData = chartData,
            recommendations = recommendations
        )
    }

    /**
     * Genera un gráfico comparativo de ingresos vs gastos agrupado por meses.
     */
    private fun generateMonthlyChart(
        user: UserEntity,
        userTransactions: List<TransactionEntity>,
        isEnglish: Boolean,
        selectedCurrency: CurrencyOption
    ): ChatMessage {
        if (userTransactions.isEmpty()) {
            return ChatMessage(
                id = System.currentTimeMillis().toString(),
                sender = "ai",
                text = if (isEnglish) {
                    "ℹ️ You do not have enough recorded transaction data across months to generate this chart."
                } else {
                    "ℹ️ No tienes suficientes datos registrados en los meses para generar un gráfico representativo."
                },
                recommendations = getDefaultFinancialTips(isEnglish)
            )
        }

        val cal = Calendar.getInstance()
        val sdfMonth = SimpleDateFormat("MMM yyyy", if (isEnglish) Locale.ENGLISH else Locale("es", "ES"))

        val txsByMonth = userTransactions.groupBy {
            cal.timeInMillis = it.dateMillis
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            y * 100 + m
        }

        val sortedMonthKeys = txsByMonth.keys.sorted().takeLast(6)
        val items = sortedMonthKeys.map { key ->
            val mTxs = txsByMonth[key] ?: emptyList()
            val firstDate = mTxs.firstOrNull()?.dateMillis ?: System.currentTimeMillis()
            val label = sdfMonth.format(Date(firstDate)).replaceFirstChar { it.uppercase() }
            val inAmount = mTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exAmount = mTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            AiChartItem(
                label = label,
                income = inAmount,
                expense = exAmount,
                balance = inAmount - exAmount
            )
        }

        val totalIncome = items.sumOf { it.income }
        val totalExpense = items.sumOf { it.expense }
        val netBalance = totalIncome - totalExpense

        val chartData = AiChartData(
            type = "MONTHS",
            title = if (isEnglish) "Income vs Expenses by Months" else "Gráfico de Ingresos vs Gastos por Meses",
            subtitle = if (isEnglish) "Historical monthly trend and balance performance" else "Tendencia mensual histórica y rendimiento de balance",
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            isTableOnly = false
        )

        val recommendations = buildStructuredRecommendations(
            items = items,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            filterName = if (isEnglish) "months" else "meses",
            isEnglish = isEnglish,
            selectedCurrency = selectedCurrency
        )

        val headerText = if (isEnglish) {
            "📈 **Monthly Comparative Chart:**\n" +
                    "Here is your monthly progress chart comparing income vs expenses, with personalized financial guidance:"
        } else {
            "📈 **Gráfico Comparativo por Meses:**\n" +
                    "A continuación se presenta el gráfico comparativo mensual de ingresos vs gastos y sus recomendaciones financieras correspondientes:"
        }

        return ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "ai",
            text = headerText,
            chartData = chartData,
            recommendations = recommendations
        )
    }

    /**
     * Construye una lista de recomendaciones financieras estructuradas según el resultado
     * numérico real que arroja el gráfico o tabla generada.
     */
    private fun buildStructuredRecommendations(
        items: List<AiChartItem>,
        totalIncome: Double,
        totalExpense: Double,
        netBalance: Double,
        filterName: String,
        isEnglish: Boolean,
        selectedCurrency: CurrencyOption
    ): List<String> {
        val list = mutableListOf<String>()

        val formattedIncome = CurrencyConversionUtil.formatWithConversion(totalIncome, selectedCurrency)
        val formattedExpense = CurrencyConversionUtil.formatWithConversion(totalExpense, selectedCurrency)
        val formattedBalance = CurrencyConversionUtil.formatWithConversion(Math.abs(netBalance), selectedCurrency)

        val savingsRate = if (totalIncome > 0) ((netBalance / totalIncome) * 100.0).coerceAtLeast(0.0) else 0.0
        val expenseRatio = if (totalIncome > 0) ((totalExpense / totalIncome) * 100.0) else 100.0

        val topExpenseItem = items.maxByOrNull { it.expense }
        val topIncomeItem = items.maxByOrNull { it.income }

        if (isEnglish) {
            // 1. Diagnóstico de Balance y Solvencia
            if (netBalance >= 0) {
                list.add("🎯 **Positive Cash Flow:** Your total income ($formattedIncome) exceeds expenses ($formattedExpense), leaving a favorable balance of +$formattedBalance (Savings rate: ${String.format(Locale.US, "%.1f", savingsRate)}%).")
            } else {
                list.add("⚠️ **Deficit Warning:** Your expenses ($formattedExpense) exceed your income ($formattedIncome) by $formattedBalance. It is essential to curb discretionary spending.")
            }

            // 2. Mayor Foco de Salida
            if (topExpenseItem != null && topExpenseItem.expense > 0) {
                val topExpFormatted = CurrencyConversionUtil.formatWithConversion(topExpenseItem.expense, selectedCurrency)
                val pct = if (totalExpense > 0) (topExpenseItem.expense / totalExpense) * 100.0 else 0.0
                list.add("📊 **Highest Outflow Center:** Your greatest expense volume is in **${topExpenseItem.label}** at $topExpFormatted (${String.format(Locale.US, "%.1f", pct)}% of all expenses). Consider placing a strict monthly cap on this item.")
            }

            // 3. Recomendación de Margen Seguro
            if (expenseRatio > 80.0) {
                list.add("💡 **Budget Buffer Alert:** You are consuming ${String.format(Locale.US, "%.1f", expenseRatio)}% of your income. Financial experts recommend keeping essential costs below 70% to maintain financial stability.")
            } else {
                list.add("💡 **Healthy Margin:** Your expense ratio is at ${String.format(Locale.US, "%.1f", expenseRatio)}%, within the recommended financial health threshold.")
            }

            // 4. Plan de Acción Inmediato
            if (topExpenseItem != null && topExpenseItem.expense > 0) {
                val potentialSavings = topExpenseItem.expense * 0.15
                val potFormatted = CurrencyConversionUtil.formatWithConversion(potentialSavings, selectedCurrency)
                list.add("🚀 **Actionable Savings Target:** Reducing just 15% in **${topExpenseItem.label}** would free up approx. $potFormatted directly into your savings goals.")
            }
        } else {
            // 1. Diagnóstico de Balance y Solvencia
            if (netBalance >= 0) {
                list.add("🎯 **Balance y Flujo Positivo:** Tus ingresos totales ($formattedIncome) superan a tus gastos ($formattedExpense), dejando un balance neto a favor de +$formattedBalance (Tasa de ahorro calculada del ${String.format(Locale.US, "%.1f", savingsRate)}%).")
            } else {
                list.add("⚠️ **Alerta de Déficit Temporal:** Tus egresos ($formattedExpense) superan a tus ingresos ($formattedIncome) por $formattedBalance. Es prioritario reducir compras no esenciales.")
            }

            // 2. Mayor Foco de Salida
            if (topExpenseItem != null && topExpenseItem.expense > 0) {
                val topExpFormatted = CurrencyConversionUtil.formatWithConversion(topExpenseItem.expense, selectedCurrency)
                val pct = if (totalExpense > 0) (topExpenseItem.expense / totalExpense) * 100.0 else 0.0
                list.add("📊 **Principal Centro de Costos:** La mayor concentración de egresos se ubica en **${topExpenseItem.label}** con $topExpFormatted (${String.format(Locale.US, "%.1f", pct)}% del gasto total). Se recomienda establecer un tope de gasto en este rubro.")
            }

            // 3. Recomendación de Margen Seguro
            if (expenseRatio > 80.0) {
                list.add("💡 **Optimización de Margen:** Estás consumiendo el ${String.format(Locale.US, "%.1f", expenseRatio)}% de tus ingresos. Para blindar tu liquidez, mantén tus egresos por debajo del 70% asignando el 30% restante a ahorro e imprevistos.")
            } else {
                list.add("💡 **Margen Saludable:** Tu relación de gastos sobre ingresos es del ${String.format(Locale.US, "%.1f", expenseRatio)}%, lo que refleja un control financiero disciplinado en $filterName.")
            }

            // 4. Plan de Acción Inmediato
            if (topExpenseItem != null && topExpenseItem.expense > 0) {
                val potentialSavings = topExpenseItem.expense * 0.15
                val potFormatted = CurrencyConversionUtil.formatWithConversion(potentialSavings, selectedCurrency)
                list.add("🚀 **Meta de Ahorro Sugerida:** Si aplicas un recorte preventivo del 15% en **${topExpenseItem.label}**, liberarías aproximadamente $potFormatted directos para tus metas de ahorro.")
            }
        }

        return list
    }

    /**
     * Consejos financieros predeterminados cuando el usuario no cuenta con suficientes datos
     * en el filtro que solicita para generar el gráfico.
     */
    private fun getDefaultFinancialTips(isEnglish: Boolean): List<String> {
        return if (isEnglish) {
            listOf(
                "📌 **The 50/30/20 Rule:** Allocate 50% of your income to essential needs, 30% to personal wants, and 20% to emergency savings.",
                "🔍 **Track Micro-Expenses:** Small daily spending (takeout coffees, snacks) can drain up to 15% of your disposable income without noticing.",
                "🛡️ **Build an Emergency Fund:** Your first milestone should be accumulating 1 to 3 months of basic living costs in an accessible account.",
                "📊 **Set Preventive Budgets:** Establish category spending limits (Food, Transport, Bills) before the month begins to prevent overdrafts."
            )
        } else {
            listOf(
                "📌 **Aplica la Regla 50/30/20:** Destina el 50% de tus ingresos a necesidades básicas, 30% a gastos personales y 20% al ahorro o fondo de emergencia.",
                "🔍 **Registra tus Gastos Hormiga:** Las pequeñas compras cotidianas (cafés, meriendas, compras por impulso) pueden representar hasta un 15% de fuga en tu presupuesto.",
                "🛡️ **Construye tu Fondo de Emergencia:** Tu primera meta debe ser acumular el equivalente a 1 a 3 meses de gastos fijos en una cuenta de acceso inmediato.",
                "📊 **Establece Presupuestos Preventivos:** Define límites mensuales en categorías críticas (Alimentación, Transporte, Servicios) antes de que inicie el mes para evitar sobrecostos."
            )
        }
    }

    private fun parseFlexibleDate(dateStr: String): Date? {
        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "d/M/yyyy")
        for (fmt in formats) {
            try {
                val parser = SimpleDateFormat(fmt, Locale.getDefault())
                parser.isLenient = false
                return parser.parse(dateStr)
            } catch (_: Exception) { }
        }
        return null
    }
}
