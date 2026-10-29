package com.example.ui.bills

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BillReminderEntity
import com.example.data.local.FinancialAccountEntity
import com.example.data.model.CategoryDataHierarchy
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.components.formatCordobas
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSuccess
import com.example.ui.theme.FinanceSuccessContainer
import com.example.ui.theme.FinanceWarning
import com.example.ui.theme.FinanceWarningContainer
import com.example.util.AppLanguage
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Interfaz de Recordatorios de Pagos y Facturas.
 * Permite programar alertas con descripción, monto y fecha límite de pago superior a la actual.
 * Emite alertas inmediatas si el pago es en los próximos 2 días, alertas el día antes de vencer,
 * y al marcar como pagada despliega el formulario para seleccionar cuenta y categoría de gasto existente,
 * asignando automáticamente la descripción como "Pago de Factura".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val accounts by viewModel.financialAccounts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var billToPay by remember { mutableStateOf<BillReminderEntity?>(null) }
    var immediateAlertBill by remember { mutableStateOf<BillReminderEntity?>(null) }

    val pendingBills = bills.filter { !it.isPaid }
    val paidBills = bills.filter { it.isPaid }

    // Detección de facturas en fecha previa (el día antes) o que vencen en menos de 48 horas
    val nowMillis = System.currentTimeMillis()
    val urgentAlertBills = pendingBills.filter { bill ->
        val diffMillis = bill.dueDateMillis - nowMillis
        val daysLeft = (diffMillis / 86400000L).toInt()
        daysLeft <= 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.t("bills_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FinancePrimary,
                contentColor = FinanceOnPrimary,
                modifier = Modifier.testTag("fab_add_bill")
            ) {
                Icon(Icons.Default.Add, contentDescription = Localization.t("add_bill_btn", lang))
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta de resumen de pagos
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pendingBills.isNotEmpty()) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
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
                                text = if (pendingBills.isNotEmpty()) {
                                    if (lang == AppLanguage.SPANISH) "${pendingBills.size} Pagos Pendientes de Vencimiento"
                                    else "${pendingBills.size} Pending Payments Due"
                                } else {
                                    if (lang == AppLanguage.SPANISH) "¡Al día con todos tus pagos!"
                                    else "Up to date with all payments!"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (pendingBills.isNotEmpty()) {
                                    if (lang == AppLanguage.SPANISH) "Total a pagar: ${formatCordobas(pendingBills.sumOf { it.amount })}"
                                    else "Total to pay: ${formatCordobas(pendingBills.sumOf { it.amount })}"
                                } else {
                                    if (lang == AppLanguage.SPANISH) "No tienes cuentas pendientes por pagar este mes."
                                    else "You have no unpaid bills this month."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // Alerta prominente cuando llega la fecha previa (el día antes) o fecha inmediata
            if (urgentAlertBills.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        urgentAlertBills.forEach { urgentBill ->
                            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                            val dueDateStr = sdf.format(Date(urgentBill.dueDateMillis))
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FinanceError.copy(alpha = 0.5f))),
                                modifier = Modifier.fillMaxWidth().testTag("urgent_bill_alert_${urgentBill.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = FinanceError,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = Localization.t("bill_alert_banner_title", lang),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = FinanceError
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = Localization.t("bill_alert_banner_desc", lang, urgentBill.title, formatCordobas(urgentBill.amount), dueDateStr),
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D))
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { billToPay = urgentBill },
                                        colors = ButtonDefaults.buttonColors(containerColor = FinanceError),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(
                                            text = if (lang == AppLanguage.SPANISH) "Pagar Factura" else "Pay Bill",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lista vacía o contenido
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
                                text = Localization.t("no_bills_empty", lang),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B7280)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Localization.t("no_bills_empty_desc", lang),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
                            ) {
                                Text(Localization.t("add_bill_btn", lang))
                            }
                        }
                    }
                }
            } else {
                if (pendingBills.isNotEmpty()) {
                    item {
                        Text(
                            text = if (lang == AppLanguage.SPANISH) "Por Pagar (${pendingBills.size})" else "Pending (${pendingBills.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                    }
                    items(pendingBills) { bill ->
                        BillReminderCardItem(
                            bill = bill,
                            lang = lang,
                            onMarkPaidClick = {
                                // Al marcar la factura como pagada, se abre el formulario
                                billToPay = bill
                            },
                            onDelete = { viewModel.deleteBillReminder(bill) }
                        )
                    }
                }

                if (paidBills.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == AppLanguage.SPANISH) "Pagados (${paidBills.size})" else "Paid (${paidBills.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        )
                    }
                    items(paidBills) { bill ->
                        BillReminderCardItem(
                            bill = bill,
                            lang = lang,
                            onMarkPaidClick = {
                                // Desmarcar como pagado
                                viewModel.toggleBillPaid(bill)
                            },
                            onDelete = { viewModel.deleteBillReminder(bill) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Formulario para programar nuevo recordatorio de pago
        if (showAddDialog) {
            AddBillDialog(
                lang = lang,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, amount, dueDateMillis, category, isWithinTwoDays ->
                    viewModel.addBillReminder(title, amount, dueDateMillis, category)
                    showAddDialog = false
                    // Si la fecha ingresada es en los próximos 2 días, emitir la alerta inmediatamente
                    if (isWithinTwoDays) {
                        immediateAlertBill = BillReminderEntity(
                            id = 0,
                            userId = 0,
                            title = title,
                            amount = amount,
                            dueDateMillis = dueDateMillis,
                            category = category
                        )
                    }
                }
            )
        }

        // Formulario que se abre cuando el usuario marca la factura como pagada
        billToPay?.let { bill ->
            PayBillDialog(
                bill = bill,
                accounts = accounts.filter { it.isActive },
                lang = lang,
                onDismiss = { billToPay = null },
                onConfirmPayment = { selectedAccount, selectedCategory ->
                    viewModel.payBillReminder(bill, selectedAccount, selectedCategory) { success, _ ->
                        if (success) {
                            billToPay = null
                        }
                    }
                }
            )
        }

        // Alerta inmediata emitida tras registrar si la fecha está en los próximos 2 días
        immediateAlertBill?.let { bill ->
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            val dueDateStr = sdf.format(Date(bill.dueDateMillis))
            AlertDialog(
                onDismissRequest = { immediateAlertBill = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = FinanceWarning,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = Localization.t("bill_alert_immediate_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = Localization.t("bill_alert_immediate_desc", lang, dueDateStr, formatCordobas(bill.amount)),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF374151))
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { immediateAlertBill = null },
                        colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
                    ) {
                        Text(if (lang == AppLanguage.SPANISH) "Entendido" else "Understood", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

/**
 * Tarjeta individual de recordatorio de pago con indicador de vencimiento y acción de pago.
 */
@Composable
fun BillReminderCardItem(
    bill: BillReminderEntity,
    lang: AppLanguage,
    onMarkPaidClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val dueDateStr = sdf.format(Date(bill.dueDateMillis))

    val daysLeft = ((bill.dueDateMillis - System.currentTimeMillis()) / 86400000L).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
            width = 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                    onCheckedChange = { onMarkPaidClick() },
                    colors = CheckboxDefaults.colors(checkedColor = FinanceSuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${Localization.t("bill_due_date", lang)}: $dueDateStr",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else if (daysLeft <= 1) FinanceError else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (!bill.isPaid && daysLeft <= 1) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        if (!bill.isPaid && daysLeft in 0..1) {
                            Text(
                                text = if (lang == AppLanguage.SPANISH) " • ¡Vence pronto!" else " • Due soon!",
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
                        color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Formulario para programar alertas de recordatorio para pagos de factura.
 * Valida que la fecha límite sea estrictamente superior a la fecha actual del sistema.
 * Si es dentro de los próximos 2 días, indica bandera para emitir alerta inmediata.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, dueDateMillis: Long, category: String, isWithinTwoDays: Boolean) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    // Fecha límite seleccionada por defecto: 3 días a las 23:59
    val defaultDueDate = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }
    var selectedDueDateMillis by remember { mutableStateOf(defaultDueDate) }

    val expenseCategories = remember {
        CategoryDataHierarchy.fixedExpenseGroup.categories +
            CategoryDataHierarchy.variableExpenseGroup.categories +
            CategoryDataHierarchy.debtPaymentGroup.categories
    }
    var selectedCategory by remember { mutableStateOf(expenseCategories.firstOrNull()?.nameEs ?: "Servicios Básicos") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val dueDateFormatted = sdf.format(Date(selectedDueDateMillis))

    fun showDatePicker() {
        val c = Calendar.getInstance().apply { timeInMillis = selectedDueDateMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                selectedDueDateMillis = chosenCal.timeInMillis
                errorMsg = null
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.t("add_bill_title", lang),
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

                // Descripción de la factura o servicio
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMsg = null
                    },
                    label = { Text(Localization.t("bill_service_name", lang)) },
                    placeholder = { Text(if (lang == AppLanguage.SPANISH) "ej. Recibo de Luz Eléctrica" else "e.g. Electric Bill") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("bill_title_input")
                )

                // Monto de la factura
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = null
                    },
                    label = { Text(Localization.t("amount_label", lang)) },
                    placeholder = { Text("ej. 950.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("bill_amount_input")
                )

                // Fecha límite de pago con selector de calendario
                OutlinedTextField(
                    value = dueDateFormatted,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(Localization.t("bill_due_date", lang)) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar Fecha", tint = FinancePrimary)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker() }
                        .testTag("bill_due_date_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (title.isBlank()) {
                        errorMsg = Localization.t("error_bill_title_required", lang)
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMsg = Localization.t("error_bill_amount_invalid", lang)
                        return@Button
                    }

                    // Validación estricta: Solo permitir fechas límite superiores a la fecha actual del sistema
                    val todayEndOfDay = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    if (selectedDueDateMillis <= todayEndOfDay) {
                        errorMsg = Localization.t("error_bill_due_date_past", lang)
                        return@Button
                    }

                    // Verificar si la fecha está en los próximos 2 días de la fecha actual
                    val diffMillis = selectedDueDateMillis - System.currentTimeMillis()
                    val isWithinTwoDays = diffMillis <= (2 * 86400000L)

                    onConfirm(title, amount, selectedDueDateMillis, selectedCategory, isWithinTwoDays)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
            ) {
                Text(if (lang == AppLanguage.SPANISH) "Guardar Recordatorio" else "Save Reminder", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.t("cancel_btn", lang))
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Formulario que se despliega cuando el usuario marca una factura como pagada.
 * Permite seleccionar la cuenta o billetera de origen del pago, la categoría del gasto (existente),
 * y la descripción debe ser automática como "Pago de Factura".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayBillDialog(
    bill: BillReminderEntity,
    accounts: List<FinancialAccountEntity>,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmPayment: (originAccount: FinancialAccountEntity, expenseCategory: String) -> Unit
) {
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    // Categorías de gastos existentes en el sistema (sin permitir agregar nuevas)
    val existingExpenseCategories = remember(lang) {
        (CategoryDataHierarchy.fixedExpenseGroup.categories +
            CategoryDataHierarchy.variableExpenseGroup.categories +
            CategoryDataHierarchy.debtPaymentGroup.categories).map { cat ->
                if (lang == AppLanguage.SPANISH) cat.nameEs else cat.nameEn
            }
    }
    var selectedCategory by remember {
        mutableStateOf(
            existingExpenseCategories.firstOrNull { it.contains("Servicios", ignoreCase = true) || it.contains("Utilities", ignoreCase = true) }
                ?: existingExpenseCategories.firstOrNull()
                ?: "Servicios Básicos"
        )
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Descripción automática obligatoria solicitada por el usuario
    val automaticDescription = "Pago de Factura"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.t("bill_pay_dialog_title", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Localization.t("bill_pay_dialog_desc", lang),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                )

                // Resumen de la Factura
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = bill.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${Localization.t("bill_due_date", lang)}: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(bill.dueDateMillis))}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                            )
                        }
                        Text(
                            text = formatCordobas(bill.amount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinancePrimary
                            )
                        )
                    }
                }

                if (errorMsg != null) {
                    ErrorWarningBox(message = errorMsg!!)
                }

                // Descripción Automática (No modificable)
                OutlinedTextField(
                    value = automaticDescription,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(Localization.t("bill_pay_description_label", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("bill_pay_description_input")
                )

                // Selector de Cuenta de Origen
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val accountText = selectedAccount?.let {
                        "${it.bankName} - ${it.accountNumber} (${formatCordobas(it.balance)})"
                    } ?: (if (lang == AppLanguage.SPANISH) "Selecciona una cuenta" else "Select an account")

                    OutlinedTextField(
                        value = accountText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.t("bill_pay_origin_account", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("bill_pay_account_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${acc.bankName} (${acc.toolType} - ${acc.accountType})",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "No: ${acc.accountNumber} • Saldo: ${formatCordobas(acc.balance)}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                        )
                                    }
                                },
                                onClick = {
                                    selectedAccount = acc
                                    accountDropdownExpanded = false
                                    errorMsg = null
                                }
                            )
                        }
                    }
                }

                // Selector de Categoría del Gasto (Categorías existentes únicamente)
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.t("bill_pay_category_label", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("bill_pay_category_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        existingExpenseCategories.forEach { catName ->
                            DropdownMenuItem(
                                text = { Text(catName) },
                                onClick = {
                                    selectedCategory = catName
                                    categoryDropdownExpanded = false
                                    errorMsg = null
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAccount == null) {
                        errorMsg = Localization.t("error_bill_pay_account_required", lang)
                        return@Button
                    }
                    if (selectedAccount!!.balance < bill.amount) {
                        errorMsg = Localization.t("error_transfer_insufficient_funds", lang)
                        return@Button
                    }
                    onConfirmPayment(selectedAccount!!, selectedCategory)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinanceSuccess),
                modifier = Modifier.testTag("confirm_pay_bill_button")
            ) {
                Text(Localization.t("bill_pay_confirm_btn", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.t("cancel_btn", lang))
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
