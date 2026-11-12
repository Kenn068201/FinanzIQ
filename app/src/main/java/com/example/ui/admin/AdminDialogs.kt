package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FinancialAccountEntity
import com.example.data.local.FinancialInstitutionEntity
import com.example.data.local.UserEntity
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import java.util.Locale

/**
 * Diálogo modal para agregar una nueva Institución Financiera (Banco o Billetera) al catálogo de la aplicación.
 * 
 * Qué hace:
 * Solicita país de origen, nombre completo, abreviatura opcional y tipo de entidad.
 * Si es Billetera, despliega un menú desplegable con los bancos disponibles para asociar la compatibilidad
 * de vinculación con cuentas del mismo banco.
 * 
 * Por qué se implementa de esta forma:
 * Cumple estrictamente con el requerimiento de que toda acción de administración sea parametrizable y
 * valide exhaustivamente que los campos obligatorios no queden en blanco.
 * 
 * Cómo se relaciona con el resto:
 * Al confirmar, invoca el método correspondiente del ViewModel para registrar la entidad en la base de datos
 * y reflejarla automáticamente en la interfaz de usuario final.
 */
@Composable
fun AddFinancialInstitutionDialog(
    availableBanks: List<FinancialInstitutionEntity>,
    onDismiss: () -> Unit,
    onConfirm: (country: String, fullName: String, abbreviation: String, type: String, supportedBank: String) -> Unit
) {
    var country by remember { mutableStateOf("Nicaragua") }
    var fullName by remember { mutableStateOf("") }
    var abbreviation by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("BANCO") } // "BANCO" o "BILLETERA"

    var selectedBankBrand by remember {
        mutableStateOf(availableBanks.firstOrNull()?.abbreviation?.ifBlank { availableBanks.firstOrNull()?.fullName } ?: "BANPRO")
    }
    var showBankDropdown by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (selectedType == "BANCO") Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nueva Entidad Financiera",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    ErrorWarningBox(message = errorMsg!!)
                }

                // Selector de Tipo de Entidad: Banco o Billetera
                Text(
                    text = "Tipo de Entidad:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "BANCO",
                        onClick = { selectedType = "BANCO" },
                        label = { Text("Banco") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("dialog_chip_type_bank")
                    )

                    FilterChip(
                        selected = selectedType == "BILLETERA",
                        onClick = { selectedType = "BILLETERA" },
                        label = { Text("Billetera") },
                        leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("dialog_chip_type_wallet")
                    )
                }

                // País de Origen
                OutlinedTextField(
                    value = country,
                    onValueChange = {
                        country = it
                        errorMsg = null
                    },
                    label = { Text("País de Origen *") },
                    placeholder = { Text("ej. Nicaragua, Estados Unidos, Costa Rica") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_country")
                )

                // Nombre Completo de la Entidad
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMsg = null
                    },
                    label = { Text("Nombre Completo de la Entidad *") },
                    placeholder = { Text(if (selectedType == "BANCO") "ej. BAC Credomatic Nicaragua" else "ej. Kash BAC Nicaragua") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_fullname")
                )

                // Abreviatura (Opcional)
                OutlinedTextField(
                    value = abbreviation,
                    onValueChange = {
                        abbreviation = it
                        errorMsg = null
                    },
                    label = { Text("Abreviatura (Opcional)") },
                    placeholder = { Text("ej. BAC, BANPRO, BDF") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_abbreviation")
                )

                // Si es Billetera: desplegar menú con bancos disponibles para identificar a cuáles cuentas se puede vincular
                if (selectedType == "BILLETERA") {
                    Text(
                        text = "Banco compatible para vinculación de cuentas *",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = FinancePrimary)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedBankBrand,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Banco Asociado") },
                            trailingIcon = {
                                IconButton(onClick = { showBankDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Banco")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBankDropdown = true }
                                .testTag("dialog_wallet_bank_dropdown")
                        )
                        DropdownMenu(
                            expanded = showBankDropdown,
                            onDismissRequest = { showBankDropdown = false }
                        ) {
                            if (availableBanks.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay bancos registrados en el catálogo") },
                                    onClick = { showBankDropdown = false }
                                )
                            } else {
                                availableBanks.forEach { bank ->
                                    val brandLabel = bank.abbreviation.ifBlank { bank.fullName }
                                    DropdownMenuItem(
                                        text = { Text("${bank.fullName} ($brandLabel)") },
                                        onClick = {
                                            selectedBankBrand = brandLabel
                                            showBankDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (country.trim().isBlank()) {
                        errorMsg = "Por favor ingrese el país de origen de la entidad."
                        return@Button
                    }
                    if (fullName.trim().isBlank()) {
                        errorMsg = "Por favor ingrese el nombre completo de la entidad."
                        return@Button
                    }
                    if (selectedType == "BILLETERA" && selectedBankBrand.trim().isBlank()) {
                        errorMsg = "Debe asociar un banco compatible para la billetera."
                        return@Button
                    }
                    onConfirm(
                        country.trim(),
                        fullName.trim(),
                        abbreviation.trim(),
                        selectedType,
                        if (selectedType == "BILLETERA") selectedBankBrand.trim() else ""
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                modifier = Modifier.testTag("dialog_confirm_add_institution")
            ) {
                Text("Guardar Entidad", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Diálogo modal para solicitar obligatoriamente un motivo antes de realizar acciones críticas:
 * desactivar o reactivar entidad financiera, bloquear o desbloquear usuario, o eliminar definitivamente.
 * 
 * Qué hace:
 * Despliega un campo de texto para que el Administrador justifique la acción y valida que no quede vacío.
 * 
 * Por qué se implementa de esta forma:
 * Satisface la directriz del usuario: "si las desactiva debe ingresar un motivo, si el Campo queda vacío no permitir que lo desactive... mandar un mensaje de error indicando que no debe estar vacío".
 * 
 * Cómo se relaciona con el resto:
 * Retorna el motivo ingresado al flujo de confirmación en el ViewModel.
 */
@Composable
fun ActionReasonDialog(
    title: String,
    actionButtonText: String,
    actionButtonColor: Color = FinanceError,
    warningMessage: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (warningMessage != null) {
                    Text(
                        text = warningMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }

                if (errorMsg != null) {
                    ErrorWarningBox(message = errorMsg!!)
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        errorMsg = null
                    },
                    label = { Text("Motivo de la acción *") },
                    placeholder = { Text("Ingrese detalladamente la razón (Obligatorio)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = actionButtonColor,
                        focusedLabelColor = actionButtonColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.trim().isBlank()) {
                        errorMsg = "El motivo no puede estar vacío antes de realizar la acción."
                        return@Button
                    }
                    onConfirm(reason.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = actionButtonColor),
                modifier = Modifier.testTag("dialog_confirm_reason_button")
            ) {
                Text(actionButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Diálogo modal para que el Administrador revise exhaustivamente las cuentas bancarias
 * y billeteras digitales vinculadas al usuario seleccionado.
 * 
 * Qué hace:
 * Consulta y despliega la lista de cuentas con sus números, saldos, tipo, límite de crédito y estado.
 * 
 * Por qué se implementa de esta forma:
 * Da cumplimiento al requerimiento: "Permitirle ver el estado... revisar información de las cuentas".
 */
@Composable
fun UserAccountsReviewDialog(
    user: UserEntity,
    fetchAccounts: suspend (userId: Long) -> List<FinancialAccountEntity>,
    onDismiss: () -> Unit
) {
    var accounts by remember { mutableStateOf<List<FinancialAccountEntity>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user.id) {
        isLoading = true
        accounts = fetchAccounts(user.id)
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Cuentas de ${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "@${user.username.ifBlank { user.email }} • Tel: ${user.phone}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FinancePrimary)
                    }
                } else if (accounts.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Este usuario no tiene cuentas bancarias ni billeteras registradas.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(accounts!!) { acc ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (acc.toolType == "CUENTA") Icons.Default.AccountBalance else Icons.Default.PhoneIphone,
                                                contentDescription = null,
                                                tint = FinancePrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${acc.bankName} (${acc.toolType})",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (acc.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (acc.isActive) "Activa" else "Deshabilitada",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (acc.isActive) FinanceSuccess else FinanceError
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tipo: ${acc.accountType}${if (acc.debitSubType.isNotBlank()) " (${acc.debitSubType})" else ""}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4B5563))
                                    )
                                    Text(
                                        text = "Número / Tel: ${acc.accountNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                                    )

                                    if (acc.linkedBank.isNotBlank()) {
                                        Text(
                                            text = "Vinculada a banco: ${acc.linkedBank} (${acc.linkedAccountNumber})",
                                            style = MaterialTheme.typography.bodySmall.copy(color = FinancePrimary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Saldo: ${user.preferredCurrency.split(" ").firstOrNull() ?: "$"} ${String.format(Locale.US, "%,.2f", acc.balance)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        if (acc.creditLimit > 0.0) {
                                            Text(
                                                text = "Límite: ${String.format(Locale.US, "%,.2f", acc.creditLimit)}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
            ) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
