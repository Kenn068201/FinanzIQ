package com.example.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FinancialAccountEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSuccess
import com.example.util.Localization
import java.util.Locale

/**
 * Cuadro de diálogo interactivo para realizar transferencias de fondos entre cuentas y billeteras del usuario.
 * Aplica validaciones de titularidad, saldo suficiente, límites por tipo de herramienta (Billetera C$15,000 / Bancos C$200,000)
 * y cálculo dinámico de comisión interbancaria de C$ 80.00 cuando proceden de bancos distintos.
 */
@Composable
fun TransferFundsDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onTransferSuccess: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val accounts by viewModel.financialAccounts.collectAsState()

    // Solo permitir cuentas activas
    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }

    var selectedOrigin by remember { mutableStateOf<FinancialAccountEntity?>(activeAccounts.firstOrNull()) }
    var selectedDestination by remember {
        mutableStateOf<FinancialAccountEntity?>(
            if (activeAccounts.size > 1) activeAccounts[1] else null
        )
    }

    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var originDropdownExpanded by remember { mutableStateOf(false) }
    var destDropdownExpanded by remember { mutableStateOf(false) }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val isDifferentBank = remember(selectedOrigin, selectedDestination) {
        val o = selectedOrigin
        val d = selectedDestination
        if (o != null && d != null) {
            !o.bankName.equals(d.bankName, ignoreCase = true)
        } else false
    }
    val commission = if (isDifferentBank) 80.0 else 0.0
    val totalToDeduct = amountDouble + commission

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
                // Encabezado del diálogo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        tint = FinancePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = Localization.t("transfer_title", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.t("close", lang)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cuadro rojo de advertencias y errores establecido en la app
                if (errorMessage != null) {
                    ErrorWarningBox(message = errorMessage!!)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Selector de Cuenta de Origen
                Text(
                    text = Localization.t("transfer_origin_label", lang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .clickable { originDropdownExpanded = true }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedOrigin?.toolType == "BILLETERA") Icons.Default.AccountBalanceWallet else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = FinancePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedOrigin != null) {
                                        "${selectedOrigin!!.bankName} • ${selectedOrigin!!.accountType} (${selectedOrigin!!.accountNumber})"
                                    } else Localization.t("select_account_placeholder", lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (selectedOrigin != null) {
                                    Text(
                                        text = "${Localization.t("available_prefix", lang)} ${currency.symbol} ${String.format(Locale.US, "%,.2f", selectedOrigin!!.balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectedOrigin!!.balance > 0) FinanceSuccess else FinanceError
                                    )
                                }
                            }
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = originDropdownExpanded,
                        onDismissRequest = { originDropdownExpanded = false }
                    ) {
                        activeAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${acc.bankName} - ${acc.accountType} (${acc.accountNumber})", fontWeight = FontWeight.SemiBold)
                                        Text("${currency.symbol} ${String.format(Locale.US, "%,.2f", acc.balance)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedOrigin = acc
                                    originDropdownExpanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Cuenta de Destino
                Text(
                    text = Localization.t("transfer_dest_label", lang),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .clickable { destDropdownExpanded = true }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedDestination?.toolType == "BILLETERA") Icons.Default.AccountBalanceWallet else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedDestination != null) {
                                        "${selectedDestination!!.bankName} • ${selectedDestination!!.accountType} (${selectedDestination!!.accountNumber})"
                                    } else Localization.t("select_account_placeholder", lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (selectedDestination != null) {
                                    Text(
                                        text = "${Localization.t("balance_prefix", lang)} ${currency.symbol} ${String.format(Locale.US, "%,.2f", selectedDestination!!.balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = destDropdownExpanded,
                        onDismissRequest = { destDropdownExpanded = false }
                    ) {
                        activeAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${acc.bankName} - ${acc.accountType} (${acc.accountNumber})", fontWeight = FontWeight.SemiBold)
                                        Text("${currency.symbol} ${String.format(Locale.US, "%,.2f", acc.balance)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedDestination = acc
                                    destDropdownExpanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo de Monto a Transferir
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() || char == '.' }
                        errorMessage = null
                    },
                    label = { Text(Localization.t("transfer_amount_label", lang, currency.symbol)) },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cartel informativo de Comisión Interbancaria (C$ 80) o Mismo Banco (C$ 0)
                if (selectedOrigin != null && selectedDestination != null) {
                    if (isDifferentBank) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFBEB), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Localization.t(
                                        "transfer_commission_notice",
                                        lang,
                                        "${currency.symbol} ${String.format(Locale.US, "%,.2f", totalToDeduct)}"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = FinanceSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Localization.t("transfer_same_bank_notice", lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botones de Acción
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
                            val origin = selectedOrigin
                            val dest = selectedDestination

                            if (origin == null || dest == null) {
                                errorMessage = Localization.t("error_deposit_source_required", lang)
                                return@Button
                            }

                            if (amountDouble <= 0.0) {
                                errorMessage = Localization.t("error_transfer_invalid_amount", lang)
                                return@Button
                            }

                            viewModel.transferFunds(
                                originAccount = origin,
                                destinationAccount = dest,
                                amount = amountDouble
                            ) { success, err ->
                                if (success) {
                                    onTransferSuccess()
                                } else {
                                    errorMessage = err
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_transfer_button")
                    ) {
                        Text(Localization.t("transfer_btn", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
