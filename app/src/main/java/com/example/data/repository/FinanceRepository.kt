package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BillReminderEntity
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.remote.GeminiApiClient
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinancialSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val projectedMonthEndExpense: Double,
    val savingsRatePercentage: Double,
    val budgetAlerts: List<String>,
    val automatedInsights: List<String>
)

data class CategoryExpenseBreakdown(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Double,
    val colorHex: String
)

class FinanceRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val budgetDao = db.budgetDao()
    private val savingsGoalDao = db.savingsGoalDao()
    private val billDao = db.billReminderDao()
    private val categoryDao = db.categoryDao()

    // --- Authentication & Users ---
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email.trim().lowercase(Locale.ROOT))
    suspend fun getUserByPhone(phone: String): UserEntity? = userDao.getUserByPhone(phone.trim())
    suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    suspend fun registerUser(user: UserEntity): Long = userDao.insertUser(user.copy(email = user.email.trim().lowercase(Locale.ROOT)))
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
    suspend fun getUserCount(): Int = userDao.getUserCount()

    // --- Transactions ---
    fun getTransactionsByUser(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByUser(userId)

    suspend fun getTransactionsList(userId: Long): List<TransactionEntity> =
        transactionDao.getTransactionsList(userId)

    suspend fun getAllTransactionsForAdmin(): List<TransactionEntity> =
        transactionDao.getAllTransactions()

    suspend fun addTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(id: Long) =
        transactionDao.deleteById(id)

    // Automatic classification helper based on title or keywords
    fun autoClassifyCategory(title: String, type: String): String {
        val lower = title.lowercase(Locale.ROOT)
        if (type == "INCOME") {
            return when {
                lower.contains("sueldo") || lower.contains("salario") || lower.contains("nómina") || lower.contains("quincena") -> "Salario"
                lower.contains("negocio") || lower.contains("venta") || lower.contains("cliente") || lower.contains("freelance") -> "Negocio Propio"
                lower.contains("interes") || lower.contains("inversion") || lower.contains("dividendo") || lower.contains("cripto") -> "Inversiones"
                else -> "Otros Ingresos"
            }
        } else {
            return when {
                lower.contains("super") || lower.contains("comida") || lower.contains("restaurante") || lower.contains("cena") || lower.contains("almuerzo") || lower.contains("desayuno") || lower.contains("mercado") || lower.contains("pizza") || lower.contains("cafe") || lower.contains("hamburguesa") -> "Alimentación"
                lower.contains("gasolina") || lower.contains("taxi") || lower.contains("uber") || lower.contains("bus") || lower.contains("peaje") || lower.contains("mecanico") || lower.contains("combustible") -> "Transporte"
                lower.contains("farmacia") || lower.contains("medico") || lower.contains("doctor") || lower.contains("medicina") || lower.contains("hospital") || lower.contains("dentista") -> "Salud"
                lower.contains("colegio") || lower.contains("universidad") || lower.contains("curso") || lower.contains("libro") || lower.contains("matricula") -> "Educación"
                lower.contains("cine") || lower.contains("netflix") || lower.contains("spotify") || lower.contains("juego") || lower.contains("fiesta") || lower.contains("bar") -> "Entretenimiento"
                lower.contains("luz") || lower.contains("agua") || lower.contains("internet") || lower.contains("cable") || lower.contains("enel") || lower.contains("claro") || lower.contains("tigo") -> "Servicios Básicos"
                lower.contains("alquiler") || lower.contains("renta") || lower.contains("casa") || lower.contains("mantenimiento") -> "Vivienda"
                lower.contains("ropa") || lower.contains("zapato") || lower.contains("camisa") || lower.contains("pantalon") || lower.contains("tienda") -> "Ropa y Calzado"
                else -> "Otros Gastos"
            }
        }
    }

    // --- Budgets ---
    fun getBudgetsByUser(userId: Long, monthYear: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsByUserAndMonth(userId, monthYear)

    fun getAllBudgetsByUser(userId: Long): Flow<List<BudgetEntity>> =
        budgetDao.getAllBudgetsByUser(userId)

    suspend fun setBudget(budget: BudgetEntity) =
        budgetDao.insertBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.deleteBudget(budget)

    // --- Savings Goals ---
    fun getSavingsGoals(userId: Long): Flow<List<SavingsGoalEntity>> =
        savingsGoalDao.getGoalsByUser(userId)

    suspend fun addSavingsGoal(goal: SavingsGoalEntity) =
        savingsGoalDao.insertGoal(goal)

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) =
        savingsGoalDao.updateGoal(goal)

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) =
        savingsGoalDao.deleteGoal(goal)

    // --- Bill Reminders ---
    fun getBillReminders(userId: Long): Flow<List<BillReminderEntity>> =
        billDao.getBillsByUser(userId)

    suspend fun addBillReminder(bill: BillReminderEntity) =
        billDao.insertBill(bill)

    suspend fun updateBillReminder(bill: BillReminderEntity) =
        billDao.updateBill(bill)

    suspend fun deleteBillReminder(bill: BillReminderEntity) =
        billDao.deleteBill(bill)

    // --- Categories ---
    fun getCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    suspend fun addCategory(category: CategoryEntity) =
        categoryDao.insertCategory(category)

    suspend fun deleteCategory(id: Long) =
        categoryDao.deleteCategoryById(id)

    suspend fun getCategoryCount(): Int = categoryDao.getCategoryCount()

    // --- Intelligent Analytics & Calculations ---
    fun calculateSummary(
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>
    ): FinancialSummary {
        var totalIncome = 0.0
        var totalExpense = 0.0

        val currentCal = Calendar.getInstance()
        val currentMonth = currentCal.get(Calendar.MONTH)
        val currentYear = currentCal.get(Calendar.YEAR)
        val dayOfMonth = currentCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val expensesByCategory = mutableMapOf<String, Double>()
        var currentMonthExpense = 0.0

        for (tx in transactions) {
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val isThisMonth = txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear

            if (tx.type == "INCOME") {
                totalIncome += tx.amount
            } else {
                totalExpense += tx.amount
                if (isThisMonth) {
                    currentMonthExpense += tx.amount
                    expensesByCategory[tx.category] = (expensesByCategory[tx.category] ?: 0.0) + tx.amount
                }
            }
        }

        val balance = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).coerceIn(0.0, 100.0) else 0.0

        // Prorated month-end spending prediction
        val projectedMonthEndExpense = if (dayOfMonth > 0) {
            (currentMonthExpense / dayOfMonth) * daysInMonth
        } else currentMonthExpense

        // Budget Alerts & Threshold Warnings
        val alerts = mutableListOf<String>()
        for (b in budgets) {
            val spent = expensesByCategory[b.category] ?: 0.0
            val pct = (spent / b.monthlyLimit) * 100
            if (pct >= 100) {
                alerts.add("⚠️ Has superado el 100% de tu presupuesto en '${b.category}' (Gastado: C$${String.format(Locale.US, "%.2f", spent)} / Límite: C$${String.format(Locale.US, "%.2f", b.monthlyLimit)})")
            } else if (pct >= 80) {
                alerts.add("⚡ Estás cerca del límite (${String.format(Locale.US, "%.0f", pct)}%) en '${b.category}'. Te quedan C$${String.format(Locale.US, "%.2f", b.monthlyLimit - spent)}")
            }
        }

        // Automated Recommendations & Insights
        val insights = mutableListOf<String>()
        val foodExpense = expensesByCategory["Alimentación"] ?: 0.0
        val transportExpense = expensesByCategory["Transporte"] ?: 0.0

        if (transportExpense > 1500) {
            insights.add("🚗 Este mes registraste un incremento considerable en transporte (C$${String.format(Locale.US, "%.2f", transportExpense)}). Considera optimizar rutas o viajes compartidos.")
        } else {
            insights.add("🚗 Gastos de transporte estables y dentro del rango saludable.")
        }

        if (foodExpense > 2500) {
            val fastFoodSavingEstimate = 1500.0
            insights.add("🍔 Si reduces salidas a restaurantes y comida rápida ahorrarás aproximadamente C$${String.format(Locale.US, "%.0f", fastFoodSavingEstimate)} al mes.")
        }

        if (projectedMonthEndExpense > totalIncome && totalIncome > 0) {
            insights.add("🚨 Al ritmo de gasto actual, tu proyección de cierre de mes (C$${String.format(Locale.US, "%.2f", projectedMonthEndExpense)}) superará tus ingresos. Te sugerimos frenar compras prescindibles.")
        } else {
            insights.add("📈 Proyección positiva: Cerrarás el mes con un estimado de gasto de C$${String.format(Locale.US, "%.2f", projectedMonthEndExpense)}.")
        }

        return FinancialSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            projectedMonthEndExpense = projectedMonthEndExpense,
            savingsRatePercentage = savingsRate,
            budgetAlerts = alerts,
            automatedInsights = insights
        )
    }

    fun calculateCategoryBreakdown(transactions: List<TransactionEntity>): List<CategoryExpenseBreakdown> {
        val expenseTx = transactions.filter { it.type == "EXPENSE" }
        val total = expenseTx.sumOf { it.amount }
        if (total <= 0.0) return emptyList()

        val grouped = expenseTx.groupBy { it.category }
        val colorMap = mapOf(
            "Alimentación" to "#EF4444",
            "Transporte" to "#F97316",
            "Salud" to "#10B981",
            "Educación" to "#3B82F6",
            "Entretenimiento" to "#8B5CF6",
            "Servicios Básicos" to "#06B6D4",
            "Vivienda" to "#6366F1",
            "Ropa y Calzado" to "#EC4899",
            "Otros Gastos" to "#6B7280"
        )

        return grouped.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            CategoryExpenseBreakdown(
                categoryName = cat,
                totalAmount = sum,
                percentage = (sum / total) * 100,
                colorHex = colorMap[cat] ?: "#0288D1"
            )
        }.sortedByDescending { it.totalAmount }
    }

    // AI Assistant prompt generator
    suspend fun queryAiAssistant(
        userId: Long,
        question: String,
        history: List<Pair<String, String>> = emptyList()
    ): String {
        val user = userDao.getUserById(userId)
        val txs = transactionDao.getTransactionsList(userId)
        val budgets = budgetDao.getBudgetsByUserAndMonth(userId, "2026-09")
        val summary = calculateSummary(txs, emptyList())

        val contextInfo = buildString {
            append("Usuario: ${user?.firstName ?: "Cliente"} ${user?.lastName ?: ""}\n")
            append("Ingresos Registrados: C$ ${String.format(Locale.US, "%.2f", summary.totalIncome)}\n")
            append("Gastos Registrados: C$ ${String.format(Locale.US, "%.2f", summary.totalExpense)}\n")
            append("Balance Actual: C$ ${String.format(Locale.US, "%.2f", summary.balance)}\n")
            append("Proyección de gastos a fin de mes: C$ ${String.format(Locale.US, "%.2f", summary.projectedMonthEndExpense)}\n")
            append("Transacciones Recientes:\n")
            txs.take(8).forEach {
                append("- ${it.type}: ${it.title} | ${it.category} | C$ ${it.amount}\n")
            }
        }

        return GeminiApiClient.askFinancialAssistant(contextInfo, question, history)
    }

    // Formatted report for export
    fun generateExportReport(transactions: List<TransactionEntity>, userName: String): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return buildString {
            append("=========================================\n")
            append("REPORTE FINANCIERO - FINANZAS INTELIGENTES\n")
            append("Usuario: $userName\n")
            append("Fecha de Exportación: ${sdf.format(Date())}\n")
            append("=========================================\n\n")

            val incomes = transactions.filter { it.type == "INCOME" }
            val expenses = transactions.filter { it.type == "EXPENSE" }
            val totalIn = incomes.sumOf { it.amount }
            val totalEx = expenses.sumOf { it.amount }

            append("RESUMEN GENERAL:\n")
            append("Total Ingresos: C$ ${String.format(Locale.US, "%.2f", totalIn)}\n")
            append("Total Gastos:   C$ ${String.format(Locale.US, "%.2f", totalEx)}\n")
            append("Balance Neto:   C$ ${String.format(Locale.US, "%.2f", totalIn - totalEx)}\n\n")

            append("DETALLE DE MOVIMIENTOS (CSV / FORMATEADO):\n")
            append("Fecha | Tipo | Categoría | Concepto | Monto (C$)\n")
            append("--------------------------------------------------\n")
            transactions.forEach { tx ->
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(tx.dateMillis))
                append("$dateStr | ${tx.type} | ${tx.category} | ${tx.title} | ${String.format(Locale.US, "%.2f", tx.amount)}\n")
            }
            append("\n=========================================\n")
            append("Fin del reporte generado con Finanzas Inteligentes.\n")
        }
    }
}
