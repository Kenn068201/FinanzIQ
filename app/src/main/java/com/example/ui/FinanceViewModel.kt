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
import com.example.data.model.CategoryDataHierarchy
import com.example.data.repository.CategoryExpenseBreakdown
import com.example.data.repository.FinanceRepository
import com.example.data.repository.FinancialSummary
import com.example.util.AppLanguage
import com.example.util.CountryPhoneConfig
import com.example.util.CountryPhoneData
import com.example.util.CurrencyOption
import com.example.util.Localization
import com.example.util.Validators
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class NestedCategorySpending(
    val categoryName: String,
    val iconEmoji: String,
    val totalSpent: Double,
    val subcategoryBreakdown: Map<String, Double>
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    val database: AppDatabase

    init {
        database = AppDatabase.getDatabase(application, viewModelScope)
        repository = FinanceRepository(database)
        observeCategories()
    }

    // --- UI Theme & Localization ---
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    private val _currentLanguage = MutableStateFlow(AppLanguage.SPANISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    private val _selectedCurrency = MutableStateFlow(CountryPhoneData.americanCurrencies.first())
    val selectedCurrency: StateFlow<CurrencyOption> = _selectedCurrency.asStateFlow()

    fun setCurrency(currency: CurrencyOption) {
        _selectedCurrency.value = currency
    }

    // --- Login Rate Limiting & Lockout ---
    private var lockoutJob: Job? = null
    private val _failedLoginAttempts = MutableStateFlow(0)
    val failedLoginAttempts: StateFlow<Int> = _failedLoginAttempts.asStateFlow()

    private val _lockoutRemainingSeconds = MutableStateFlow(0)
    val lockoutRemainingSeconds: StateFlow<Int> = _lockoutRemainingSeconds.asStateFlow()

    // --- Global "Deseos" Budget Limit (Single monthly budget for variable spending) ---
    private val _deseosBudgetLimit = MutableStateFlow(8000.0)
    val deseosBudgetLimit: StateFlow<Double> = _deseosBudgetLimit.asStateFlow()

    fun setDeseosBudgetLimit(limit: Double) {
        _deseosBudgetLimit.value = limit
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
    fun login(identifier: String, password: String, onResult: ((Boolean) -> Unit)? = null): Boolean {
        // 1. Check if user is currently locked out
        if (_lockoutRemainingSeconds.value > 0) {
            val minutes = _lockoutRemainingSeconds.value / 60
            val seconds = _lockoutRemainingSeconds.value % 60
            val timeFormatted = String.format("%02d:%02d", minutes, seconds)
            _authError.value = if (_currentLanguage.value == AppLanguage.SPANISH) {
                "Has ingresado credenciales incorrectas 3 veces. Debes esperar 5 minutos para volver a intentar (Tiempo restante: $timeFormatted)."
            } else {
                "You have entered incorrect credentials 3 times. Please wait 5 minutes before trying again (Remaining time: $timeFormatted)."
            }
            onResult?.invoke(false)
            return false
        }

        _authError.value = null
        val id = identifier.trim()
        val pwd = password.trim()

        if (id.isBlank() || pwd.isBlank()) {
            _authError.value = if (_currentLanguage.value == AppLanguage.SPANISH) {
                "Por favor, complete todos los campos."
            } else {
                "Please fill in all fields."
            }
            onResult?.invoke(false)
            return false
        }

        // Validate email format if identifier contains @
        if (id.contains("@") && !Validators.isValidEmail(id)) {
            _authError.value = if (_currentLanguage.value == AppLanguage.SPANISH) {
                "El correo electrónico debe tener el formato 'usuario@dominio.com'."
            } else {
                "Email must follow the format 'user@domain.com'."
            }
            onResult?.invoke(false)
            return false
        }

        // Validate password rules
        val (isPwdValid, pwdErr) = Validators.isValidPassword(pwd)
        if (!isPwdValid) {
            _authError.value = pwdErr
            onResult?.invoke(false)
            return false
        }

        var loggedIn = false
        viewModelScope.launch {
            val user = repository.getUserByEmail(id)
            if (user != null && user.passwordHash == pwd) {
                _currentUser.value = user
                _failedLoginAttempts.value = 0
                _lockoutRemainingSeconds.value = 0
                lockoutJob?.cancel()
                CountryPhoneData.americanCurrencies.find { it.code in user.preferredCurrency }?.let {
                    _selectedCurrency.value = it
                }
                observeUserData(user.id)
                loggedIn = true
                onResult?.invoke(true)
            } else {
                _failedLoginAttempts.value += 1
                if (_failedLoginAttempts.value >= 3) {
                    startLockoutTimer()
                } else {
                    _authError.value = if (_currentLanguage.value == AppLanguage.SPANISH) {
                        "Credenciales incorrectas. Verifique su correo y contraseña. (Intento ${_failedLoginAttempts.value} de 3)"
                    } else {
                        "Incorrect credentials. Please verify your email and password. (Attempt ${_failedLoginAttempts.value} of 3)"
                    }
                }
                onResult?.invoke(false)
            }
        }
        return loggedIn
    }

    private fun startLockoutTimer() {
        _failedLoginAttempts.value = 0
        _lockoutRemainingSeconds.value = 300 // 5 minutes
        val msg = if (_currentLanguage.value == AppLanguage.SPANISH) {
            "Has ingresado credenciales incorrectas 3 veces seguidas. Por seguridad, debes esperar 5 minutos para volver a intentar (Tiempo restante: 05:00)."
        } else {
            "You have entered incorrect credentials 3 times. For security, please wait 5 minutes before trying again (Remaining time: 05:00)."
        }
        _authError.value = msg
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            while (_lockoutRemainingSeconds.value > 0) {
                delay(1000L)
                _lockoutRemainingSeconds.value -= 1
                if (_lockoutRemainingSeconds.value > 0) {
                    val m = _lockoutRemainingSeconds.value / 60
                    val s = _lockoutRemainingSeconds.value % 60
                    val timeFormatted = String.format("%02d:%02d", m, s)
                    _authError.value = if (_currentLanguage.value == AppLanguage.SPANISH) {
                        "Has ingresado credenciales incorrectas 3 veces seguidas. Por seguridad, debes esperar 5 minutos para volver a intentar (Tiempo restante: $timeFormatted)."
                    } else {
                        "You have entered incorrect credentials 3 times. For security, please wait 5 minutes before trying again (Remaining time: $timeFormatted)."
                    }
                }
            }
            _authError.value = null
        }
    }

    fun register(
        firstNames: String,
        lastNames: String,
        ageStr: String,
        phone: String,
        countryConfig: CountryPhoneConfig = CountryPhoneData.americanCountries.first(),
        email: String,
        password: String,
        confirmPassword: String,
        preferredCurrency: CurrencyOption = CountryPhoneData.americanCurrencies.first(),
        role: String = "USER",
        onSuccess: () -> Unit
    ) {
        _authError.value = null

        // 1. First names validation with 70-character max limit
        if (firstNames.length > 70) {
            _authError.value = if (_currentLanguage.value == AppLanguage.SPANISH) {
                "Has alcanzado el límite máximo de 70 caracteres para los nombres."
            } else {
                "You have reached the maximum limit of 70 characters for first names."
            }
            return
        }
        val (isFirstValid, firstErr) = Validators.validateName(firstNames, isLastName = false)
        if (!isFirstValid) {
            _authError.value = firstErr
            return
        }

        // 2. Last names validation with 70-character max limit
        if (lastNames.length > 70) {
            _authError.value = if (_currentLanguage.value == AppLanguage.SPANISH) {
                "Has alcanzado el límite máximo de 70 caracteres para los apellidos."
            } else {
                "You have reached the maximum limit of 70 characters for last names."
            }
            return
        }
        val (isLastValid, lastErr) = Validators.validateName(lastNames, isLastName = true)
        if (!isLastValid) {
            _authError.value = lastErr
            return
        }

        // 3. Age validation (> 18)
        val (isAgeValid, ageErr) = Validators.isValidAge(ageStr)
        if (!isAgeValid) {
            _authError.value = ageErr
            return
        }

        // 4. Phone validation according to selected country specification
        val (isPhoneValid, phoneErr) = CountryPhoneData.validatePhone(countryConfig, phone)
        if (!isPhoneValid) {
            _authError.value = phoneErr
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
            // Check email uniqueness in DB
            val existingEmail = repository.getUserByEmail(email)
            if (existingEmail != null) {
                _authError.value = "El correo electrónico ingresado ya está registrado en la base de datos."
                return@launch
            }

            // Check phone uniqueness in DB (without revealing other user's name)
            val cleanDigits = phone.filter { it.isDigit() }
            val existingPhone = repository.getUserByPhone(cleanDigits) ?: repository.getUserByPhone("${countryConfig.dialPrefix}$cleanDigits")
            if (existingPhone != null) {
                _authError.value = "El número de teléfono ya ha sido registrado por otro usuario."
                return@launch
            }

            val newUser = UserEntity(
                firstName = firstNames.trim(),
                lastName = lastNames.trim(),
                age = ageStr.toInt(),
                phone = cleanDigits,
                phonePrefix = countryConfig.dialPrefix,
                email = email.trim().lowercase(),
                passwordHash = password.trim(),
                role = role,
                preferredCurrency = "${preferredCurrency.symbol} ${preferredCurrency.code}"
            )

            val newId = repository.registerUser(newUser)
            val created = repository.getUserById(newId)
            _currentUser.value = created
            _selectedCurrency.value = preferredCurrency
            created?.let { observeUserData(it.id) }
            onSuccess()
        }
    }

    // --- Incomes and Expenses Advanced Movement ---
    fun addMovement(
        title: String,
        amount: Double,
        type: String, // "INCOME" or "EXPENSE"
        incomeSubType: String = "ACTIVE", // "ACTIVE" or "PASSIVE"
        frequency: String = "",
        payoutDate: String = "",
        destination: String = "",
        categoryGroup: String,
        category: String,
        subCategory: String = "",
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ) {
        val userId = _currentUser.value?.id ?: 1L
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    userId = userId,
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    incomeSubType = incomeSubType,
                    frequency = frequency,
                    payoutDate = payoutDate,
                    destination = destination,
                    categoryGroup = categoryGroup,
                    category = category,
                    subCategory = subCategory,
                    dateMillis = dateMillis,
                    note = note.trim()
                )
            )
        }
    }

    // --- Fixed & Variable Budget Calculations ---
    fun getAutoCalculatedFixedExpensesPreviousMonth(): Double {
        val txs = _allTransactions.value
        val now = Calendar.getInstance()
        now.add(Calendar.MONTH, -1)
        val prevMonth = now.get(Calendar.MONTH)
        val prevYear = now.get(Calendar.YEAR)

        val fixedCatNames = CategoryDataHierarchy.fixedExpenseGroup.categories.map { it.nameEs.lowercase() }
        val calculated = txs.filter { tx ->
            if (tx.type != "EXPENSE") return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val isPrevMonth = txCal.get(Calendar.MONTH) == prevMonth && txCal.get(Calendar.YEAR) == prevYear
            val isFixed = tx.categoryGroup == "GASTOS_FIJOS" || fixedCatNames.any { tx.category.lowercase().contains(it) }
            isPrevMonth && isFixed
        }.sumOf { it.amount }

        return if (calculated <= 0.0) 6500.0 else calculated
    }

    fun getVariableExpensesSpentThisMonth(): Double {
        val txs = _allTransactions.value
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val variableCatNames = CategoryDataHierarchy.variableExpenseGroup.categories.map { it.nameEs.lowercase() }
        return txs.filter { tx ->
            if (tx.type != "EXPENSE") return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val isThisMonth = txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
            val isVariable = tx.categoryGroup == "GASTOS_VARIABLES" || variableCatNames.any { tx.category.lowercase().contains(it) }
            isThisMonth && isVariable
        }.sumOf { it.amount }
    }

    fun getRemainingDeseosFund(): Double {
        val spent = getVariableExpensesSpentThisMonth()
        return (_deseosBudgetLimit.value - spent).coerceAtLeast(0.0)
    }

    fun getNestedVariableSpending(): List<NestedCategorySpending> {
        val txs = _allTransactions.value
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val thisMonthExpenses = txs.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            tx.type == "EXPENSE" && txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        return CategoryDataHierarchy.variableExpenseGroup.categories.map { cat ->
            val matchingTxs = thisMonthExpenses.filter { tx ->
                tx.category.equals(cat.nameEs, ignoreCase = true) || tx.category.equals(cat.nameEn, ignoreCase = true)
            }
            val total = matchingTxs.sumOf { it.amount }
            val subMap = mutableMapOf<String, Double>()
            cat.subcategories.forEach { sub ->
                val subTotal = matchingTxs.filter { tx ->
                    tx.subCategory.equals(sub.nameEs, ignoreCase = true) || tx.subCategory.equals(sub.nameEn, ignoreCase = true)
                }.sumOf { it.amount }
                if (subTotal > 0.0) {
                    subMap[sub.nameEs] = subTotal
                }
            }
            NestedCategorySpending(
                categoryName = cat.nameEs,
                iconEmoji = cat.iconEmoji,
                totalSpent = total,
                subcategoryBreakdown = subMap
            )
        }.filter { it.totalSpent > 0.0 }
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
        val userId = _currentUser.value?.id ?: 1L
        viewModelScope.launch {
            val actualCategory = if (category.isBlank() || category == "Auto") {
                repository.autoClassifyCategory(title, type)
            } else category

            repository.addTransaction(
                TransactionEntity(
                    userId = userId,
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
