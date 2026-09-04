package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BillReminderEntity
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.repository.CategoryExpenseBreakdown
import com.example.data.repository.FinanceRepository
import com.example.data.repository.FinancialSummary
import com.example.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UiAlert(
    val message: String,
    val isError: Boolean = true
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    val database: AppDatabase

    init {
        database = AppDatabase.getDatabase(application, viewModelScope)
        repository = FinanceRepository(database)
        observeCategories()
    }

    // --- Current Auth State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    // --- Transactions & Filters ---
    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<TransactionEntity>> = _allTransactions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("Todas")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedPeriodFilter = MutableStateFlow("Mes") // "Todos", "Semana", "Mes", "Año"
    val selectedPeriodFilter: StateFlow<String> = _selectedPeriodFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("TODOS") // "TODOS", "INCOME", "EXPENSE"
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    // --- Budgets ---
    private val _budgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val budgets: StateFlow<List<BudgetEntity>> = _budgets.asStateFlow()

    // --- Savings Goals ---
    private val _savingsGoals = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = _savingsGoals.asStateFlow()

    // --- Bill Reminders ---
    private val _bills = MutableStateFlow<List<BillReminderEntity>>(emptyList())
    val bills: StateFlow<List<BillReminderEntity>> = _bills.asStateFlow()

    // --- Categories ---
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    // --- Admin state ---
    private val _allUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allUsers: StateFlow<List<UserEntity>> = _allUsers.asStateFlow()

    // --- Financial Summary & Charts ---
    private val _financialSummary = MutableStateFlow(
        FinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, emptyList(), emptyList())
    )
    val financialSummary: StateFlow<FinancialSummary> = _financialSummary.asStateFlow()

    private val _categoryBreakdown = MutableStateFlow<List<CategoryExpenseBreakdown>>(emptyList())
    val categoryBreakdown: StateFlow<List<CategoryExpenseBreakdown>> = _categoryBreakdown.asStateFlow()

    // --- AI Assistant Chat ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome",
                sender = "ai",
                text = "¡Hola! Soy tu Asistente Financiero Inteligente. Puedo analizar tus gastos, ayudarte a ahorrar y evaluar tus presupuestos. ¿En qué puedo ayudarte hoy?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Savings Simulator State ---
    private val _simMonthlyAmount = MutableStateFlow(500.0)
    val simMonthlyAmount: StateFlow<Double> = _simMonthlyAmount.asStateFlow()

    private val _simMonths = MutableStateFlow(12)
    val simMonths: StateFlow<Int> = _simMonths.asStateFlow()

    private val _simInterestRate = MutableStateFlow(5.0) // 5% annual
    val simInterestRate: StateFlow<Double> = _simInterestRate.asStateFlow()

    fun updateSimulator(monthly: Double, months: Int, rate: Double = 5.0) {
        _simMonthlyAmount.value = monthly
        _simMonths.value = months
        _simInterestRate.value = rate
    }

    fun calculateSimulationTotal(): Pair<Double, Double> {
        val monthly = _simMonthlyAmount.value
        val months = _simMonths.value
        val principal = monthly * months
        val monthlyRate = (_simInterestRate.value / 100) / 12
        var accumulated = 0.0
        for (i in 1..months) {
            accumulated = (accumulated + monthly) * (1 + monthlyRate)
        }
        return principal to accumulated
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.getCategories().collectLatest {
                _categories.value = it
            }
        }
        viewModelScope.launch {
            repository.getAllUsers().collectLatest {
                _allUsers.value = it
            }
        }
    }

    private fun observeUserData(userId: Long) {
        viewModelScope.launch {
            repository.getTransactionsByUser(userId).collectLatest { txs ->
                _allTransactions.value = txs
                recalculateAnalytics()
            }
        }
        viewModelScope.launch {
            repository.getBudgetsByUser(userId, "2026-09").collectLatest { bList ->
                _budgets.value = bList
                recalculateAnalytics()
            }
        }
        viewModelScope.launch {
            repository.getSavingsGoals(userId).collectLatest { gList ->
                _savingsGoals.value = gList
            }
        }
        viewModelScope.launch {
            repository.getBillReminders(userId).collectLatest { bList ->
                _bills.value = bList
            }
        }
    }

    private fun recalculateAnalytics() {
        val txs = _allTransactions.value
        val bList = _budgets.value
        _financialSummary.value = repository.calculateSummary(txs, bList)
        _categoryBreakdown.value = repository.calculateCategoryBreakdown(txs)
    }

    // --- Auth Methods ---
    fun login(identifier: String, password: String):Boolean {
        _authError.value = null
        val id = identifier.trim()
        val pwd = password.trim()

        if (id.isBlank() || pwd.isBlank()) {
            _authError.value = "Por favor, complete todos los campos."
            return false
        }

        // Validate email format if identifier contains @
        if (id.contains("@") && !Validators.isValidEmail(id)) {
            _authError.value = "El correo electrónico debe tener el formato 'usuario@dominio.com'."
            return false
        }

        // Validate password rules
        val (isPwdValid, pwdErr) = Validators.isValidPassword(pwd)
        if (!isPwdValid) {
            _authError.value = pwdErr
            return false
        }

        var loggedIn = false
        viewModelScope.launch {
            val user = repository.getUserByEmail(id)
            if (user != null && user.passwordHash == pwd) {
                _currentUser.value = user
                observeUserData(user.id)
                loggedIn = true
            } else {
                _authError.value = "Credenciales incorrectas. Verifique su correo y contraseña."
            }
        }
        return loggedIn
    }

    fun register(
        firstNames: String,
        lastNames: String,
        ageStr: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String = "USER",
        onSuccess: () -> Unit
    ) {
        _authError.value = null

        // 1. First names validation
        if (!Validators.isValidName(firstNames)) {
            _authError.value = "Los nombres deben empezar con mayúscula seguida de minúsculas (ej: Carlos Alberto)."
            return
        }

        // 2. Last names validation
        if (!Validators.isValidName(lastNames)) {
            _authError.value = "Los apellidos deben empezar con mayúscula seguida de minúsculas (ej: Mendoza López)."
            return
        }

        // 3. Age validation (> 18)
        val (isAgeValid, ageErr) = Validators.isValidAge(ageStr)
        if (!isAgeValid) {
            _authError.value = ageErr
            return
        }

        // 4. Phone validation (starts with 5, 7, 8 and 8 digits)
        if (!Validators.isValidPhone(phone)) {
            _authError.value = "El teléfono debe empezar por 5, 7 u 8 y tener exactamente 8 dígitos."
            return
        }

        // 5. Email validation
        if (!Validators.isValidEmail(email)) {
            _authError.value = "El correo electrónico no es válido. Formato requerido: 'usuario@dominio.com'."
            return
        }

        // 6. Password validation
        val (isPwdValid, pwdErr) = Validators.isValidPassword(password)
        if (!isPwdValid) {
            _authError.value = pwdErr
            return
        }

        // 7. Password confirmation match
        if (password != confirmPassword) {
            _authError.value = "La confirmación de contraseña no coincide con la contraseña ingresada."
            return
        }

        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _authError.value = "Ya existe un usuario registrado con este correo electrónico."
                return@launch
            }

            val newUser = UserEntity(
                firstName = firstNames.trim(),
                lastName = lastNames.trim(),
                age = ageStr.toInt(),
                phone = phone.trim(),
                email = email.trim().lowercase(),
                passwordHash = password.trim(),
                role = role
            )

            val newId = repository.registerUser(newUser)
            val created = repository.getUserById(newId)
            _currentUser.value = created
            created?.let { observeUserData(it.id) }
            onSuccess()
        }
    }

    fun requestPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        if (!Validators.isValidEmail(email)) {
            onResult(false, "El correo debe tener el formato 'usuario@dominio.com'.")
            return
        }

        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                onResult(true, "Se ha enviado un enlace de restablecimiento al correo $email.")
            } else {
                onResult(false, "No se encontró ninguna cuenta registrada con este correo.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _allTransactions.value = emptyList()
        _budgets.value = emptyList()
        _savingsGoals.value = emptyList()
        _bills.value = emptyList()
        _authError.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // --- Transactions Management ---
    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        dateMillis: Long,
        note: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val actualCategory = if (category.isBlank() || category == "Auto") {
                repository.autoClassifyCategory(title, type)
            } else category

            repository.addTransaction(
                TransactionEntity(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    category = actualCategory,
                    dateMillis = dateMillis,
                    note = note.trim()
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun autoSuggestCategory(title: String, type: String): String {
        return repository.autoClassifyCategory(title, type)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setPeriodFilter(period: String) {
        _selectedPeriodFilter.value = period
    }

    fun setTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    fun getFilteredTransactions(): List<TransactionEntity> {
        val query = _searchQuery.value.trim().lowercase()
        val cat = _selectedCategoryFilter.value
        val period = _selectedPeriodFilter.value
        val type = _selectedTypeFilter.value

        val now = Calendar.getInstance()
        val currentWeek = now.get(Calendar.WEEK_OF_YEAR)
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        return _allTransactions.value.filter { tx ->
            val matchesQuery = query.isEmpty() ||
                    tx.title.lowercase().contains(query) ||
                    tx.category.lowercase().contains(query) ||
                    tx.note.lowercase().contains(query)

            val matchesCat = (cat == "Todas") || tx.category.equals(cat, ignoreCase = true)
            val matchesType = (type == "TODOS") || tx.type == type

            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val matchesPeriod = when (period) {
                "Semana" -> txCal.get(Calendar.WEEK_OF_YEAR) == currentWeek && txCal.get(Calendar.YEAR) == currentYear
                "Mes" -> txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
                "Año" -> txCal.get(Calendar.YEAR) == currentYear
                else -> true
            }

            matchesQuery && matchesCat && matchesType && matchesPeriod
        }
    }

    // --- Budgets ---
    fun setBudget(category: String, monthlyLimit: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.setBudget(
                BudgetEntity(
                    userId = user.id,
                    category = category,
                    monthlyLimit = monthlyLimit,
                    monthYear = "2026-09"
                )
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // --- Savings Goals ---
    fun addSavingsGoal(title: String, targetAmount: Double, initialSaved: Double, targetDateMillis: Long) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addSavingsGoal(
                SavingsGoalEntity(
                    userId = user.id,
                    title = title.trim(),
                    targetAmount = targetAmount,
                    currentAmount = initialSaved,
                    targetDateMillis = targetDateMillis
                )
            )
        }
    }

    fun addFundsToGoal(goal: SavingsGoalEntity, depositAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + depositAmount)
            repository.updateSavingsGoal(updated)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    // --- Bill Reminders ---
    fun addBillReminder(title: String, amount: Double, dueDateMillis: Long, category: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addBillReminder(
                BillReminderEntity(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    dueDateMillis = dueDateMillis,
                    category = category
                )
            )
        }
    }

    fun toggleBillPaid(bill: BillReminderEntity) {
        viewModelScope.launch {
            repository.updateBillReminder(bill.copy(isPaid = !bill.isPaid))
        }
    }

    fun deleteBillReminder(bill: BillReminderEntity) {
        viewModelScope.launch {
            repository.deleteBillReminder(bill)
        }
    }

    // --- Category Management (Admin & Custom) ---
    fun addCategory(name: String, type: String, iconName: String = "category", colorHex: String = "#0288D1") {
        viewModelScope.launch {
            repository.addCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type,
                    iconName = iconName,
                    colorHex = colorHex,
                    isCustom = true
                )
            )
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    // --- User Management (Admin) ---
    fun updateUserRole(user: UserEntity, newRole: String) {
        viewModelScope.launch {
            repository.updateUser(user.copy(role = newRole))
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    // --- AI Assistant ---
    fun sendAiMessage(promptText: String) {
        val user = _currentUser.value ?: return
        val q = promptText.trim()
        if (q.isBlank()) return

        val userMsg = ChatMessage(id = System.currentTimeMillis().toString(), sender = "user", text = q)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val historyPairs = _chatMessages.value.map { it.sender to it.text }
            val answer = repository.queryAiAssistant(user.id, q, historyPairs)
            val aiMsg = ChatMessage(id = (System.currentTimeMillis() + 1).toString(), sender = "ai", text = answer)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    // --- Export Report ---
    fun generateExportContent(): String {
        val user = _currentUser.value
        val name = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim().ifEmpty { "Usuario" }
        return repository.generateExportReport(_allTransactions.value, name)
    }
}
