package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.FinanceViewModel
import com.example.ui.admin.AdminScreen
import com.example.ui.ai_assistant.AiAssistantScreen
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.bills.BillsScreen
import com.example.ui.budgets.BudgetsScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.savings.SavingsScreen
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.transactions.AddTransactionDialog
import com.example.ui.transactions.TransactionsScreen
import com.example.ui.wallets.ManageFinancialToolScreen
import com.example.ui.wallets.WalletsScreen

sealed class Screen(val route: String, val titleKey: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "nav_dashboard", Icons.Default.Home)
    object Transactions : Screen("transactions", "nav_transactions", Icons.Default.ReceiptLong)
    object Budgets : Screen("budgets", "nav_budgets", Icons.Default.PieChart)
    object Analytics : Screen("analytics", "nav_analytics", Icons.Default.BarChart)
    object Savings : Screen("savings", "nav_savings", Icons.Default.Savings)
    object Bills : Screen("bills", "nav_bills", Icons.Default.NotificationsActive)
    object Wallets : Screen("wallets", "nav_wallets", Icons.Default.AccountBalanceWallet)
    object AiAssistant : Screen("ai_assistant", "nav_ai", Icons.Default.AutoAwesome)
    object Admin : Screen("admin", "nav_admin", Icons.Default.AdminPanelSettings)
}

@Composable
fun MainAppNavigation(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val authNavController = rememberNavController()

    if (currentUser == null) {
        NavHost(
            navController = authNavController,
            startDestination = "login",
            modifier = modifier.fillMaxSize()
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { authNavController.navigate("register") },
                    onLoginSuccess = { /* state handled by viewModel */ }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateBack = { authNavController.popBackStack() },
                    onRegisterSuccess = { /* state handled by viewModel */ }
                )
            }
        }
    } else {
        AuthenticatedAppScaffold(
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedAppScaffold(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val financialAccounts by viewModel.financialAccounts.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(Screen.Dashboard.route) }
    var showUserMenu by remember { mutableStateOf(false) }

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var quickAddType by remember { mutableStateOf("EXPENSE") }

    val isAdmin = currentUser?.role == "ADMIN"

    // Se elimina la pestaña de billeteras de la barra de navegación conforme a la instrucción
    val navItems = buildList {
        add(Screen.Dashboard)
        add(Screen.Transactions)
        add(Screen.Budgets)
        add(Screen.Analytics)
        add(Screen.Savings)
        add(Screen.Bills)
        add(Screen.AiAssistant)
        if (isAdmin) {
            add(Screen.Admin)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FinanceSecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = FinancePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FinanzIQ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Text(
                                text = "${currentUser?.firstName ?: "Usuario"} • ${if (isAdmin) "🛡️ ${com.example.util.Localization.t("role_admin", lang)}" else "👤 ${com.example.util.Localization.t("role_user", lang)}"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showUserMenu = true },
                        modifier = Modifier.testTag("user_profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FinancePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.firstName?.take(1)?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${currentUser?.firstName} ${currentUser?.lastName}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentUser?.email ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { showUserMenu = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = com.example.util.Localization.t("logout", lang),
                                        tint = FinanceError
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(com.example.util.Localization.t("logout", lang), color = FinanceError, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                showUserMenu = false
                                viewModel.logout()
                            },
                            modifier = Modifier.testTag("logout_button")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val title = com.example.util.Localization.t(screen.titleKey, lang)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentRoute = screen.route
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FinancePrimary,
                            selectedTextColor = FinancePrimary,
                            indicatorColor = FinanceSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = {
                        currentRoute = Screen.Transactions.route
                        navController.navigate(Screen.Transactions.route)
                    },
                    onNavigateToBudgets = {
                        currentRoute = Screen.Budgets.route
                        navController.navigate(Screen.Budgets.route)
                    },
                    onNavigateToSavings = {
                        currentRoute = Screen.Savings.route
                        navController.navigate(Screen.Savings.route)
                    },
                    onNavigateToBills = {
                        currentRoute = Screen.Bills.route
                        navController.navigate(Screen.Bills.route)
                    },
                    onNavigateToAiAssistant = {
                        currentRoute = Screen.AiAssistant.route
                        navController.navigate(Screen.AiAssistant.route)
                    },
                    onNavigateToWallets = {
                        currentRoute = Screen.Wallets.route
                        navController.navigate(Screen.Wallets.route)
                    },
                    onOpenAddTransaction = { type ->
                        quickAddType = type
                        showQuickAddDialog = true
                    }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(viewModel = viewModel)
            }

            composable(Screen.Budgets.route) {
                BudgetsScreen(viewModel = viewModel)
            }

            composable(Screen.Wallets.route) {
                WalletsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToManageTool = { navController.navigate("manage_tool") }
                )
            }

            composable("manage_tool") {
                ManageFinancialToolScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }

            composable(Screen.Savings.route) {
                SavingsScreen(viewModel = viewModel)
            }

            composable(Screen.Bills.route) {
                BillsScreen(viewModel = viewModel)
            }

            composable(Screen.AiAssistant.route) {
                AiAssistantScreen(viewModel = viewModel)
            }

            if (isAdmin) {
                composable(Screen.Admin.route) {
                    AdminScreen(viewModel = viewModel)
                }
            }
        }

        if (showQuickAddDialog) {
            AddTransactionDialog(
                initialType = quickAddType,
                categories = categories.map { it.name }.distinct(),
                accounts = financialAccounts,
                currencySymbol = selectedCurrency.symbol,
                lang = lang,
                onAutoClassify = { title, type -> viewModel.autoSuggestCategory(title, type) },
                onDismiss = { showQuickAddDialog = false },
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
                    showQuickAddDialog = false
                }
            )
        }
    }
}
