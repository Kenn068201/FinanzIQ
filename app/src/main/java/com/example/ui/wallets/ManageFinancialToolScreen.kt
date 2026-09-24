package com.example.ui.wallets

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FinancialAccountEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.util.Localization

/**
 * Interfaz de Administración y Registro de Herramientas Financieras (Cuentas y Billeteras).
 * Oculta las notas de validación en la interfaz principal para no descompensar el espacio visual,
 * mostrando cualquier incumplimiento de validación exclusivamente en el recuadro rojo de errores.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFinancialToolScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val registeredAccounts by viewModel.financialAccounts.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()

    // Bancos reconocidos de Nicaragua
    val nicaraguaBanks = listOf(
        "BANPRO Grupo Promerica",
        "BAC Credomatic Nicaragua",
        "Banco LAFISE BANCENTRO",
        "Banco de Finanzas (BDF)",
        "Banco FICOHSA Nicaragua",
        "Banco AVANZ"
    )

    // Proveedores de billeteras digitales
    val walletProviders = listOf(
        "Billetera Móvil Banpro",
        "Billetera Móvil LAFISE",
        "Kash BAC Nicaragua",
        "Billetera Móvil BDF",
        "Billetera Móvil Ficohsa"
    )

    // Tipo de herramienta principal: "CUENTA" o "BILLETERA"
    var selectedToolType by remember { mutableStateOf("CUENTA") }

    // Campos para Cuenta Bancaria
    var selectedBank by remember { mutableStateOf(nicaraguaBanks.first()) }
    var showBankDropdown by remember { mutableStateOf(false) }

    var accountType by remember { mutableStateOf("DEBITO") } // "DEBITO" o "CREDITO"
    var debitSubType by remember { mutableStateOf("AHORRO") } // "AHORRO", "CORRIENTE", "NOMINA"

    var accountNumber by remember { mutableStateOf("") }
    var initialBalanceStr by remember { mutableStateOf("1000") }
    var creditLimitStr by remember { mutableStateOf("15000") }

    // Campos para Billetera Digital
    var selectedWalletProvider by remember { mutableStateOf(walletProviders.first()) }
    var showWalletDropdown by remember { mutableStateOf(false) }

    var associatedPhone by remember { mutableStateOf(currentUser?.phone ?: "") }

    // Cuentas bancarias de débito activas del usuario para vincular
    val eligibleBankAccounts = registeredAccounts.filter { it.toolType == "CUENTA" && it.isActive }
    var selectedLinkedAccount by remember { mutableStateOf(eligibleBankAccounts.firstOrNull()) }
    var showLinkedAccountDropdown by remember { mutableStateOf(false) }

    var walletInitialBalanceStr by remember { mutableStateOf("500") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.t("manage_tool_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("manage_tool_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Localization.t("back", lang),
                            tint = FinancePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recuadro rojo de advertencias y errores establecido para la aplicación
            if (errorMessage != null) {
                ErrorWarningBox(message = errorMessage!!)
            }

            // Selector del Tipo de Herramienta Financiera
            Text(
                text = Localization.t("tool_type_label", lang),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedToolType == "CUENTA",
                    onClick = {
                        selectedToolType = "CUENTA"
                        errorMessage = null
                    },
                    label = { Text(Localization.t("bank_account_chip", lang), fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinanceSecondaryContainer,
                        selectedLabelColor = FinancePrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_type_account_chip")
                )

                FilterChip(
                    selected = selectedToolType == "BILLETERA",
                    onClick = {
                        selectedToolType = "BILLETERA"
                        errorMessage = null
                    },
                    label = { Text(Localization.t("wallet_chip", lang), fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PhoneIphone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinanceSecondaryContainer,
                        selectedLabelColor = FinancePrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tool_type_wallet_chip")
                )
            }

            if (selectedToolType == "CUENTA") {
                // --- FORMULARIO DE CUENTA BANCARIA ---
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = Localization.t("bank_data_section", lang),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Selector de Banco
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedBank,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(Localization.t("bank_label", lang)) },
                                trailingIcon = {
                                    IconButton(onClick = { showBankDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Banco")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showBankDropdown = true }
                                    .testTag("bank_selector_input")
                            )
                            DropdownMenu(
                                expanded = showBankDropdown,
                                onDismissRequest = { showBankDropdown = false }
                            ) {
                                nicaraguaBanks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = { Text(bank) },
                                        onClick = {
                                            selectedBank = bank
                                            showBankDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Tipo de Cuenta: Débito o Crédito
                        Text(
                            text = Localization.t("account_type_label", lang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = accountType == "DEBITO",
                                onClick = { accountType = "DEBITO" },
                                label = { Text(Localization.t("account_type_debit", lang)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FinanceSecondaryContainer,
                                    selectedLabelColor = FinancePrimary
                                ),
                                modifier = Modifier.weight(1f).testTag("account_type_debit_chip")
                            )
                            FilterChip(
                                selected = accountType == "CREDITO",
                                onClick = { accountType = "CREDITO" },
                                label = { Text(Localization.t("account_type_credit", lang)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FinanceSecondaryContainer,
                                    selectedLabelColor = FinancePrimary
                                ),
                                modifier = Modifier.weight(1f).testTag("account_type_credit_chip")
                            )
                        }

                        // Subtipo para Cuentas de Débito (Ahorro, Corriente, Nómina) - Sin textos de validación invasivos
                        if (accountType == "DEBITO") {
                            Text(
                                text = Localization.t("debit_subtype_label", lang),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "AHORRO" to Localization.t("subtype_savings", lang),
                                    "CORRIENTE" to Localization.t("subtype_checking", lang),
                                    "NOMINA" to Localization.t("subtype_payroll", lang)
                                ).forEach { (code, label) ->
                                    FilterChip(
                                        selected = debitSubType == code,
                                        onClick = { debitSubType = code },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FinanceSecondaryContainer,
                                            selectedLabelColor = FinancePrimary
                                        ),
                                        modifier = Modifier.testTag("debit_subtype_${code.lowercase()}_chip")
                                    )
                                }
                            }
                        }

                        // Número de Cuenta
                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = {
                                accountNumber = it
                                errorMessage = null
                            },
                            label = { Text(Localization.t("account_number_label", lang)) },
                            placeholder = { Text("100234567890") },
                            leadingIcon = {
                                Icon(
                                    if (accountType == "CREDITO") Icons.Default.CreditCard else Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = FinancePrimary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("account_number_input")
                        )

                        // Límite de Crédito (Solo para Crédito)
                        if (accountType == "CREDITO") {
                            OutlinedTextField(
                                value = creditLimitStr,
                                onValueChange = {
                                    creditLimitStr = it
                                    errorMessage = null
                                },
                                label = { Text("${Localization.t("credit_limit_label", lang)} (${currency.symbol})") },
                                placeholder = { Text("15000.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("credit_limit_input")
                            )
                        }

                        // Monto Inicial / Saldo Disponible Inicial
                        OutlinedTextField(
                            value = initialBalanceStr,
                            onValueChange = {
                                initialBalanceStr = it
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    if (accountType == "CREDITO")
                                        "${Localization.t("available_credit_initial", lang)} (${currency.symbol})"
                                    else
                                        "${Localization.t("initial_opening_amount", lang)} (${currency.symbol})"
                                )
                            },
                            placeholder = { Text("1000.00") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("initial_balance_input")
                        )
                    }
                }
            } else {
                // --- FORMULARIO DE BILLETERA DIGITAL ---
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = Localization.t("wallet_data_section", lang),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Selector de Proveedor de Billetera
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedWalletProvider,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(Localization.t("wallet_provider_label", lang)) },
                                trailingIcon = {
                                    IconButton(onClick = { showWalletDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Billetera")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showWalletDropdown = true }
                                    .testTag("wallet_provider_input")
                            )
                            DropdownMenu(
                                expanded = showWalletDropdown,
                                onDismissRequest = { showWalletDropdown = false }
                            ) {
                                walletProviders.forEach { provider ->
                                    DropdownMenuItem(
                                        text = { Text(provider) },
                                        onClick = {
                                            selectedWalletProvider = provider
                                            showWalletDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Número telefónico asociado
                        OutlinedTextField(
                            value = associatedPhone,
                            onValueChange = {
                                associatedPhone = it
                                errorMessage = null
                            },
                            label = { Text(Localization.t("associated_phone_label", lang)) },
                            placeholder = { Text(currentUser?.phone ?: "88887777") },
                            leadingIcon = {
                                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = FinancePrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("associated_phone_input")
                        )

                        // Cuenta bancaria vinculada
                        Text(
                            text = Localization.t("linked_account_label", lang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedLinkedAccount?.let { "${it.bankName} - ${it.accountType} (${it.accountNumber})" }
                                    ?: Localization.t("select_linked_account_placeholder", lang),
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = FinancePrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showLinkedAccountDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Cuenta")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showLinkedAccountDropdown = true }
                                    .testTag("linked_account_selector")
                            )
                            DropdownMenu(
                                expanded = showLinkedAccountDropdown,
                                onDismissRequest = { showLinkedAccountDropdown = false }
                            ) {
                                eligibleBankAccounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.bankName} - ${acc.accountType} (${acc.accountNumber})") },
                                        onClick = {
                                            selectedLinkedAccount = acc
                                            showLinkedAccountDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Saldo Inicial de la Billetera
                        OutlinedTextField(
                            value = walletInitialBalanceStr,
                            onValueChange = {
                                walletInitialBalanceStr = it
                                errorMessage = null
                            },
                            label = { Text("${Localization.t("wallet_initial_balance_label", lang)} (${currency.symbol})") },
                            placeholder = { Text("500.00") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("wallet_initial_balance_input")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón de Registrar Herramienta
            Button(
                onClick = {
                    errorMessage = null
                    val initBal = if (selectedToolType == "CUENTA") {
                        initialBalanceStr.toDoubleOrNull() ?: -1.0
                    } else {
                        walletInitialBalanceStr.toDoubleOrNull() ?: -1.0
                    }

                    val credLim = creditLimitStr.toDoubleOrNull() ?: 0.0

                    viewModel.addFinancialTool(
                        toolType = selectedToolType,
                        bankName = if (selectedToolType == "CUENTA") selectedBank else selectedWalletProvider,
                        accountType = if (selectedToolType == "CUENTA") accountType else "BILLETERA",
                        debitSubType = if (selectedToolType == "CUENTA" && accountType == "DEBITO") debitSubType else "",
                        accountNumber = if (selectedToolType == "CUENTA") accountNumber else associatedPhone,
                        creditLimit = credLim,
                        initialBalance = initBal,
                        associatedPhone = associatedPhone,
                        linkedBank = selectedLinkedAccount?.bankName ?: "",
                        linkedAccountType = selectedLinkedAccount?.accountType ?: "",
                        linkedDebitSubType = selectedLinkedAccount?.debitSubType ?: "",
                        linkedAccountNumber = selectedLinkedAccount?.accountNumber ?: ""
                    ) { success, error ->
                        if (success) {
                            onNavigateBack()
                        } else {
                            errorMessage = error
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_financial_tool_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = FinanceOnPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.t("register_tool_btn", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FinanceOnPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
