package com.example.ui.savings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FinancialAccountEntity
import com.example.data.local.SavingsGoalEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
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

/**
 * Interfaz de Metas y Simulador de Ahorro Inteligente de FinanzIQ.
 * Incluye:
 * 1. Simulación mensual exacta (Monto ingresado/sugerido x Plazo en meses de 6m a 3 años).
 * 2. Simulación de ahorro diario con validaciones de fechas de inicio y fin y monto mayor a 0.
 * 3. Tarjetas de metas con barras de progreso y porcentaje exacto.
 * 4. Formulario de abono que descuenta directamente de la cuenta o billetera activa seleccionada del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val goals by viewModel.savingsGoals.collectAsState()
    val financialAccounts by viewModel.financialAccounts.collectAsState()

    val simMonthly by viewModel.simMonthlyAmount.collectAsState()
    val simMonths by viewModel.simMonths.collectAsState()

    val simDailyAmount by viewModel.simDailyAmount.collectAsState()
    val simDailyStart by viewModel.simDailyStartDate.collectAsState()
    val simDailyEnd by viewModel.simDailyEndDate.collectAsState()

    var simulatorTab by remember { mutableIntStateOf(0) } // 0: Mensual, 1: Diario
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var depositGoalTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    // Cálculo exacto mensual
    val (principalTotal, _) = viewModel.calculateSimulationTotal()

    // Cálculo exacto diario
    val (dailyDays, dailyTotal, dailyError) = viewModel.calculateDailySimulation()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.t("savings_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = FinancePrimary,
                contentColor = FinanceOnPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_savings_goal")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = Localization.t("add_goal_title", lang),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta Principal del Simulador de Ahorro
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_simulator_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FinanceSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = FinancePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Localization.t("simulator_header", lang),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = Localization.t("simulator_subtitle", lang),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Selector de modo de simulación (Mensual vs Diario)
                        TabRow(
                            selectedTabIndex = simulatorTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = FinancePrimary,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Tab(
                                selected = simulatorTab == 0,
                                onClick = { simulatorTab = 0 },
                                text = { Text(Localization.t("sim_mode_monthly", lang), fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = simulatorTab == 1,
                                onClick = { simulatorTab = 1 },
                                text = { Text(Localization.t("sim_mode_daily", lang), fontWeight = FontWeight.Bold) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (simulatorTab == 0) {
                            // --- SIMULACIÓN MENSUAL EXACTA ---
                            val currentPrincipalTotal = simMonthly * simMonths

                            // Campo de Entrada Manual de Monto Mensual
                            OutlinedTextField(
                                value = if (simMonthly > 0) String.format(Locale.US, "%.2f", simMonthly).removeSuffix(".00") else "",
                                onValueChange = {
                                    val newAmount = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateSimulator(newAmount, simMonths)
                                },
                                label = { Text("${Localization.t("sim_monthly_label", lang)} (${currency.symbol})") },
                                placeholder = { Text("500.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sim_monthly_amount_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Slider interactivo de monto mensual
                            Slider(
                                value = simMonthly.toFloat().coerceIn(100f, 10000f),
                                onValueChange = { viewModel.updateSimulator(it.toDouble(), simMonths) },
                                valueRange = 100f..10000f,
                                steps = 99,
                                colors = SliderDefaults.colors(
                                    thumbColor = FinancePrimary,
                                    activeTrackColor = FinancePrimary
                                )
                            )

                            // Chips de montos sugeridos
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(200.0, 500.0, 1000.0, 2500.0, 5000.0).forEach { amount ->
                                    FilterChip(
                                        selected = simMonthly == amount,
                                        onClick = { viewModel.updateSimulator(amount, simMonths) },
                                        label = { Text("${currency.symbol} ${amount.toInt()}", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FinanceSecondaryContainer,
                                            selectedLabelColor = FinancePrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Variación de Periodos de Tiempo (6 meses, 1 a 3 años)
                            Text(
                                text = "${Localization.t("sim_time_horizon_label", lang)} $simMonths ${Localization.t("months", lang)} (${String.format(Locale.US, "%.1f", simMonths / 12.0)} ${Localization.t("years", lang)})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                listOf(
                                    6 to Localization.t("sim_6_months", lang),
                                    12 to Localization.t("sim_1_year", lang),
                                    24 to Localization.t("sim_2_years", lang),
                                    36 to Localization.t("sim_3_years", lang)
                                ).forEach { (months, label) ->
                                    FilterChip(
                                        selected = simMonths == months,
                                        onClick = { viewModel.updateSimulator(simMonthly, months) },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FinanceSecondaryContainer,
                                            selectedLabelColor = FinancePrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cuadro de Resultado de Cálculo Exacto Mensual
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = FinanceSuccessContainer),
                                modifier = Modifier.fillMaxWidth().testTag("sim_monthly_result_box")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = Localization.t("sim_result_header", lang),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = Localization.t(
                                            "sim_result_monthly_text",
                                            lang,
                                            "${currency.symbol} ${String.format(Locale.US, "%,.2f", simMonthly)}",
                                            "$simMonths",
                                            "${currency.symbol} ${String.format(Locale.US, "%,.2f", currentPrincipalTotal)}",
                                            "${currency.symbol} ${String.format(Locale.US, "%,.2f", simMonthly)}",
                                            "$simMonths"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "(${currency.symbol} ${String.format(Locale.US, "%,.2f", simMonthly)} × $simMonths meses = ${currency.symbol} ${String.format(Locale.US, "%,.2f", currentPrincipalTotal)})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF166534),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        } else {
                            // --- SIMULACIÓN DIARIA EXACTA ---
                            if (dailyError != null) {
                                ErrorWarningBox(message = dailyError)
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Campo de Monto Diario
                            OutlinedTextField(
                                value = if (simDailyAmount > 0) String.format(Locale.US, "%.2f", simDailyAmount).removeSuffix(".00") else "",
                                onValueChange = {
                                    val newAmount = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateDailySimulator(newAmount, simDailyStart, simDailyEnd)
                                },
                                label = { Text("${Localization.t("sim_daily_amount_label", lang)} (${currency.symbol})") },
                                placeholder = { Text("50.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sim_daily_amount_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Chips de Montos Diarios Sugeridos
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(20.0, 50.0, 100.0, 200.0, 500.0).forEach { amt ->
                                    FilterChip(
                                        selected = simDailyAmount == amt,
                                        onClick = { viewModel.updateDailySimulator(amt, simDailyStart, simDailyEnd) },
                                        label = { Text("${currency.symbol} ${amt.toInt()}", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FinanceSecondaryContainer,
                                            selectedLabelColor = FinancePrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Fecha de Inicio (Valida >= Hoy)
                            OutlinedTextField(
                                value = simDailyStart,
                                onValueChange = {
                                    viewModel.updateDailySimulator(simDailyAmount, it, simDailyEnd)
                                },
                                label = { Text(Localization.t("sim_daily_start_date", lang)) },
                                placeholder = { Text("AAAA-MM-DD") },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = FinancePrimary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sim_daily_start_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fecha de Fin (Valida > Fecha Inicio)
                            OutlinedTextField(
                                value = simDailyEnd,
                                onValueChange = {
                                    viewModel.updateDailySimulator(simDailyAmount, simDailyStart, it)
                                },
                                label = { Text(Localization.t("sim_daily_end_date", lang)) },
                                placeholder = { Text("AAAA-MM-DD") },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = FinancePrimary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sim_daily_end_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cuadro de Resultado Diario Exacto
                            if (dailyError == null && dailyDays > 0) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = FinanceSuccessContainer),
                                    modifier = Modifier.fillMaxWidth().testTag("sim_daily_result_box")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = Localization.t("sim_result_header", lang),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF14532D)
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = Localization.t(
                                                "sim_result_daily_text",
                                                lang,
                                                "${currency.symbol} ${String.format(Locale.US, "%,.2f", simDailyAmount)}",
                                                simDailyStart,
                                                simDailyEnd,
                                                "$dailyDays",
                                                "${currency.symbol} ${String.format(Locale.US, "%,.2f", dailyTotal)}",
                                                "${currency.symbol} ${String.format(Locale.US, "%,.2f", simDailyAmount)}",
                                                "$dailyDays"
                                            ),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF14532D)
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "(${currency.symbol} ${String.format(Locale.US, "%,.2f", simDailyAmount)} × $dailyDays días = ${currency.symbol} ${String.format(Locale.US, "%,.2f", dailyTotal)})",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF166534),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Encabezado de la lista de Metas de Ahorro
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.t("savings_goals_header", lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    TextButton(onClick = { showAddGoalDialog = true }) {
                        Text(
                            text = "+ ${Localization.t("new_goal_btn", lang)}",
                            color = FinancePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Listado de Metas de Ahorro
            if (goals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = Localization.t("empty_savings_goals", lang),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddGoalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(Localization.t("create_first_goal", lang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    SavingsGoalProgressCard(
                        goal = goal,
                        currencySymbol = currency.symbol,
                        lang = lang,
                        onDeposit = { depositGoalTarget = goal },
                        onDelete = { viewModel.deleteSavingsGoal(goal) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Diálogo para Agregar Nueva Meta de Ahorro
        if (showAddGoalDialog) {
            AddSavingsGoalModal(
                lang = lang,
                currencySymbol = currency.symbol,
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { title, target, initial ->
                    val now = System.currentTimeMillis()
                    val targetDate = now + (180L * 86400000L) // 6 meses por defecto
                    viewModel.addSavingsGoal(title, target, initial, targetDate)
                    showAddGoalDialog = false
                }
            )
        }

        // Diálogo para Abonar a Meta con Deducción de Cuenta/Billetera del Usuario
        depositGoalTarget?.let { goal ->
            DepositToGoalWithAccountModal(
                goal = goal,
                viewModel = viewModel,
                accounts = financialAccounts,
                lang = lang,
                currencySymbol = currency.symbol,
                onDismiss = { depositGoalTarget = null },
                onSuccess = {
                    depositGoalTarget = null
                }
            )
        }
    }
}

/**
 * Tarjeta de Meta de Ahorro con Barra de Progreso y Porcentaje Exacto.
 */
