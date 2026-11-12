package com.example.ui.wallets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FinancialAccountEntity
import com.example.ui.FinanceViewModel
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.ui.theme.FinanceSuccessContainer
import com.example.ui.transactions.TransferFundsDialog
import com.example.util.Localization
import java.util.Locale

/**
 * Interfaz de Billeteras y Cuentas Bancarias de FinanzIQ.
 * Muestra el listado vertical de herramientas financieras activas y deshabilitadas,
 * permitiendo deshabilitar o reactivar cuentas y realizar transferencias inmediatas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToManageTool: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val accounts by viewModel.financialAccounts.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Todas, 1: Cuentas, 2: Billeteras
    var accountToToggle by remember { mutableStateOf<FinancialAccountEntity?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }

    val filteredList = when (selectedTab) {
        1 -> accounts.filter { it.toolType == "CUENTA" }
        2 -> accounts.filter { it.toolType == "BILLETERA" }
        else -> accounts
    }

    val totalActiveBalance = accounts.filter { it.isActive }.sumOf { it.balance }
    val totalCreditLimit = accounts.filter { it.isActive && it.accountType == "CREDITO" }.sumOf { it.creditLimit }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.t("wallets_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("wallets_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Localization.t("back", lang),
                            tint = FinancePrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.testTag("wallets_transfer_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = Localization.t("transfer_btn", lang),
                            tint = FinancePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToManageTool,
                containerColor = FinancePrimary,
                contentColor = FinanceOnPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_financial_tool_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = Localization.t("manage_tool_title", lang),
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
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Encabezado consolidado con el saldo total disponible
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceSecondaryContainer.copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = Localization.t("consolidated_balance", lang),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currency.symbol} ${String.format(Locale.US, "%,.2f", totalActiveBalance)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (totalCreditLimit > 0.0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${Localization.t("credit_limit_label", lang)}: ${currency.symbol} ${String.format(Locale.US, "%,.2f", totalCreditLimit)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Pestañas de filtrado de herramientas
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = FinancePrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("${Localization.t("all_accounts", lang)} (${accounts.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("${Localization.t("accounts_tab", lang)} (${accounts.count { it.toolType == "CUENTA" }})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("${Localization.t("wallets_tab", lang)} (${accounts.count { it.toolType == "BILLETERA" }})", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Estado vacío si no hay cuentas registradas
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Localization.t("no_tools_registered", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Localization.t("no_tools_description", lang),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Listado de tarjetas de cuentas y billeteras
                items(filteredList, key = { it.id }) { item ->
                    FinancialAccountCard(
                        account = item,
                        currencySymbol = currency.symbol,
                        lang = lang,
                        onToggleActive = { accountToToggle = item }
                    )
                }
            }

            // Espaciado final para no tapar el botón flotante
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Diálogo de confirmación para deshabilitar o reactivar herramienta
        accountToToggle?.let { acc ->
            val isCurrentlyActive = acc.isActive
            AlertDialog(
                onDismissRequest = { accountToToggle = null },
                title = {
                    Text(
                        text = if (isCurrentlyActive) Localization.t("disable_account_title", lang) else Localization.t("reactivate_account_title", lang),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (isCurrentlyActive) {
                            "¿Deseas deshabilitar '${acc.bankName}' (${acc.accountNumber})? No se realizarán cargos activos sobre ella, pero su historial se mantendrá seguro."
                        } else {
                            "¿Deseas reactivar '${acc.bankName}' (${acc.accountNumber}) para su uso inmediato en la plataforma?"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleAccountActive(acc)
                            accountToToggle = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentlyActive) FinanceError else FinanceSuccess
                        )
                    ) {
                        Text(
                            text = if (isCurrentlyActive) Localization.t("disable_btn", lang) else Localization.t("reactivate_btn", lang),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { accountToToggle = null }) {
                        Text(Localization.t("cancel", lang))
                    }
                }
            )
        }

        // Diálogo de Transferencia entre Cuentas y Billeteras
        if (showTransferDialog) {
            TransferFundsDialog(
                viewModel = viewModel,
                onDismiss = { showTransferDialog = false },
                onTransferSuccess = {
                    showTransferDialog = false
                }
            )
        }
    }
}

/**
 * Componente de Tarjeta que representa cada Cuenta Bancaria o Billetera Digital.
 * Si la cuenta está deshabilitada, se muestra sombreada con un botón verde de 'Reactivar'.
 * Si está activa, muestra los detalles y un botón rojo 'X' para deshabilitar.
 */
