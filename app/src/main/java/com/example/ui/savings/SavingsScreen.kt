package com.example.ui.savings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavingsGoalEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatCordobas
import com.example.ui.theme.FinanceBackground
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
fun SavingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.savingsGoals.collectAsState()
    val simMonthly by viewModel.simMonthlyAmount.collectAsState()
    val simMonths by viewModel.simMonths.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var depositGoalTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val (principalTotal, accumulatedTotal) = viewModel.calculateSimulationTotal()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Metas y Simulador de Ahorro",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinanceBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = FinancePrimary,
                contentColor = FinanceOnPrimary,
                modifier = Modifier.testTag("fab_add_savings_goal")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Meta de Ahorro")
            }
        },
        containerColor = FinanceBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Savings Simulator Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_simulator_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FinanceSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = FinancePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Simulador de Ahorro Inteligente",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                                Text(
                                    text = "Proyecta cuánto tendrás acumulado en el tiempo",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Monthly Contribution Preset Chips
                        Text(
                            text = "Ahorro mensual sugerido: ${formatCordobas(simMonthly)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = FinancePrimary
                            )
                        )

                        Slider(
                            value = simMonthly.toFloat(),
                            onValueChange = { viewModel.updateSimulator(it.toDouble(), simMonths) },
                            valueRange = 100f..5000f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = FinancePrimary,
                                activeTrackColor = FinancePrimary
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(200.0, 500.0, 1000.0, 2000.0).forEach { amount ->
                                FilterChip(
                                    selected = simMonthly == amount,
                                    onClick = { viewModel.updateSimulator(amount, simMonths) },
                                    label = { Text("C$ ${amount.toInt()}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FinanceSecondaryContainer,
                                        selectedLabelColor = FinancePrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time Horizon Chips (6, 12, 24, 36 months)
                        Text(
                            text = "Plazo de tiempo: $simMonths meses (${simMonths / 12.0} años)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151)
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            listOf(6 to "6 meses", 12 to "1 año (12m)", 24 to "2 años", 36 to "3 años").forEach { (months, label) ->
                                FilterChip(
                                    selected = simMonths == months,
                                    onClick = { viewModel.updateSimulator(simMonthly, months) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FinanceSecondaryContainer,
                                        selectedLabelColor = FinancePrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Simulation Result Banner
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FinanceSuccessContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "💡 Resultado de la Simulación:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Si ahorras ${formatCordobas(simMonthly)} al mes, en $simMonths meses tendrás ${formatCordobas(principalTotal)} en aportes directos.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D)
                                    )
                                )
                                Text(
                                    text = "Con un rendimiento anual estimado del 5%, alcanzarías aprox. ${formatCordobas(accumulatedTotal)}.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF14532D).copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Savings Goals List
            item {
                SectionHeader(
                    title = "Tus Metas de Ahorro",
                    actionText = "+ Nueva Meta",
                    onActionClick = { showAddGoalDialog = true }
                )
            }

            if (goals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tienes metas de ahorro registradas",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddGoalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
                            ) {
                                Text("Crear Primera Meta")
                            }
                        }
                    }
                }
            } else {
                items(goals) { goal ->
                    SavingsGoalCardItem(
                        goal = goal,
                        onDeposit = { depositGoalTarget = goal },
                        onDelete = { viewModel.deleteSavingsGoal(goal) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Add Savings Goal Dialog
        if (showAddGoalDialog) {
            AddSavingsGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { title, target, initial ->
                    val now = System.currentTimeMillis()
                    val targetDate = now + (180L * 86400000L) // 6 months default
                    viewModel.addSavingsGoal(title, target, initial, targetDate)
                    showAddGoalDialog = false
                }
            )
        }

        // Deposit to Goal Dialog
        depositGoalTarget?.let { goal ->
            DepositToGoalDialog(
                goal = goal,
                onDismiss = { depositGoalTarget = null },
                onConfirm = { amount ->
                    viewModel.addFundsToGoal(goal, amount)
                    depositGoalTarget = null
                }
            )
        }
    }
}

@Composable
fun SavingsGoalCardItem(
    goal: SavingsGoalEntity,
    onDeposit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount) * 100 else 0.0
    val isCompleted = goal.currentAmount >= goal.targetAmount

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FinanceSuccessContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = FinanceSuccess,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                        Text(
                            text = if (isCompleted) "🎉 ¡Meta completada al 100%!" else "${String.format(Locale.US, "%.1f", percentage)}% alcanzado",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isCompleted) FinanceSuccess else FinancePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = FinanceSuccess,
                trackColor = Color(0xFFE5E7EB),
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ahorrado: ${formatCordobas(goal.currentAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    )
                    Text(
                        text = "Objetivo: ${formatCordobas(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }

                Button(
                    onClick = onDeposit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceSecondaryContainer),
                    modifier = Modifier.testTag("deposit_button_${goal.id}")
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Abonar", color = FinancePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddSavingsGoalDialog(
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
                text = "Nueva Meta de Ahorro",
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
                    label = { Text("Nombre de la meta (ej. Vacaciones, Auto)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = {
                        targetStr = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = null
                    },
                    label = { Text("Monto Objetivo (C$)") },
                    placeholder = { Text("ej. 20000.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialStr,
                    onValueChange = {
                        initialStr = it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Aporte Inicial (C$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull()
                    val initial = initialStr.toDoubleOrNull() ?: 0.0
                    if (title.isBlank()) {
                        errorMsg = "Por favor ingresa un título para la meta."
                        return@Button
                    }
                    if (target == null || target <= 0.0) {
                        errorMsg = "Por favor ingresa un monto objetivo válido mayor a 0."
                        return@Button
                    }
                    onConfirm(title, target, initial)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
            ) {
                Text("Guardar Meta", fontWeight = FontWeight.Bold)
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

@Composable
fun DepositToGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Abonar a '${goal.title}'",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ahorrado actual: ${formatCordobas(goal.currentAmount)} / ${formatCordobas(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF4B5563))
                )

                if (errorMsg != null) {
                    ErrorWarningBox(message = errorMsg!!)
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = null
                    },
                    label = { Text("Monto a abonar (C$)") },
                    placeholder = { Text("ej. 500.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMsg = "Ingresa un monto válido mayor a 0."
                        return@Button
                    }
                    onConfirm(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
            ) {
                Text("Abonar Fondos", fontWeight = FontWeight.Bold)
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
