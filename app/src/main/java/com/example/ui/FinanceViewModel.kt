package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BillReminderEntity
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.FinancialAccountEntity
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    private val _selectedPeriodFilter = MutableStateFlow("Todos") // "Todos", "Semana", "Mes", "Año"
    val selectedPeriodFilter: StateFlow<String> = _selectedPeriodFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("TODOS") // "TODOS", "INCOME", "EXPENSE"
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    /**
     * Flujo de estado reactivo para las transacciones filtradas.
     * Se recalcula y emite en tiempo real ante cualquier cambio en la lista de transacciones
     * o en los criterios de filtro (búsqueda, categoría, período y tipo de movimiento).
     */
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        _allTransactions,
        _searchQuery,
        _selectedCategoryFilter,
        _selectedPeriodFilter,
        _selectedTypeFilter
    ) { txs, query, cat, period, type ->
        filterTransactionsList(txs, query, cat, period, type)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // --- Budgets ---
    private val _budgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val budgets: StateFlow<List<BudgetEntity>> = _budgets.asStateFlow()

    // --- Savings Goals ---
    private val _savingsGoals = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = _savingsGoals.asStateFlow()

    // --- Bill Reminders ---
    private val _bills = MutableStateFlow<List<BillReminderEntity>>(emptyList())
    val bills: StateFlow<List<BillReminderEntity>> = _bills.asStateFlow()

    // --- Herramientas Financieras: Cuentas y Billeteras ---
    private val _financialAccounts = MutableStateFlow<List<FinancialAccountEntity>>(emptyList())
    val financialAccounts: StateFlow<List<FinancialAccountEntity>> = _financialAccounts.asStateFlow()

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

    private val _simMonths = MutableStateFlow(24) // Default 2 years (24 months)
    val simMonths: StateFlow<Int> = _simMonths.asStateFlow()

    private val _simInterestRate = MutableStateFlow(5.0) // 5% annual
    val simInterestRate: StateFlow<Double> = _simInterestRate.asStateFlow()

    // Daily simulator state
    private val _simDailyAmount = MutableStateFlow(50.0)
    val simDailyAmount: StateFlow<Double> = _simDailyAmount.asStateFlow()

    private val _simDailyStartDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    )
    val simDailyStartDate: StateFlow<String> = _simDailyStartDate.asStateFlow()

    private val _simDailyEndDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() + 30L * 86400000L))
    )
    val simDailyEndDate: StateFlow<String> = _simDailyEndDate.asStateFlow()

    fun updateSimulator(monthly: Double, months: Int, rate: Double = 5.0) {
        _simMonthlyAmount.value = monthly
        _simMonths.value = months
        _simInterestRate.value = rate
    }

    fun updateDailySimulator(dailyAmount: Double, startDate: String, endDate: String) {
        _simDailyAmount.value = dailyAmount
        _simDailyStartDate.value = startDate
        _simDailyEndDate.value = endDate
    }

    /**
     * Calcula el resultado exacto de la simulación mensual:
     * Retorna el monto principal exacto (Monto mensual * Meses) y el total proyectado.
     */
    fun calculateSimulationTotal(): Pair<Double, Double> {
        val monthly = _simMonthlyAmount.value
        val months = _simMonths.value
        val principal = monthly * months
        return principal to principal
    }

    /**
     * Calcula el ahorro diario exacto entre dos fechas validadas.
     * Retorna: Triple(días, totalAhorrado, mensajeDeErrorSiAplica)
     */
    fun calculateDailySimulation(): Triple<Int, Double, String?> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val todayStr = sdf.format(Date())
        val todayDate = try { sdf.parse(todayStr) } catch (e: Exception) { Date() }

        val amount = _simDailyAmount.value
        if (amount <= 0) {
            val err = if (_currentLanguage.value == AppLanguage.SPANISH)
                "El monto diario debe ser mayor a 0."
            else
                "Daily savings amount must be greater than 0."
            return Triple(0, 0.0, err)
        }

        val startDate = try {
            sdf.parse(_simDailyStartDate.value)
        } catch (e: Exception) {
            null
        }

        val endDate = try {
            sdf.parse(_simDailyEndDate.value)
        } catch (e: Exception) {
            null
        }

        if (startDate == null) {
            val err = if (_currentLanguage.value == AppLanguage.SPANISH)
                "Formato de fecha de inicio inválido (Use AAAA-MM-DD)."
            else
                "Invalid start date format (Use YYYY-MM-DD)."
            return Triple(0, 0.0, err)
        }

        if (endDate == null) {
            val err = if (_currentLanguage.value == AppLanguage.SPANISH)
                "Formato de fecha de fin inválido (Use AAAA-MM-DD)."
            else
                "Invalid end date format (Use YYYY-MM-DD)."
            return Triple(0, 0.0, err)
        }

        if (startDate.before(todayDate)) {
            val err = if (_currentLanguage.value == AppLanguage.SPANISH)
                "La fecha de inicio debe ser igual o mayor a la fecha del sistema ($todayStr)."
            else
                "Start date must be today or a future date ($todayStr)."
            return Triple(0, 0.0, err)
        }

        if (!endDate.after(startDate)) {
            val err = if (_currentLanguage.value == AppLanguage.SPANISH)
                "La fecha de fin debe ser mayor a la fecha de inicio ingresada."
            else
                "End date must be strictly greater than start date."
            return Triple(0, 0.0, err)
        }

        val diffMillis = endDate.time - startDate.time
        val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        val total = days * amount
        return Triple(days, total, null)
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
        viewModelScope.launch {
            repository.getFinancialAccounts(userId).collectLatest { accList ->
                _financialAccounts.value = accList
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
        note: String = "",
        accountId: Long? = null
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

            // Si se especificó una cuenta o billetera activa del usuario, actualizar su saldo individual
            if (accountId != null && accountId > 0L) {
                val account = repository.getAccountById(accountId)
                if (account != null && account.userId == userId) {
                    val newBalance = if (type == "EXPENSE") {
                        (account.balance - amount).coerceAtLeast(0.0)
                    } else {
                        account.balance + amount
                    }
                    repository.updateAccountBalance(accountId, newBalance)
                }
            }
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

    /**
     * Cierra la sesión activa del usuario actual.
     * Limpia los datos de sesión y reinicia los filtros temporales de búsqueda/listas,
     * pero preserva intactas las preferencias globales de Idioma y Modo Oscuro/Claro.
     */
    fun logout() {
        _currentUser.value = null
        _allTransactions.value = emptyList()
        _budgets.value = emptyList()
        _savingsGoals.value = emptyList()
        _bills.value = emptyList()
        _financialAccounts.value = emptyList()
        _authError.value = null

        // Reiniciar filtros temporales a sus valores por defecto en tiempo real
        _searchQuery.value = ""
        _selectedCategoryFilter.value = "Todas"
        _selectedPeriodFilter.value = "Todos"
        _selectedTypeFilter.value = "TODOS"
        // Nota: _currentLanguage y _isDarkMode se mantienen intactos tal como lo requiere el sistema
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

    /**
     * Retorna la lista filtrada de forma síncrona evaluando la lógica unificada de filtrado.
     */
    fun getFilteredTransactions(): List<TransactionEntity> {
        return filterTransactionsList(
            _allTransactions.value,
            _searchQuery.value,
            _selectedCategoryFilter.value,
            _selectedPeriodFilter.value,
            _selectedTypeFilter.value
        )
    }

    /**
     * Lógica centralizada y dinámica de filtrado en tiempo real.
     * Permite coincidencia flexible de texto, categorías (incluyendo subcategorías y grupos),
     * tipos de movimiento y períodos cronológicos.
     */
    private fun filterTransactionsList(
        transactions: List<TransactionEntity>,
        query: String,
        cat: String,
        period: String,
        type: String
    ): List<TransactionEntity> {
        val q = query.trim().lowercase()
        val now = Calendar.getInstance()
        val currentWeek = now.get(Calendar.WEEK_OF_YEAR)
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        return transactions.filter { tx ->
            // Filtro por texto de búsqueda (búsqueda en concepto, categoría, grupo, subcategoría, destino y notas)
            val matchesQuery = q.isEmpty() ||
                    tx.title.lowercase().contains(q) ||
                    tx.category.lowercase().contains(q) ||
                    tx.categoryGroup.lowercase().contains(q) ||
                    tx.subCategory.lowercase().contains(q) ||
                    tx.destination.lowercase().contains(q) ||
                    tx.note.lowercase().contains(q)

            // Filtro por categoría seleccionada
            val matchesCat = (cat == "Todas" || cat == "All" || cat.isBlank()) ||
                    tx.category.equals(cat, ignoreCase = true) ||
                    tx.category.contains(cat, ignoreCase = true) ||
                    cat.contains(tx.category, ignoreCase = true) ||
                    tx.categoryGroup.equals(cat, ignoreCase = true) ||
                    tx.subCategory.equals(cat, ignoreCase = true)

            // Filtro por tipo de movimiento (TODOS, INCOME, EXPENSE)
            val matchesType = (type == "TODOS" || type == "ALL") || tx.type.equals(type, ignoreCase = true)

            // Filtro por período de tiempo (Semana, Mes, Año, Todos)
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val matchesPeriod = when (period) {
                "Semana", "Week" -> txCal.get(Calendar.WEEK_OF_YEAR) == currentWeek && txCal.get(Calendar.YEAR) == currentYear
                "Mes", "Month" -> txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
                "Año", "Year" -> txCal.get(Calendar.YEAR) == currentYear
                else -> true // "Todos", "All", etc.
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

        val isEnglish = _currentLanguage.value == AppLanguage.ENGLISH
        val sym = _selectedCurrency.value.symbol

        viewModelScope.launch {
            val historyPairs = _chatMessages.value.map { it.sender to it.text }
            val answer = repository.queryAiAssistant(user.id, q, historyPairs, isEnglish, sym)
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

    // --- Herramientas Financieras (Cuentas y Billeteras) ---
    /**
     * Alterna el estado activo/deshabilitado de una cuenta o billetera financiera.
     * Al deshabilitar, conserva el historial de transacciones intacto pero no permite nuevos cobros activos.
     */
    fun toggleAccountActive(account: FinancialAccountEntity) {
        viewModelScope.launch {
            repository.setFinancialAccountStatus(account.id, !account.isActive)
        }
    }

    /**
     * Realiza una transferencia entre cuentas bancarias y/o billeteras digitales del usuario.
     * Valida:
     * 1. Que origen y destino sean distintos.
     * 2. Que ambas pertenezcan al usuario y estén activas.
     * 3. Que el monto sea mayor a 0.
     * 4. Límites: Billeteras máx C$ 15,000, Cuentas bancarias máx C$ 200,000.
     * 5. Comisión: C$ 80 si los bancos son diferentes (se deduce del origen: Monto + C$ 80).
     * 6. Fondos suficientes en la cuenta de origen.
     */
    fun transferFunds(
        originAccount: FinancialAccountEntity,
        destinationAccount: FinancialAccountEntity,
        amount: Double,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, if (_currentLanguage.value == AppLanguage.SPANISH) "Sesión inválida." else "Invalid session.")
            return
        }

        // 1. Validar origen != destino
        if (originAccount.id == destinationAccount.id) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "La cuenta o billetera de origen debe ser distinta a la cuenta o billetera de destino."
                else
                    "The source account/wallet must be different from the destination account/wallet."
            )
            return
        }

        // 2. Validar que ambas pertenezcan al mismo usuario
        if (originAccount.userId != user.id || destinationAccount.userId != user.id) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "La transacción no es válida ya que las cuentas no están vinculadas al mismo usuario."
                else
                    "The transaction is not valid because the accounts are not linked to the same user."
            )
            return
        }

        // Validar que ambas estén activas
        if (!originAccount.isActive || !destinationAccount.isActive) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "Una de las cuentas o billeteras seleccionadas se encuentra deshabilitada."
                else
                    "One of the selected accounts or wallets is currently disabled."
            )
            return
        }

        // 3. Validar monto > 0
        if (amount <= 0.0) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "El monto asignado debe ser mayor a 0."
                else
                    "The assigned amount must be greater than 0."
            )
            return
        }

        // 4. Validar límites de monto: Billeteras max 15,000, Cuentas bancarias max 200,000
        val isWalletInvolved = originAccount.toolType == "BILLETERA" || destinationAccount.toolType == "BILLETERA"
        val maxLimit = if (isWalletInvolved) 15000.0 else 200000.0
        if (amount > maxLimit) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH) {
                    if (isWalletInvolved)
                        "El monto no debe ser mayor a C$ 15,000.00 en transacciones que involucran billeteras digitales."
                    else
                        "El monto máximo permitido para transacciones entre cuentas bancarias es de C$ 200,000.00."
                } else {
                    if (isWalletInvolved)
                        "The amount must not exceed C$ 15,000.00 for transactions involving digital wallets."
                    else
                        "The maximum amount allowed for transactions between bank accounts is C$ 200,000.00."
                }
            )
            return
        }

        // 5. Validar comisión por bancos distintos (C$ 80 adicionales deducidos de la cuenta de origen)
        val isDifferentBank = !originAccount.bankName.equals(destinationAccount.bankName, ignoreCase = true)
        val commission = if (isDifferentBank) 80.0 else 0.0
        val totalToDeduct = amount + commission

        // 6. Validar fondos suficientes en la cuenta de origen
        if (originAccount.balance < totalToDeduct) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "No se puede realizar la transacción por falta de fondos disponibles en la cuenta/billetera de origen (Saldo disponible: C$ ${String.format(Locale.US, "%,.2f", originAccount.balance)}, Requerido: C$ ${String.format(Locale.US, "%,.2f", totalToDeduct)})."
                else
                    "Cannot complete transaction due to insufficient funds in the source account/wallet (Available: C$ ${String.format(Locale.US, "%,.2f", originAccount.balance)}, Required: C$ ${String.format(Locale.US, "%,.2f", totalToDeduct)})."
            )
            return
        }

        viewModelScope.launch {
            repository.transferBetweenAccounts(
                originAccount = originAccount,
                destinationAccount = destinationAccount,
                amount = amount,
                commission = commission
            )
            onResult(true, null)
        }
    }

    /**
     * Realiza un abono a una meta de ahorro descontando los fondos de la cuenta o billetera seleccionada.
     * Valida la titularidad del usuario, que la cuenta esté activa, que el monto sea > 0 y fondos suficientes.
     */
    fun depositFundsToSavingsGoal(
        goal: SavingsGoalEntity,
        sourceAccount: FinancialAccountEntity?,
        amount: Double,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, if (_currentLanguage.value == AppLanguage.SPANISH) "Sesión no válida." else "Invalid session.")
            return
        }

        if (sourceAccount == null) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "Debe seleccionar la cuenta bancaria o billetera de origen de los fondos."
                else
                    "You must select the source bank account or wallet for the funds."
            )
            return
        }

        if (sourceAccount.userId != user.id) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "La cuenta de origen seleccionada no está vinculada al usuario actual."
                else
                    "The selected source account is not linked to the current user."
            )
            return
        }

        if (!sourceAccount.isActive) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "La cuenta o billetera seleccionada se encuentra deshabilitada. Debe reactivarla para usarla."
                else
                    "The selected account or wallet is disabled. You must reactivate it to use it."
            )
            return
        }

        if (amount <= 0.0) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "El monto a abonar debe ser mayor a 0."
                else
                    "The deposit amount must be greater than 0."
            )
            return
        }

        if (sourceAccount.balance < amount) {
            onResult(
                false,
                if (_currentLanguage.value == AppLanguage.SPANISH)
                    "Fondos insuficientes en la cuenta/billetera seleccionada (Disponible: C$ ${String.format(Locale.US, "%,.2f", sourceAccount.balance)}, Requerido: C$ ${String.format(Locale.US, "%,.2f", amount)})."
                else
                    "Insufficient funds in the selected account/wallet (Available: C$ ${String.format(Locale.US, "%,.2f", sourceAccount.balance)}, Required: C$ ${String.format(Locale.US, "%,.2f", amount)})."
            )
            return
        }

        viewModelScope.launch {
            // Deducir de la cuenta/billetera origen
            val updatedAccount = sourceAccount.copy(balance = sourceAccount.balance - amount)
            repository.updateFinancialAccount(updatedAccount)

            // Abonar a la meta de ahorro
            val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
            repository.updateSavingsGoal(updatedGoal)

            // Registrar transacción de gasto / aporte a meta
            repository.addTransaction(
                TransactionEntity(
                    userId = user.id,
                    title = "Aporte a Meta: ${goal.title}",
                    amount = amount,
                    type = "EXPENSE",
                    category = "Ahorro e Inversión",
                    dateMillis = System.currentTimeMillis(),
                    note = "Abono desde ${sourceAccount.bankName} (${sourceAccount.accountType} - ${sourceAccount.accountNumber})"
                )
            )

            onResult(true, null)
        }
    }

    /**
     * Registra una nueva herramienta financiera (Cuenta Bancaria o Billetera Digital).
     * Ejecuta validaciones estrictas de montos mínimos y coincidencia de número telefónico y cuenta vinculada.
     */
    fun addFinancialTool(
        toolType: String, // "CUENTA" o "BILLETERA"
        bankName: String,
        accountType: String, // "DEBITO", "CREDITO", "BILLETERA"
        debitSubType: String, // "AHORRO", "CORRIENTE", "NOMINA"
        accountNumber: String,
        creditLimit: Double,
        initialBalance: Double,
        associatedPhone: String,
        linkedBank: String,
        linkedAccountType: String,
        linkedDebitSubType: String,
        linkedAccountNumber: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "Sesión no válida. Por favor, vuelva a iniciar sesión.")
            return
        }

        if (toolType == "BILLETERA") {
            val userDigits = user.phone.filter { it.isDigit() }
            val inputDigits = associatedPhone.filter { it.isDigit() }

            // 1. Validación de número de teléfono del usuario
            if (inputDigits.isBlank()) {
                onResult(false, "Debe ingresar el número telefónico asociado a la billetera digital.")
                return
            }
            if (inputDigits != userDigits) {
                onResult(
                    false,
                    "El número telefónico ingresado ($associatedPhone) no coincide con el número registrado en su perfil (${user.phone}). La billetera debe pertenecer al titular."
                )
                return
            }

            // 2. Validación de cuenta bancaria vinculada
            if (linkedBank.isBlank() || linkedAccountNumber.isBlank()) {
                onResult(
                    false,
                    "Debe seleccionar o especificar la cuenta bancaria vinculada (Banco, Tipo, Subtipo y Número de Cuenta) para respaldar la billetera."
                )
                return
            }

            val matchingAccount = _financialAccounts.value.find {
                it.toolType == "CUENTA" &&
                it.bankName.equals(linkedBank.trim(), ignoreCase = true) &&
                (it.accountNumber.filter { c -> c.isDigit() } == linkedAccountNumber.filter { c -> c.isDigit() } ||
                 it.accountNumber.contains(linkedAccountNumber.trim()))
            }

            if (matchingAccount == null) {
                onResult(
                    false,
                    "La cuenta bancaria vinculada ($linkedBank - $linkedAccountNumber) no coincide con ninguna cuenta activa registrada en su perfil."
                )
                return
            }

            if (initialBalance < 0.0) {
                onResult(false, "El saldo inicial de la billetera no puede ser negativo.")
                return
            }

            viewModelScope.launch {
                repository.addFinancialAccount(
                    FinancialAccountEntity(
                        userId = user.id,
                        toolType = "BILLETERA",
                        bankName = bankName.ifBlank { matchingAccount.bankName },
                        accountType = "BILLETERA",
                        debitSubType = "",
                        accountNumber = associatedPhone.trim(),
                        creditLimit = 0.0,
                        balance = initialBalance,
                        associatedPhone = associatedPhone.trim(),
                        linkedBank = matchingAccount.bankName,
                        linkedAccountType = matchingAccount.accountType,
                        linkedDebitSubType = matchingAccount.debitSubType,
                        linkedAccountNumber = matchingAccount.accountNumber,
                        isActive = true
                    )
                )
                onResult(true, null)
            }
        } else {
            // Validación de Cuenta Bancaria
            if (bankName.isBlank()) {
                onResult(false, "Debe seleccionar una institución bancaria de Nicaragua.")
                return
            }
            if (accountNumber.trim().length < 6) {
                onResult(false, "El número de cuenta bancaria debe contener al menos 6 dígitos válidos.")
                return
            }

            if (accountType == "DEBITO") {
                val minRequired = when (debitSubType) {
                    "AHORRO" -> 500.0
                    "CORRIENTE" -> 2500.0
                    "NOMINA" -> 0.0
                    else -> 500.0
                }
                if (initialBalance < minRequired) {
                    onResult(
                        false,
                        "El monto mínimo de apertura para una Cuenta de $debitSubType en $bankName es de C$ ${String.format(Locale.US, "%.2f", minRequired)}."
                    )
                    return
                }

                viewModelScope.launch {
                    repository.addFinancialAccount(
                        FinancialAccountEntity(
                            userId = user.id,
                            toolType = "CUENTA",
                            bankName = bankName.trim(),
                            accountType = "DEBITO",
                            debitSubType = debitSubType,
                            accountNumber = accountNumber.trim(),
                            creditLimit = 0.0,
                            balance = initialBalance,
                            isActive = true
                        )
                    )
                    onResult(true, null)
                }
            } else if (accountType == "CREDITO") {
                if (creditLimit < 7000.0) {
                    onResult(
                        false,
                        "El límite de crédito mínimo para una tarjeta de crédito en $bankName es de C$ 7,000.00 (o equivalente $200)."
                    )
                    return
                }
                if (initialBalance < 0.0 || initialBalance > creditLimit) {
                    onResult(false, "El saldo disponible inicial debe estar entre C$ 0.00 y el límite de crédito aprobado (C$ ${String.format(Locale.US, "%.2f", creditLimit)}).")
                    return
                }

                viewModelScope.launch {
                    repository.addFinancialAccount(
                        FinancialAccountEntity(
                            userId = user.id,
                            toolType = "CUENTA",
                            bankName = bankName.trim(),
                            accountType = "CREDITO",
                            debitSubType = "",
                            accountNumber = accountNumber.trim(),
                            creditLimit = creditLimit,
                            balance = initialBalance,
                            isActive = true
                        )
                    )
                    onResult(true, null)
                }
            } else {
                onResult(false, "Seleccione un tipo de cuenta válido (Débito o Crédito).")
            }
        }
    }
}