@Composable
fun SavingsGoalProgressCard(
    goal: SavingsGoalEntity,
    currencySymbol: String,
    lang: com.example.util.AppLanguage,
    onDeposit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount) * 100 else 0.0
    val isCompleted = goal.currentAmount >= goal.targetAmount

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("savings_goal_item_${goal.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) FinanceSuccessContainer else FinanceSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (isCompleted) FinanceSuccess else FinancePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = if (isCompleted) {
                                "🎉 ${Localization.t("goal_completed_badge", lang)}"
                            } else {
                                "${String.format(Locale.US, "%.1f", percentage)}% ${Localization.t("goal_progress_badge", lang)}"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isCompleted) FinanceSuccess else FinancePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = Localization.t("delete", lang),
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra de progreso interactiva
            LinearProgressIndicator(
                progress = { progress },
                color = if (isCompleted) FinanceSuccess else FinancePrimary,
                trackColor = Color(0xFFE2E8F0),
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${Localization.t("goal_saved_label", lang)} $currencySymbol ${String.format(Locale.US, "%,.2f", goal.currentAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${Localization.t("goal_target_label", lang)} $currencySymbol ${String.format(Locale.US, "%,.2f", goal.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Button(
                    onClick = onDeposit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceSecondaryContainer),
                    modifier = Modifier.testTag("deposit_button_${goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = FinancePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.t("deposit_funds_btn", lang),
                        color = FinancePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Modal para registrar una nueva meta de ahorro.
 */
@Composable
fun AddSavingsGoalModal(
    lang: com.example.util.AppLanguage,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, target: Double, initial: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var initialStr by remember { mutableStateOf("0") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.t("add_goal_title", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMsg != null) {
                    ErrorWarningBox(message = errorMsg!!)
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMsg = null
                    },
                    label = { Text(Localization.t("goal_title_placeholder", lang)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = {
                        targetStr = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = null
                    },
                    label = { Text("${Localization.t("goal_target_amount_label", lang)} ($currencySymbol)") },
                    placeholder = { Text("20000.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("goal_target_input")
                )

                OutlinedTextField(
                    value = initialStr,
                    onValueChange = {
                        initialStr = it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("${Localization.t("goal_initial_deposit_label", lang)} ($currencySymbol)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("goal_initial_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull()
                    val initial = initialStr.toDoubleOrNull() ?: 0.0
                    if (title.isBlank()) {
                        errorMsg = Localization.t("error_goal_title_required", lang)
                        return@Button
                    }
                    if (target == null || target <= 0.0) {
                        errorMsg = Localization.t("error_goal_target_invalid", lang)
                        return@Button
                    }
                    onConfirm(title, target, initial)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(Localization.t("save_goal_btn", lang), fontWeight = FontWeight.Bold)
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

/**
 * Modal de Abono a Meta de Ahorro:
 * Permite seleccionar la herramienta financiera (Cuenta de Débito, Crédito o Billetera Digital activa)
 * del usuario para deducir los fondos de manera sincronizada.
 */
@Composable
fun DepositToGoalWithAccountModal(
    goal: SavingsGoalEntity,
    viewModel: FinanceViewModel,
    accounts: List<FinancialAccountEntity>,
    lang: com.example.util.AppLanguage,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
    var selectedAccount by remember { mutableStateOf<FinancialAccountEntity?>(activeAccounts.firstOrNull()) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var amountStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Encabezado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = FinanceSuccess,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${Localization.t("deposit_to_goal_title", lang)} '${goal.title}'",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = Localization.t("close", lang))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${Localization.t("goal_saved_label", lang)} $currencySymbol ${String.format(Locale.US, "%,.2f", goal.currentAmount)} / $currencySymbol ${String.format(Locale.US, "%,.2f", goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mensaje de Error
                if (errorMsg != null) {
                    ErrorWarningBox(message = errorMsg!!)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Selector de Cuenta o Billetera de Origen
                Text(
                    text = Localization.t("deposit_source_account_label", lang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (activeAccounts.isEmpty()) {
                    ErrorWarningBox(message = Localization.t("error_no_active_accounts_for_deposit", lang))
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .clickable { dropdownExpanded = true }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (selectedAccount?.toolType == "BILLETERA") Icons.Default.AccountBalanceWallet else Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = FinancePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedAccount != null) {
                                            "${selectedAccount!!.bankName} • ${selectedAccount!!.accountType} (${selectedAccount!!.accountNumber})"
                                        } else Localization.t("select_account_placeholder", lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (selectedAccount != null) {
                                        Text(
                                            text = "${Localization.t("available_prefix", lang)} $currencySymbol ${String.format(Locale.US, "%,.2f", selectedAccount!!.balance)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (selectedAccount!!.balance > 0) FinanceSuccess else FinanceError
                                        )
                                    }
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            activeAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${acc.bankName} - ${acc.accountType} (${acc.accountNumber})", fontWeight = FontWeight.SemiBold)
                                            Text("$currencySymbol ${String.format(Locale.US, "%,.2f", acc.balance)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        selectedAccount = acc
                                        dropdownExpanded = false
                                        errorMsg = null
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo de Monto a Abonar
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = null
                    },
                    label = { Text("${Localization.t("deposit_amount_label", lang)} ($currencySymbol)") },
                    placeholder = { Text("500.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Localization.t("cancel", lang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val acc = selectedAccount
                            val amount = amountStr.toDoubleOrNull()

                            if (acc == null) {
                                errorMsg = Localization.t("error_deposit_source_required", lang)
                                return@Button
                            }

                            if (amount == null || amount <= 0.0) {
                                errorMsg = Localization.t("error_deposit_amount_invalid", lang)
                                return@Button
                            }

                            viewModel.depositFundsToSavingsGoal(
                                goal = goal,
                                sourceAccount = acc,
                                amount = amount
                            ) { success, err ->
                                if (success) {
                                    onSuccess()
                                } else {
                                    errorMsg = err
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_deposit_button")
                    ) {
                        Text(Localization.t("confirm_deposit_btn", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