@Composable
fun FinancialAccountCard(
    account: FinancialAccountEntity,
    currencySymbol: String,
    lang: com.example.util.AppLanguage,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWallet = account.toolType == "BILLETERA"
    val isCredit = account.accountType == "CREDITO"
    val isDebit = account.accountType == "DEBITO"
    val isActive = account.isActive

    // Estilo sombreado cuando está deshabilitada
    val cardBackground = if (isActive) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentAlpha = if (isActive) 1f else 0.6f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isActive) Modifier.border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                else Modifier
            )
            .testTag("account_card_${account.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila Superior: Icono de Banco/Billetera, Nombre de Banco y Botón de Estado (X / Reactivar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (!isActive) Color(0xFFE2E8F0)
                                else if (isWallet) Color(0xFFE0F2FE)
                                else if (isCredit) Color(0xFFFEF3C7)
                                else FinanceSecondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isWallet -> Icons.Default.PhoneIphone
                                isCredit -> Icons.Default.CreditCard
                                else -> Icons.Default.AccountBalance
                            },
                            contentDescription = null,
                            tint = when {
                                !isActive -> Color.Gray
                                isWallet -> Color(0xFF0284C7)
                                isCredit -> Color(0xFFD97706)
                                else -> FinancePrimary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = account.bankName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                            )
                        )
                        Text(
                            text = when {
                                isWallet -> Localization.t("wallet_chip", lang)
                                isCredit -> Localization.t("account_type_credit", lang)
                                isDebit -> "${Localization.t("account_type_debit", lang)} (${account.debitSubType.ifEmpty { "Ahorro" }})"
                                else -> account.accountType
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (!isActive) Color.Gray else FinancePrimary
                            )
                        )
                    }
                }

                // Botón de Deshabilitar (Rojo con X) o Reactivar (Verde)
                if (isActive) {
                    IconButton(
                        onClick = onToggleActive,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FinanceError.copy(alpha = 0.1f))
                            .testTag("disable_account_btn_${account.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.t("disable_account_title", lang),
                            tint = FinanceError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onToggleActive,
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceSuccess),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("reactivate_account_btn_${account.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Localization.t("reactivate_btn", lang),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila Central: Número de cuenta / Teléfono y Saldo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (isWallet) Localization.t("associated_phone_label", lang) else Localization.t("account_number_label", lang),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = if (isWallet) "📞 ${account.associatedPhone.ifEmpty { account.accountNumber }}" else "💳 ${account.accountNumber}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isCredit) Localization.t("available_credit_initial", lang) else Localization.t("available_balance", lang),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "$currencySymbol ${String.format(Locale.US, "%,.2f", account.balance)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!isActive) Color.Gray else if (account.balance >= 0) FinanceSuccess else FinanceError
                        )
                    )
                }
            }

            // Si es Crédito: Especificar el Límite de Crédito
            if (isCredit && account.creditLimit > 0.0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7).copy(alpha = if (isActive) 0.8f else 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Localization.t("credit_limit_label", lang)}:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                    )
                    Text(
                        text = "$currencySymbol ${String.format(Locale.US, "%,.2f", account.creditLimit)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    )
                }
            }

            // Si es Billetera: Especificar la Cuenta Bancaria Vinculada
            if (isWallet && account.linkedAccountNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9).copy(alpha = if (isActive) 1f else 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = FinancePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${Localization.t("linked_account_label", lang)}: ${account.linkedBank} • ${account.linkedAccountType} (${account.linkedAccountNumber})",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Indicador de Estado Deshabilitado
            if (!isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ ${Localization.t("account_disabled_notice", lang)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = FinanceError
                    )
                )
            }
        }
    }
}
