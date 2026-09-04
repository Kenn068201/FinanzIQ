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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategoryFilter.collectAsState()
    val selectedPeriod by viewModel.selectedPeriodFilter.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var dialogInitialType by remember { mutableStateOf("EXPENSE") }
    var showExportDialog by remember { mutableStateOf(false) }

    val filteredList = viewModel.getFilteredTransactions()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Movimientos",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Exportar Reporte",
                            tint = FinancePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinanceBackground)
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
                Icon(Icons.Default.Add, contentDescription = "Nuevo Movimiento")
            }
        },
        containerColor = FinanceBackground,
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
                placeholder = { Text("Buscar por concepto, categoría o nota...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = FinancePrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
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
                listOf("Semana", "Mes", "Año", "Todos").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.setPeriodFilter(period) },
                        label = { Text(period) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier.testTag("filter_period_$period")
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
                        label = { Text("Todas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        )
                    )
                }
                items(categories.map { it.name }.distinct()) { catName ->
                    FilterChip(
                        selected = selectedCat == catName,
                        onClick = { viewModel.setCategoryFilter(catName) },
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
                listOf("TODOS" to "Todos", "INCOME" to "Ingresos", "EXPENSE" to "Gastos").forEach { (key, label) ->
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
                            text = "No se encontraron movimientos",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280)
                            )
                        )
                        Text(
                            text = "Prueba cambiando los filtros o registra uno nuevo",
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
                onAutoClassify = { title, type -> viewModel.autoSuggestCategory(title, type) },
                onDismiss = { showAddDialog = false },
                onConfirm = { title, amount, type, category, date, note ->
                    viewModel.addTransaction(title, amount, type, category, date, note)
                    showAddDialog = false
                }
            )
        }

        // Export Report Dialog
        if (showExportDialog) {
            val reportContent = viewModel.generateExportContent()
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = {
                    Text(
                        text = "Exportar Reporte Financiero",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Puedes compartir tu reporte completo en formato compatible con Excel (CSV) y texto estructurado para PDF.",
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
                                putExtra(Intent.EXTRA_SUBJECT, "Reporte Financiero - Finanzas Inteligentes")
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
                        Text("Compartir / Exportar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cerrar")
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun TransactionCardDetail(
    tx: TransactionEntity,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Text(
                            text = "${tx.category} • $dateStr",
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

            if (tx.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
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
    onAutoClassify: (String, String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, type: String, category: String, date: Long, note: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Auto") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto classify live preview
    val autoSuggested = if (title.isNotBlank()) onAutoClassify(title, type) else "Alimentación"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (type == "INCOME") "Registrar Ingreso" else "Registrar Gasto",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("Gasto") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = FinanceError
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("Ingreso") },
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

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Concepto (ej. Supermercado, Gasolina)") },
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
                    label = { Text("Monto en Córdobas (C$)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_amount_input")
                )

                // Category selection with Auto-classification badge
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categoría:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = FinancePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auto: $autoSuggested",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = FinancePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == "Auto",
                                onClick = { selectedCategory = "Auto" },
                                label = { Text("✨ Auto-clasificar") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FinanceSecondaryContainer,
                                    selectedLabelColor = FinancePrimary
                                )
                            )
                        }
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota opcional") },
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
                    if (title.isBlank()) {
                        errorMessage = "Por favor ingresa un concepto para el movimiento."
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Por favor ingresa un monto válido mayor a 0."
                        return@Button
                    }
                    val finalCategory = if (selectedCategory == "Auto") autoSuggested else selectedCategory
                    onConfirm(title, amount, type, finalCategory, System.currentTimeMillis(), note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                modifier = Modifier.testTag("add_tx_confirm_button")
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White
    )
}
