package com.example.ui.bills

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.BillReminderEntity
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
import com.example.ui.theme.FinanceWarning
import com.example.ui.theme.FinanceWarningContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.bills.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingBills = bills.filter { !it.isPaid }
    val paidBills = bills.filter { it.isPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recordatorios de Pagos",
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
                onClick = { showAddDialog = true },
                containerColor = FinancePrimary,
                contentColor = FinanceOnPrimary,
                modifier = Modifier.testTag("fab_add_bill")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Recordatorio")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header summary
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pendingBills.isNotEmpty()) FinanceWarningContainer.copy(alpha = 0.5f) else FinanceSuccessContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (pendingBills.isNotEmpty()) FinanceWarning.copy(alpha = 0.2f) else FinanceSuccess.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (pendingBills.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (pendingBills.isNotEmpty()) FinanceWarning else FinanceSuccess,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (pendingBills.isNotEmpty()) "${pendingBills.size} Pagos Pendientes de Vencimiento" else "¡Al día con todos tus pagos!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            )
                            Text(
                                text = if (pendingBills.isNotEmpty()) "Total a pagar: ${formatCordobas(pendingBills.sumOf { it.amount })}" else "No tienes cuentas pendientes por pagar este mes.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4B5563))
                            )
                        }
                    }
                }
            }

            if (bills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No tienes recordatorios de pago",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B7280)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
                            ) {
                                Text("Agregar Primer Recordatorio")
                            }
                        }
                    }
                }
            } else {
                if (pendingBills.isNotEmpty()) {
                    item {
                        Text(
                            text = "Por Pagar (${pendingBills.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                    }
                    items(pendingBills) { bill ->
                        BillReminderCardItem(
                            bill = bill,
                            onTogglePaid = { viewModel.toggleBillPaid(bill) },
                            onDelete = { viewModel.deleteBillReminder(bill) }
                        )
                    }
                }

                if (paidBills.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pagados (${paidBills.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        )
                    }
                    items(paidBills) { bill ->
                        BillReminderCardItem(
                            bill = bill,
                            onTogglePaid = { viewModel.toggleBillPaid(bill) },
                            onDelete = { viewModel.deleteBillReminder(bill) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        if (showAddDialog) {
            AddBillDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, amount, daysAhead, category ->
                    val dueDate = System.currentTimeMillis() + (daysAhead * 86400000L)
                    viewModel.addBillReminder(title, amount, dueDate, category)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BillReminderCardItem(
    bill: BillReminderEntity,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val dueDateStr = sdf.format(Date(bill.dueDateMillis))

    val daysLeft = ((bill.dueDateMillis - System.currentTimeMillis()) / 86400000L).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) Color(0xFFF9FAFB) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (bill.isPaid) 0.dp else 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("bill_item_${bill.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = bill.isPaid,
                    onCheckedChange = { onTogglePaid() },
                    colors = CheckboxDefaults.colors(checkedColor = FinanceSuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (bill.isPaid) Color(0xFF9CA3AF) else Color(0xFF111827)
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Vence: $dueDateStr",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (bill.isPaid) Color(0xFF9CA3AF) else if (daysLeft <= 3) FinanceError else Color(0xFF6B7280),
                                fontWeight = if (!bill.isPaid && daysLeft <= 3) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        if (!bill.isPaid && daysLeft in 0..3) {
                            Text(
                                text = " • ¡Vence pronto!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = FinanceError,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCordobas(bill.amount),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (bill.isPaid) Color(0xFF9CA3AF) else Color(0xFF111827)
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, daysAhead: Int, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var daysAheadStr by remember { mutableStateOf("7") }
    var category by remember { mutableStateOf("Servicios Básicos") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nuevo Recordatorio de Pago",
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
                    label = { Text("Servicio o Factura (ej. Recibo de Luz)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = null
                    },
                    label = { Text("Monto a pagar (C$)") },
                    placeholder = { Text("ej. 950.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = daysAheadStr,
                    onValueChange = {
                        daysAheadStr = it.filter { c -> c.isDigit() }
                    },
                    label = { Text("Días hasta la fecha de vencimiento") },
                    placeholder = { Text("ej. 7") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    val days = daysAheadStr.toIntOrNull() ?: 7
                    if (title.isBlank()) {
                        errorMsg = "Por favor ingresa un título para el recordatorio."
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMsg = "Por favor ingresa un monto válido mayor a 0."
                        return@Button
                    }
                    onConfirm(title, amount, days, category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
            ) {
                Text("Guardar Recordatorio", fontWeight = FontWeight.Bold)
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
