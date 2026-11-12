package com.example.ui.transactions

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.model.CategoryDataHierarchy
import com.example.data.model.CategoryGroupType
import com.example.data.model.CategoryItem
import com.example.data.model.PassiveIncomeCategory
import com.example.data.model.SubcategoryItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.data.local.FinancialAccountEntity
import com.example.util.AppLanguage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.components.formatCordobas
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.ui.theme.FinanceSuccessContainer
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategoryFilter.collectAsState()
    val selectedPeriod by viewModel.selectedPeriodFilter.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val financialAccounts by viewModel.financialAccounts.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val filteredList by viewModel.filteredTransactions.collectAsState()

    // Categorías dinámicas disponibles: combina las categorías de la base de datos con las que el usuario ha registrado
    val availableCategoryNames = remember(allTransactions, categories) {
        val fromTxs = allTransactions.map { it.category }.filter { it.isNotBlank() }
        val fromDb = categories.map { it.name }.filter { it.isNotBlank() }
        (fromTxs + fromDb).distinct().sorted()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var dialogInitialType by remember { mutableStateOf("EXPENSE") }
    var showExportDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.t("transactions_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.testTag("transfer_button_transactions")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = Localization.t("transfer_btn", lang),
                            tint = FinancePrimary
                        )
                    }
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = Localization.t("export_report_title", lang),
                            tint = FinancePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogInitialType = "EXPENSE"
                    showAddDialog = true
                },
                containerColor = FinancePrimary,
                contentColor = FinanceOnPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(Icons.Default.Add, contentDescription = Localization.t("new_movement", lang))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(Localization.t("search_transactions_placeholder", lang)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = FinancePrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = Localization.t("clear", lang))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FinancePrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Period Filter Chips (Semana, Mes, Año, Todos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Semana" to Localization.t("filter_week", lang),
                    "Mes" to Localization.t("filter_month", lang),
                    "Año" to Localization.t("filter_year", lang),
                    "Todos" to Localization.t("filter_all", lang)
                ).forEach { (periodKey, label) ->
                    FilterChip(
                        selected = selectedPeriod == periodKey,
                        onClick = { viewModel.setPeriodFilter(periodKey) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier.testTag("filter_period_$periodKey")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Category Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCat == "Todas",
                        onClick = { viewModel.setCategoryFilter("Todas") },
                        label = { Text(Localization.t("all_categories", lang)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        )
                    )
                }
                items(availableCategoryNames) { catName ->
                    FilterChip(
                        selected = selectedCat == catName,
                        onClick = {
                            if (selectedCat == catName) {
                                viewModel.setCategoryFilter("Todas")
                            } else {
                                viewModel.setCategoryFilter(catName)
                            }
                        },
                        label = { Text(catName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Transaction Type Selector (TODOS, INCOME, EXPENSE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "TODOS" to Localization.t("all_types", lang),
                    "INCOME" to Localization.t("incomes_label", lang),
                    "EXPENSE" to Localization.t("expenses_label", lang)
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedType == key,
                        onClick = { viewModel.setTypeFilter(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (key == "INCOME") FinanceSuccessContainer else if (key == "EXPENSE") Color(0xFFFEE2E2) else FinanceSecondaryContainer,
                            selectedLabelColor = if (key == "INCOME") FinanceSuccess else if (key == "EXPENSE") FinanceError else FinancePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transaction List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = Localization.t("no_movements_found", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280)
                            )
                        )
                        Text(
                            text = Localization.t("no_movements_hint", lang),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { tx ->
                        TransactionCardDetail(
                            tx = tx,
                            currencySymbol = currency.symbol,
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }

        // Add Transaction Dialog
        if (showAddDialog) {
            AddTransactionDialog(
                initialType = dialogInitialType,
                categories = categories.map { it.name }.distinct(),
                accounts = financialAccounts,
                currencySymbol = currency.symbol,
                lang = lang,
                onAutoClassify = { title, type -> viewModel.autoSuggestCategory(title, type) },
                onDismiss = { showAddDialog = false },
                onConfirmMovement = { title, amount, type, incomeSubType, frequency, payoutDate, destination, categoryGroup, category, subCategory, date, note, accountId ->
                    viewModel.addMovement(
                        title = title,
                        amount = amount,
                        type = type,
                        incomeSubType = incomeSubType,
                        frequency = frequency,
                        payoutDate = payoutDate,
                        destination = destination,
                        categoryGroup = categoryGroup,
                        category = category,
                        subCategory = subCategory,
                        dateMillis = date,
                        note = note,
                        accountId = accountId
                    )
                    showAddDialog = false
                }
            )
        }

        // Transfer Funds Dialog
        if (showTransferDialog) {
            TransferFundsDialog(
                viewModel = viewModel,
                onDismiss = { showTransferDialog = false },
                onTransferSuccess = { showTransferDialog = false }
            )
        }

        // Export Report Dialog
        if (showExportDialog) {
            val reportContent = viewModel.generateExportContent()
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = {
                    Text(
                        text = Localization.t("export_report_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = Localization.t("export_report_desc", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF4B5563))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FinanceSecondaryContainer.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "• Incluye balance general, total ingresos, total gastos y detalle cronológico.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF111827)),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportContent)
                                putExtra(Intent.EXTRA_SUBJECT, "Reporte Financiero - FinanzIQ")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Exportar Reporte")
                            context.startActivity(shareIntent)
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                        modifier = Modifier.testTag("confirm_export_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.t("share_export_btn", lang))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text(Localization.t("close", lang))
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun TransactionCardDetail(
    tx: TransactionEntity,
    currencySymbol: String = "C$",
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = tx.type == "INCOME"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(tx.dateMillis))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isIncome) FinanceSuccessContainer else Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isIncome) FinanceSuccess else FinanceError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tx.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                        val subtitle = buildString {
                            append(tx.category)
                            if (tx.subCategory.isNotBlank()) {
                                append(" • ${tx.subCategory}")
                            }
                            append(" • $dateStr")
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF6B7280)
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (isIncome) "+" else "-"} ${formatCordobas(tx.amount)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) FinanceSuccess else FinanceError
                        )
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Badges for passive income, destination, and payment date
            if (tx.incomeSubType == "PASSIVE" || tx.destination.isNotBlank() || tx.frequency.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tx.incomeSubType == "PASSIVE") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FinanceSecondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "🌱 Pasivo • ${tx.frequency}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = FinancePrimary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (tx.payoutDate.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "📅 ${tx.payoutDate}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4B5563)),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (tx.destination.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "🏦 ${tx.destination}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4B5563)),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (tx.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nota: ${tx.note}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF4B5563),
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    initialType: String,
    categories: List<String>,
    accounts: List<FinancialAccountEntity> = emptyList(),
    currencySymbol: String = "C$",
    lang: AppLanguage = AppLanguage.SPANISH,
    onAutoClassify: (String, String) -> String,
    onDismiss: () -> Unit,
    onConfirmMovement: (
        title: String,
        amount: Double,
        type: String,
        incomeSubType: String,
        frequency: String,
        payoutDate: String,
        destination: String,
        categoryGroup: String,
        category: String,
        subCategory: String,
        date: Long,
        note: String,
        accountId: Long?
    ) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var incomeSubType by remember { mutableStateOf("ACTIVE") } // "ACTIVE" or "PASSIVE"
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Active linked accounts
    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
    var selectedAccountId by remember(activeAccounts) { mutableStateOf(activeAccounts.firstOrNull()?.id) }

    // Passive Income State
    var passiveFrequency by remember { mutableStateOf("Mensual") }
    var payoutDate by remember { mutableStateOf("Día 15 de cada mes") }
    var selectedPassiveIndex by remember { mutableStateOf(0) }
    val passiveCategories = CategoryDataHierarchy.passiveIncomeCategories
    val selectedPassiveCategory = passiveCategories[selectedPassiveIndex]

    // Active Income State
    var selectedActiveIndex by remember { mutableStateOf(0) }
    val activeCategories = CategoryDataHierarchy.activeIncomeCategories

    // Expense State (Hierarchical)
    var selectedGroupType by remember { mutableStateOf(CategoryGroupType.FIXED_EXPENSE) }
    val currentGroupCategories = when (selectedGroupType) {
        CategoryGroupType.FIXED_EXPENSE -> CategoryDataHierarchy.fixedExpenseGroup.categories
        CategoryGroupType.VARIABLE_EXPENSE -> CategoryDataHierarchy.variableExpenseGroup.categories
        CategoryGroupType.DEBT_PAYMENT -> CategoryDataHierarchy.debtPaymentGroup.categories
        else -> CategoryDataHierarchy.fixedExpenseGroup.categories
    }
    var selectedCategoryIndex by remember(selectedGroupType) { mutableStateOf(0) }
    val safeCategoryIndex = selectedCategoryIndex.coerceIn(0, (currentGroupCategories.size - 1).coerceAtLeast(0))
    val selectedCategoryItem = currentGroupCategories.getOrNull(safeCategoryIndex) ?: currentGroupCategories.first()

    var selectedSubcategoryIndex by remember(selectedCategoryItem) { mutableStateOf(0) }
    val hasSubcategories = selectedCategoryItem.subcategories.isNotEmpty()
    val safeSubcategoryIndex = selectedSubcategoryIndex.coerceIn(0, (selectedCategoryItem.subcategories.size - 1).coerceAtLeast(0))
    val selectedSubcategoryItem = if (hasSubcategories) selectedCategoryItem.subcategories[safeSubcategoryIndex] else null

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (type == "INCOME") Localization.t("income", lang) else Localization.t("expense", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            errorMessage = null
                        },
                        label = { Text("📉 ${Localization.t("expense", lang)}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = FinanceError
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = type == "INCOME",
                        onClick = {
                            type = "INCOME"
                            errorMessage = null
                        },
                        label = { Text("📈 ${Localization.t("income", lang)}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSuccessContainer,
                            selectedLabelColor = FinanceSuccess
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    ErrorWarningBox(message = errorMessage!!)
                }

                // If INCOME: toggle Active vs Passive
                if (type == "INCOME") {
                    Text(
                        text = "Tipo de Ingreso:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = incomeSubType == "ACTIVE",
                            onClick = { incomeSubType = "ACTIVE" },
                            label = { Text("💼 ${Localization.t("active_income", lang)}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinanceSecondaryContainer,
                                selectedLabelColor = FinancePrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = incomeSubType == "PASSIVE",
                            onClick = { incomeSubType = "PASSIVE" },
                            label = { Text("🌱 ${Localization.t("passive_income", lang)}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD1FAE5),
                                selectedLabelColor = Color(0xFF065F46)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Passive Income specific details
                    if (incomeSubType == "PASSIVE") {
                        Text(
                            text = "Categoría de Ingreso Pasivo:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            passiveCategories.forEachIndexed { index, cat ->
                                FilterChip(
                                    selected = selectedPassiveIndex == index,
                                    onClick = { selectedPassiveIndex = index },
                                    label = { Text("${cat.iconEmoji} ${if (lang == AppLanguage.ENGLISH) cat.titleEn else cat.titleEs}") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Category Explanation Box
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = FinanceSecondaryContainer.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = FinancePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${selectedPassiveCategory.iconEmoji} ¿Qué es ${if (lang == AppLanguage.ENGLISH) selectedPassiveCategory.titleEn else selectedPassiveCategory.titleEs}?",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF004C73))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.ENGLISH) selectedPassiveCategory.explanationEn else selectedPassiveCategory.explanationEs,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF1F2937), lineHeight = 18.sp)
                                    )
                                }
                            }
                        }

                        // Frequency selection
                        Text(
                            text = Localization.t("frequency", lang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("Mensual", "Trimestral", "Semestral", "Anual", "Esporádico")) { freq ->
                                FilterChip(
                                    selected = passiveFrequency == freq,
                                    onClick = { passiveFrequency = freq },
                                    label = { Text(freq) }
                                )
                            }
                        }

                        // Collection Date
                        OutlinedTextField(
                            value = payoutDate,
                            onValueChange = { payoutDate = it },
                            label = { Text(Localization.t("payout_date", lang)) },
                            placeholder = { Text("ej. Día 15 de cada mes") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Active Income categories
                        Text(
                            text = "Categoría de Ingreso Activo:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(activeCategories.size) { index ->
                                val cat = activeCategories[index]
                                FilterChip(
                                    selected = selectedActiveIndex == index,
                                    onClick = { selectedActiveIndex = index },
                                    label = { Text("${cat.iconEmoji} ${if (lang == AppLanguage.ENGLISH) cat.nameEn else cat.nameEs}") }
                                )
                            }
                        }
                    }

                    // Destination Account for Income
                    Text(
                        text = Localization.t("income_dest_account_label", lang),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    if (activeAccounts.isEmpty()) {
                        Text(
                            text = if (lang == AppLanguage.SPANISH) "No hay herramientas financieras registradas. Se acreditará a la cuenta general." else "No registered financial tools. Will credit general balance.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(activeAccounts) { acc ->
                                val isSelected = selectedAccountId == acc.id
                                val accountLabel = if (acc.toolType == "BILLETERA") "Billetera" else "${acc.accountType} (${acc.debitSubType})"
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedAccountId = acc.id
                                        errorMessage = null
                                    },
                                    label = { Text("$accountLabel (${acc.bankName}) • $currencySymbol ${String.format(Locale.US, "%,.2f", acc.balance)}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FinanceSuccessContainer,
                                        selectedLabelColor = FinanceSuccess
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // EXPENSE HIERARCHY
                    Text(
                        text = "Grupo de Gastos:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedGroupType == CategoryGroupType.FIXED_EXPENSE,
                            onClick = {
                                selectedGroupType = CategoryGroupType.FIXED_EXPENSE
                                selectedCategoryIndex = 0
                                selectedSubcategoryIndex = 0
                            },
                            label = { Text("🔒 ${Localization.t("group_fixed_expenses", lang)}") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedGroupType == CategoryGroupType.VARIABLE_EXPENSE,
                            onClick = {
                                selectedGroupType = CategoryGroupType.VARIABLE_EXPENSE
                                selectedCategoryIndex = 0
                                selectedSubcategoryIndex = 0
                            },
                            label = { Text("🎯 ${Localization.t("group_variable_expenses", lang)}") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedGroupType == CategoryGroupType.DEBT_PAYMENT,
                            onClick = {
                                selectedGroupType = CategoryGroupType.DEBT_PAYMENT
                                selectedCategoryIndex = 0
                                selectedSubcategoryIndex = 0
                            },
                            label = { Text("💳 ${Localization.t("group_debt_payments", lang)}") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category within group
                    Text(
                        text = "Categoría Principal:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(currentGroupCategories.size) { idx ->
                            val item = currentGroupCategories[idx]
                            FilterChip(
                                selected = selectedCategoryIndex == idx,
                                onClick = {
                                    selectedCategoryIndex = idx
                                    selectedSubcategoryIndex = 0
                                },
                                label = { Text("${item.iconEmoji} ${if (lang == AppLanguage.ENGLISH) item.nameEn else item.nameEs}") }
                            )
                        }
                    }

                    // Subcategories if available
                    if (hasSubcategories) {
                        Text(
                            text = "Subcategoría Específica:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(selectedCategoryItem.subcategories.size) { subIdx ->
                                val sub = selectedCategoryItem.subcategories[subIdx]
                                FilterChip(
                                    selected = selectedSubcategoryIndex == subIdx,
                                    onClick = {
                                        selectedSubcategoryIndex = subIdx
                                        if (title.isBlank()) {
                                            title = if (lang == AppLanguage.ENGLISH) sub.nameEn else sub.nameEs
                                        }
                                    },
                                    label = { Text("${sub.iconEmoji} ${if (lang == AppLanguage.ENGLISH) sub.nameEn else sub.nameEs}") }
                                )
                            }
                        }
                    }

                    // Source account for Expense (Linked to user and validated against balance)
                    Text(
                        text = Localization.t("expense_source_account_label", lang),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    if (activeAccounts.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (lang == AppLanguage.SPANISH) "⚠️ No tienes billeteras o cuentas activas vinculadas. Agrega una desde la sección de Billeteras." else "⚠️ You have no active linked wallets or accounts. Add one from the Wallets section.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF92400E)),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(activeAccounts) { acc ->
                                val isSelected = selectedAccountId == acc.id
                                val accountLabel = if (acc.toolType == "BILLETERA") "Billetera" else "${acc.accountType} (${acc.debitSubType})"
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedAccountId = acc.id
                                        errorMessage = null
                                    },
                                    label = { Text("$accountLabel (${acc.bankName}) • $currencySymbol ${String.format(Locale.US, "%,.2f", acc.balance)}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FinanceSecondaryContainer,
                                        selectedLabelColor = FinancePrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // General Movement Fields: Concept & Amount
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text(Localization.t("concept", lang)) },
                    placeholder = { Text(Localization.t("concept_placeholder", lang)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_title_input")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it.filter { c -> c.isDigit() || c == '.' }
                        errorMessage = null
                    },
                    label = { Text("${Localization.t("amount", lang)} ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_amount_input")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.t("notes", lang)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    val trimmedTitle = title.trim().ifBlank {
                        if (type == "INCOME") {
                            if (incomeSubType == "PASSIVE") {
                                if (lang == AppLanguage.ENGLISH) selectedPassiveCategory.titleEn else selectedPassiveCategory.titleEs
                            } else {
                                if (lang == AppLanguage.ENGLISH) activeCategories[selectedActiveIndex].nameEn else activeCategories[selectedActiveIndex].nameEs
                            }
                        } else {
                            if (lang == AppLanguage.ENGLISH) {
                                selectedSubcategoryItem?.nameEn ?: selectedCategoryItem.nameEn
                            } else {
                                selectedSubcategoryItem?.nameEs ?: selectedCategoryItem.nameEs
                            }
                        }
                    }

                    if (trimmedTitle.isBlank()) {
                        errorMessage = Localization.t("error_empty_title", lang)
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMessage = Localization.t("error_invalid_amount", lang)
                        return@Button
                    }

                    val selectedAcc = activeAccounts.firstOrNull { it.id == selectedAccountId }
                    val selectedAccDisplayName = selectedAcc?.let {
                        if (it.toolType == "BILLETERA") "Billetera (${it.bankName})" else "${it.accountType} (${it.bankName})"
                    } ?: "Cuenta"

                    // Validación estricta de saldo suficiente en la billetera/cuenta de origen para gastos
                    if (type == "EXPENSE") {
                        if (activeAccounts.isNotEmpty() && selectedAcc == null) {
                            errorMessage = Localization.t("error_no_active_source_account", lang)
                            return@Button
                        }
                        if (selectedAcc != null && selectedAcc.balance < amount) {
                            val formattedBal = "$currencySymbol ${String.format(Locale.US, "%,.2f", selectedAcc.balance)}"
                            val formattedAmt = "$currencySymbol ${String.format(Locale.US, "%,.2f", amount)}"
                            errorMessage = String.format(
                                Localization.t("error_expense_insufficient_funds", lang),
                                selectedAccDisplayName,
                                formattedBal,
                                formattedAmt
                            )
                            return@Button
                        }
                    }

                    val finalGroup: String
                    val finalCategory: String
                    val finalSubCategory: String
                    val finalDestination: String
                    val finalFrequency: String
                    val finalPayoutDate: String

                    if (type == "INCOME") {
                        if (incomeSubType == "PASSIVE") {
                            finalGroup = "Ingresos Pasivos"
                            finalCategory = selectedPassiveCategory.titleEs
                            finalSubCategory = ""
                            finalDestination = selectedAcc?.let { "${if (it.toolType == "BILLETERA") "Billetera" else it.accountType} (${it.bankName})" } ?: "Ingreso General"
                            finalFrequency = passiveFrequency
                            finalPayoutDate = payoutDate.trim()
                        } else {
                            finalGroup = "Ingresos Activos"
                            finalCategory = activeCategories[selectedActiveIndex].nameEs
                            finalSubCategory = ""
                            finalDestination = selectedAcc?.let { "${if (it.toolType == "BILLETERA") "Billetera" else it.accountType} (${it.bankName})" } ?: "Ingreso General"
                            finalFrequency = ""
                            finalPayoutDate = ""
                        }
                    } else {
                        finalGroup = when (selectedGroupType) {
                            CategoryGroupType.FIXED_EXPENSE -> "Gastos Fijos"
                            CategoryGroupType.VARIABLE_EXPENSE -> "Gastos Variables"
                            CategoryGroupType.DEBT_PAYMENT -> "Pago de Deudas"
                            else -> "Gastos"
                        }
                        finalCategory = selectedCategoryItem.nameEs
                        finalSubCategory = selectedSubcategoryItem?.nameEs ?: ""
                        finalDestination = selectedAcc?.let { "${if (it.toolType == "BILLETERA") "Billetera" else it.accountType} (${it.bankName})" } ?: "Gasto General"
                        finalFrequency = ""
                        finalPayoutDate = ""
                    }

                    onConfirmMovement(
                        trimmedTitle,
                        amount,
                        type,
                        if (type == "INCOME") incomeSubType else "ACTIVE",
                        finalFrequency,
                        finalPayoutDate,
                        finalDestination,
                        finalGroup,
                        finalCategory,
                        finalSubCategory,
                        System.currentTimeMillis(),
                        note,
                        selectedAcc?.id
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                modifier = Modifier.testTag("add_tx_confirm_button")
            ) {
                Text(Localization.t("save_movement_btn", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.t("cancel", lang))
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

