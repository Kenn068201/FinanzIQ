package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.FinancialInstitutionEntity
import com.example.data.local.SecurityPolicyEntity
import com.example.data.local.UserEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.util.Localization
import java.util.Locale

/**
 * Interfaz de Administración Principal de FinanzIQ.
 * 
 * Qué hace:
 * Proporciona cuatro áreas de control completas para el Administrador:
 * 1. Catálogo de Instituciones Financieras (Bancos y Billeteras):
 *    Permite dar de alta entidades con país, nombre, abreviatura y banco compatible si es billetera;
 *    desactivar con motivo obligatorio (deshabilita vinculación a usuarios finales sin afectar cuentas previas);
 *    reactivar en cualquier momento o eliminar definitivamente con motivo.
 * 2. Panel de Usuarios Activos:
 *    Muestra información completa (nombres, apellidos, nombre de usuario, correo, contraseña revelable, teléfono);
 *    filtros de búsqueda multicampo; revisión de cuentas bancarias y billeteras del usuario;
 *    suspensión, reactivación y eliminación con motivo obligatorio, impidiendo la auto-suspensión o auto-eliminación.
 * 3. Directivas de Seguridad:
 *    Ajuste de intentos de login, tiempo de bloqueo, directivas de contraseña y edad;
 *    valida parámetros no negativos y actualiza globalmente el sistema.
 * 4. Gestión de Categorías del Sistema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val institutions by viewModel.financialInstitutions.collectAsState()
    val securityPolicy by viewModel.securityPolicy.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Estados de Auditoría, Monitor de APIs y Versiones
    val auditLogs by viewModel.auditLogs.collectAsState()
    val apiStatusList by viewModel.apiStatusList.collectAsState()
    val apiErrorAlert by viewModel.apiErrorAlert.collectAsState()
    val isApiDiagnosing by viewModel.isApiDiagnosing.collectAsState()
    val appVersions by viewModel.appVersions.collectAsState()
    val latestAppVersion by viewModel.latestAppVersion.collectAsState()
    val clientAppVersion by viewModel.clientAppVersion.collectAsState()

    // 0: Entidades, 1: Usuarios, 2: Auditoría, 3: Monitor APIs, 4: Versiones, 5: Seguridad, 6: Categorías
    var selectedTab by remember { mutableIntStateOf(0) }

    // Control de diálogos de Instituciones
    var showAddInstitutionDialog by remember { mutableStateOf(false) }
    var institutionToDeactivate by remember { mutableStateOf<FinancialInstitutionEntity?>(null) }
    var institutionToReactivate by remember { mutableStateOf<FinancialInstitutionEntity?>(null) }
    var institutionToDelete by remember { mutableStateOf<FinancialInstitutionEntity?>(null) }

    // Control de diálogos de Usuarios
    var userToToggleBlock by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var userToInspectAccounts by remember { mutableStateOf<UserEntity?>(null) }

    // Control de diálogo de Categorías
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Banner de retroalimentación de operaciones
    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = FinancePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Localization.t("admin_title", lang),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinanceBackground)
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = { showAddInstitutionDialog = true },
                        containerColor = FinancePrimary,
                        contentColor = FinanceOnPrimary,
                        modifier = Modifier.testTag("fab_admin_add_institution")
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Nueva Entidad")
                    }
                }
                6 -> {
                    FloatingActionButton(
                        onClick = { showAddCategoryDialog = true },
                        containerColor = FinancePrimary,
                        contentColor = FinanceOnPrimary,
                        modifier = Modifier.testTag("fab_admin_add_category")
                    ) {
                        Icon(Icons.Default.Category, contentDescription = "Nueva Categoría")
                    }
                }
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
            // Selector de Pestañas internacionalizado según el idioma seleccionado
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = FinanceBackground,
                contentColor = FinancePrimary,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${Localization.t("admin_tab_institutions", lang)} (${institutions.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${Localization.t("admin_tab_users", lang)} (${users.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("${Localization.t("admin_tab_audit", lang)} (${auditLogs.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(Localization.t("admin_tab_api_monitor", lang), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("${Localization.t("admin_tab_versions", lang)} (${appVersions.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text(Localization.t("admin_tab_security", lang), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    text = { Text("${Localization.t("admin_tab_categories", lang)} (${categories.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Banner de respuesta o error global
            feedbackMessage?.let { (isSuccess, msg) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSuccess) FinanceSuccess else FinanceError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (isSuccess) Color(0xFF166534) else Color(0xFF991B1B)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { feedbackMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                        }
                    }
                }
            }

            // Despliegue de la interfaz seleccionada
            when (selectedTab) {
                0 -> {
                    InstitutionsManagementSection(
                        institutions = institutions,
                        onDeactivate = { institutionToDeactivate = it },
                        onReactivate = { institutionToReactivate = it },
                        onDelete = { institutionToDelete = it }
                    )
                }
                1 -> {
                    UsersManagementSection(
                        users = users,
                        currentUser = currentUser,
                        onToggleBlock = { user ->
                            if (currentUser?.id == user.id) {
                                feedbackMessage = Pair(false, "No puede auto - suspender o eliminar su cuenta activa de administración")
                            } else {
                                userToToggleBlock = user
                            }
                        },
                        onDeleteUser = { user ->
                            if (currentUser?.id == user.id) {
                                feedbackMessage = Pair(false, "No puede auto - suspender o eliminar su cuenta activa de administración")
                            } else {
                                userToDelete = user
                            }
                        },
                        onInspectAccounts = { user ->
                            userToInspectAccounts = user
                        },
                        onUpdateRole = { user, newRole ->
                            viewModel.updateUserRole(user, newRole)
                            feedbackMessage = Pair(true, "Rol de ${user.firstName} cambiado a $newRole")
                        }
                    )
                }
                2 -> {
                    AuditManagementSection(
                        auditLogs = auditLogs
                    )
                }
                3 -> {
                    ApiMonitorSection(
                        apiStatusList = apiStatusList,
                        apiErrorAlert = apiErrorAlert,
                        isDiagnosing = isApiDiagnosing,
                        onQueryApis = { viewModel.runApiDiagnostics() },
                        onSimulateFailure = { viewModel.simulateApiFailure() },
                        onResetMetrics = { viewModel.resetApiMetrics() }
                    )
                }
                4 -> {
                    VersionControlSection(
                        versions = appVersions,
                        latestVersion = latestAppVersion,
                        clientAppVersion = clientAppVersion,
                        onPublishVersion = { versionNumber, releaseNotes, isMandatory, callback ->
                            viewModel.publishAppVersion(versionNumber, releaseNotes, isMandatory, callback)
                        }
                    )
                }
                5 -> {
                    SecurityDirectivesSection(
                        currentPolicy = securityPolicy,
                        onSavePolicy = { updatedPolicy ->
                            viewModel.updateSecurityPolicy(updatedPolicy) { success, errorMsg ->
                                if (success) {
                                    feedbackMessage = Pair(true, "Directivas de seguridad actualizadas y aplicadas globalmente con éxito.")
                                } else {
                                    feedbackMessage = Pair(false, errorMsg ?: "Error al actualizar las directivas de seguridad.")
                                }
                            }
                        }
                    )
                }
                6 -> {
                    CategoriesManagementSection(
                        categories = categories,
                        onDeleteCategory = { catId ->
                            viewModel.deleteCategory(catId)
                            feedbackMessage = Pair(true, "Categoría eliminada con éxito.")
                        }
                    )
                }
            }
        }

        // --- DIÁLOGOS DE INSTITUCIONES FINANCIERAS ---

        if (showAddInstitutionDialog) {
            AddFinancialInstitutionDialog(
                availableBanks = institutions.filter { it.type == "BANCO" && it.isActive },
                onDismiss = { showAddInstitutionDialog = false },
                onConfirm = { country, fullName, abbreviation, type, supportedBank ->
                    viewModel.addFinancialInstitution(
                        country = country,
                        fullName = fullName,
                        abbreviation = abbreviation,
                        type = type,
                        supportedBankBrands = supportedBank
                    ) { success, error ->
                        if (success) {
                            feedbackMessage = Pair(true, "Entidad '$fullName' incorporada exitosamente al catálogo.")
                            showAddInstitutionDialog = false
                        } else {
                            feedbackMessage = Pair(false, error ?: "Error al registrar la entidad.")
                        }
                    }
                }
            )
        }

        institutionToDeactivate?.let { inst ->
            ActionReasonDialog(
                title = "Desactivar ${inst.fullName}",
                actionButtonText = "Desactivar Entidad",
                actionButtonColor = Color(0xFFD97706),
                warningMessage = "Al desactivar esta entidad, los usuarios finales no podrán vincular nuevas cuentas a ella, pero mantendrán activas las cuentas ya vinculadas.",
                onDismiss = { institutionToDeactivate = null },
                onConfirm = { reason ->
                    viewModel.deactivateFinancialInstitution(inst, reason) { success, error ->
                        if (success) {
                            feedbackMessage = Pair(true, "Entidad '${inst.fullName}' desactivada. Nuevas vinculaciones deshabilitadas.")
                            institutionToDeactivate = null
                        } else {
                            feedbackMessage = Pair(false, error ?: "El motivo no puede estar vacío.")
                        }
                    }
                }
            )
        }

        institutionToReactivate?.let { inst ->
            ActionReasonDialog(
                title = "Reactivar ${inst.fullName}",
                actionButtonText = "Reactivar Entidad",
                actionButtonColor = FinanceSuccess,
                warningMessage = "La entidad volverá a estar habilitada para que los usuarios puedan vincular nuevas cuentas.",
                onDismiss = { institutionToReactivate = null },
                onConfirm = { reason ->
                    viewModel.reactivateFinancialInstitution(inst, reason) { success, _ ->
                        if (success) {
                            feedbackMessage = Pair(true, "Entidad '${inst.fullName}' reactivada exitosamente.")
                            institutionToReactivate = null
                        }
                    }
                }
            )
        }

        institutionToDelete?.let { inst ->
            ActionReasonDialog(
                title = "Eliminar ${inst.fullName}",
                actionButtonText = "Eliminar Definitivamente",
                actionButtonColor = FinanceError,
                warningMessage = "Esta acción eliminará la entidad por completo del catálogo del sistema.",
                onDismiss = { institutionToDelete = null },
                onConfirm = { reason ->
                    viewModel.deleteFinancialInstitution(inst, reason) { success, error ->
                        if (success) {
                            feedbackMessage = Pair(true, "Entidad '${inst.fullName}' eliminada definitivamente.")
                            institutionToDelete = null
                        } else {
                            feedbackMessage = Pair(false, error ?: "El motivo no puede estar vacío.")
                        }
                    }
                }
            )
        }

        // --- DIÁLOGOS DE USUARIOS ---

        userToToggleBlock?.let { targetUser ->
            val isCurrentlyBlocked = targetUser.isBlocked
            val actionLabel = if (isCurrentlyBlocked) "Reactivar Usuario" else "Suspender Usuario"
            ActionReasonDialog(
                title = "$actionLabel (${targetUser.firstName} ${targetUser.lastName})",
                actionButtonText = actionLabel,
                actionButtonColor = if (isCurrentlyBlocked) FinanceSuccess else FinanceError,
                warningMessage = if (isCurrentlyBlocked)
                    "El usuario podrá volver a ingresar a la aplicación con sus credenciales habituales."
                else
                    "El usuario no podrá iniciar sesión en la aplicación mientras su cuenta se encuentre suspendida.",
                onDismiss = { userToToggleBlock = null },
                onConfirm = { reason ->
                    viewModel.toggleUserBlockWithReason(targetUser, reason) { success, error ->
                        if (success) {
                            feedbackMessage = Pair(true, "Estado de '${targetUser.firstName}' actualizado exitosamente.")
                            userToToggleBlock = null
                        } else {
                            feedbackMessage = Pair(false, error ?: "El motivo no debe estar vacío antes de realizar la acción")
                        }
                    }
                }
            )
        }

        userToDelete?.let { targetUser ->
            ActionReasonDialog(
                title = "Eliminar Usuario Definitivamente",
                actionButtonText = "Eliminar Usuario",
                actionButtonColor = FinanceError,
                warningMessage = "Esta acción eliminará de forma irreversible al usuario '${targetUser.firstName} ${targetUser.lastName}'.",
                onDismiss = { userToDelete = null },
                onConfirm = { reason ->
                    viewModel.deleteUserWithReason(targetUser, reason) { success, error ->
                        if (success) {
                            feedbackMessage = Pair(true, "Usuario eliminado permanentemente.")
                            userToDelete = null
                        } else {
                            feedbackMessage = Pair(false, error ?: "El motivo no debe estar vacío antes de realizar la acción")
                        }
                    }
                }
            )
        }

        userToInspectAccounts?.let { targetUser ->
            UserAccountsReviewDialog(
                user = targetUser,
                fetchAccounts = { userId -> viewModel.getUserAccountsList(userId) },
                onDismiss = { userToInspectAccounts = null }
            )
        }

        if (showAddCategoryDialog) {
            AddCategoryDialog(
                lang = lang,
                onDismiss = { showAddCategoryDialog = false },
                onConfirm = { name, type ->
                    viewModel.addCategory(name, type)
                    feedbackMessage = Pair(true, "Categoría '$name' creada con éxito.")
                    showAddCategoryDialog = false
                }
            )
        }
    }
}

/**
 * Sección de Catálogo de Instituciones Financieras.
 * Permite filtrar, revisar el estado, desactivar con motivo, reactivar o eliminar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstitutionsManagementSection(
    institutions: List<FinancialInstitutionEntity>,
    onDeactivate: (FinancialInstitutionEntity) -> Unit,
    onReactivate: (FinancialInstitutionEntity) -> Unit,
    onDelete: (FinancialInstitutionEntity) -> Unit
) {
    var filterType by remember { mutableStateOf("TODAS") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = institutions.filter { inst ->
        val matchesType = when (filterType) {
            "BANCO" -> inst.type == "BANCO"
            "BILLETERA" -> inst.type == "BILLETERA"
            "ACTIVAS" -> inst.isActive
            "DESHABILITADAS" -> !inst.isActive
            else -> true
        }
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        val matchesSearch = query.isEmpty() ||
            inst.fullName.lowercase(Locale.ROOT).contains(query) ||
            inst.abbreviation.lowercase(Locale.ROOT).contains(query) ||
            inst.country.lowercase(Locale.ROOT).contains(query)
        matchesType && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar entidad por nombre, siglas o país...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FinancePrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_institutions_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "TODAS" to "Todas (${institutions.size})",
                "BANCO" to "Bancos (${institutions.count { it.type == "BANCO" }})",
                "BILLETERA" to "Billeteras (${institutions.count { it.type == "BILLETERA" }})",
                "ACTIVAS" to "Activas (${institutions.count { it.isActive }})",
                "DESHABILITADAS" to "Deshabilitadas (${institutions.count { !it.isActive }})"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = filterType == key,
                    onClick = { filterType = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinanceSecondaryContainer,
                        selectedLabelColor = FinancePrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No se encontraron instituciones en el catálogo",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredList, key = { it.id }) { inst ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().testTag("institution_item_${inst.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (inst.type == "BANCO") Color(0xFFEFF6FF) else Color(0xFFF3E8FF))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = inst.type,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (inst.type == "BANCO") Color(0xFF1D4ED8) else Color(0xFF7E22CE)
                                            )
                                        )
                                    }

                                    Text(
                                        text = "📍 ${inst.country}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF4B5563),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (inst.isActive) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (inst.isActive) "ACTIVA" else "DESHABILITADA",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (inst.isActive) Color(0xFF15803D) else Color(0xFFB45309)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${inst.fullName}${if (inst.abbreviation.isNotBlank()) " (${inst.abbreviation})" else ""}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            )

                            if (inst.type == "BILLETERA") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔗 Banco compatible para vinculación: ${inst.supportedBankBrands.ifBlank { "Mismo Banco" }}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF4B5563),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            if (!inst.isActive && inst.statusReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Motivo de desactivación: ${inst.statusReason}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF92400E),
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (inst.isActive) {
                                    OutlinedButton(
                                        onClick = { onDeactivate(inst) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Desactivar", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { onReactivate(inst) },
                                        colors = ButtonDefaults.buttonColors(containerColor = FinanceSuccess),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reactivar", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onDelete(inst) },
                                    modifier = Modifier.testTag("btn_delete_institution_${inst.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar institución",
                                        tint = FinanceError
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sección de Panel de Usuarios Activos e Información Detallada.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsersManagementSection(
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onToggleBlock: (UserEntity) -> Unit,
    onDeleteUser: (UserEntity) -> Unit,
    onInspectAccounts: (UserEntity) -> Unit,
    onUpdateRole: (UserEntity, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchField by remember { mutableStateOf("TODOS") }
    var statusFilter by remember { mutableStateOf("TODOS") }
    var roleFilter by remember { mutableStateOf("TODOS") }

    var revealedPasswords by remember { mutableStateOf(setOf<Long>()) }

    val filteredUsers = users.filter { user ->
        val matchesStatus = when (statusFilter) {
            "ACTIVOS" -> !user.isBlocked
            "BLOQUEADOS" -> user.isBlocked
            else -> true
        }
        val matchesRole = when (roleFilter) {
            "ADMIN" -> user.role == "ADMIN"
            "USER" -> user.role == "USER"
            else -> true
        }
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        val matchesSearch = if (query.isEmpty()) {
            true
        } else {
            when (searchField) {
                "NOMBRE" -> "${user.firstName} ${user.lastName}".lowercase(Locale.ROOT).contains(query)
                "USUARIO" -> user.username.lowercase(Locale.ROOT).contains(query)
                "CORREO" -> user.email.lowercase(Locale.ROOT).contains(query)
                "TELEFONO" -> "${user.phonePrefix}${user.phone}".lowercase(Locale.ROOT).contains(query)
                else -> {
                    "${user.firstName} ${user.lastName}".lowercase(Locale.ROOT).contains(query) ||
                        user.username.lowercase(Locale.ROOT).contains(query) ||
                        user.email.lowercase(Locale.ROOT).contains(query) ||
                        "${user.phonePrefix}${user.phone}".lowercase(Locale.ROOT).contains(query)
                }
            }
        }

        matchesStatus && matchesRole && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FinancePrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("search_users_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Filtrar búsqueda por campo:",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "TODOS" to "Todos los campos",
                "NOMBRE" to "Nombres/Apellidos",
                "USUARIO" to "Nombre de Usuario",
                "CORREO" to "Correo Electrónico",
                "TELEFONO" to "Teléfono"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = searchField == key,
                    onClick = { searchField = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinanceSecondaryContainer,
                        selectedLabelColor = FinancePrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = statusFilter == "TODOS",
                onClick = { statusFilter = "TODOS" },
                label = { Text("Todos (${users.size})", style = MaterialTheme.typography.labelSmall) }
            )
            FilterChip(
                selected = statusFilter == "ACTIVOS",
                onClick = { statusFilter = "ACTIVOS" },
                label = { Text("Activos (${users.count { !it.isBlocked }})", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFDCFCE7),
                    selectedLabelColor = Color(0xFF15803D)
                )
            )
            FilterChip(
                selected = statusFilter == "BLOQUEADOS",
                onClick = { statusFilter = "BLOQUEADOS" },
                label = { Text("Suspendidos (${users.count { it.isBlocked }})", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFEE2E2),
                    selectedLabelColor = FinanceError
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron usuarios coincidentes",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    val isRevealed = revealedPasswords.contains(user.id)
                    val isMe = currentUser?.id == user.id

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().testTag("user_card_${user.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${user.firstName} ${user.lastName}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFE0E7FF))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "TÚ (ACTIVO)",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4338CA),
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "@${user.username.ifBlank { "sin_usuario" }}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = FinancePrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (user.role == "ADMIN") Color(0xFFEDE9FE) else FinanceSecondaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = user.role,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (user.role == "ADMIN") Color(0xFF6D28D9) else FinancePrimary
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (!user.isBlocked) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (!user.isBlocked) "ACTIVO" else "SUSPENDIDO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (!user.isBlocked) Color(0xFF15803D) else FinanceError
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "📧 Correo: ${user.email}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF374151))
                                )
                                Text(
                                    text = "📱 Teléfono: ${user.phonePrefix} ${user.phone}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF374151))
                                )
                                Text(
                                    text = "🎂 Edad: ${user.age} años • 💵 Moneda: ${user.preferredCurrency}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF374151))
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🔑 Contraseña: ",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF374151))
                                    )
                                    Text(
                                        text = if (isRevealed) user.passwordHash else "••••••••",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRevealed) Color(0xFF1F2937) else Color.Gray
                                        )
                                    )
                                    IconButton(
                                        onClick = {
                                            revealedPasswords = if (isRevealed) {
                                                revealedPasswords - user.id
                                            } else {
                                                revealedPasswords + user.id
                                            }
                                        },
                                        modifier = Modifier.size(24.dp).padding(start = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (isRevealed) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = FinancePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (user.isBlocked && user.statusReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = FinanceError,
                                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Motivo de suspensión: ${user.statusReason}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF991B1B),
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onInspectAccounts(user) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ver Cuentas", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            val nextRole = if (user.role == "ADMIN") "USER" else "ADMIN"
                                            onUpdateRole(user, nextRole)
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (user.role == "ADMIN") "Hacer Usuario" else "Hacer Admin",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = FinancePrimary
                                            )
                                        )
                                    }

                                    Button(
                                        onClick = { onToggleBlock(user) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (user.isBlocked) FinanceSuccess else Color(0xFFDC2626)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (user.isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (user.isBlocked) "Reactivar" else "Suspender",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteUser(user) },
                                        modifier = Modifier.size(32.dp).testTag("btn_delete_user_${user.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar usuario definitivamente",
                                            tint = FinanceError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sección de Directivas de Seguridad Globales de la Aplicación.
 */
@Composable
fun SecurityDirectivesSection(
    currentPolicy: SecurityPolicyEntity,
    onSavePolicy: (SecurityPolicyEntity) -> Unit
) {
    var maxLoginAttemptsStr by remember(currentPolicy) { mutableStateOf(currentPolicy.maxLoginAttempts.toString()) }
    var lockoutSecondsStr by remember(currentPolicy) { mutableStateOf(currentPolicy.lockoutDurationSeconds.toString()) }
    var minAgeStr by remember(currentPolicy) { mutableStateOf(currentPolicy.minAge.toString()) }
    var maxNameLengthStr by remember(currentPolicy) { mutableStateOf(currentPolicy.maxNameLength.toString()) }
    var passwordMinLengthStr by remember(currentPolicy) { mutableStateOf(currentPolicy.passwordMinLength.toString()) }
    var passwordMaxLengthStr by remember(currentPolicy) { mutableStateOf(currentPolicy.passwordMaxLength.toString()) }

    var requireUppercase by remember(currentPolicy) { mutableStateOf(currentPolicy.requireUppercase) }
    var requireLowercase by remember(currentPolicy) { mutableStateOf(currentPolicy.requireLowercase) }
    var requireNumber by remember(currentPolicy) { mutableStateOf(currentPolicy.requireNumber) }
    var requireSpecialChar by remember(currentPolicy) { mutableStateOf(currentPolicy.requireSpecialChar) }

    var localValidationError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = FinanceSecondaryContainer.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ajuste de directivas de seguridad globales. Cada cambio guardado se reflejará instantáneamente en el inicio de sesión y registro de todos los usuarios.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF1E3A8A), fontWeight = FontWeight.Medium)
                )
            }
        }

        if (localValidationError != null) {
            ErrorWarningBox(message = localValidationError!!)
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Seguridad de Inicio de Sesión",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = maxLoginAttemptsStr,
                    onValueChange = {
                        maxLoginAttemptsStr = it
                        localValidationError = null
                    },
                    label = { Text("Número máximo de intentos fallidos *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("policy_max_login_attempts")
                )

                OutlinedTextField(
                    value = lockoutSecondsStr,
                    onValueChange = {
                        lockoutSecondsStr = it
                        localValidationError = null
                    },
                    label = { Text("Tiempo de bloqueo tras agotar intentos (segundos) *") },
                    placeholder = { Text("ej. 300 para 5 minutos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("policy_lockout_seconds")
                )
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Requisitos de Registro",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = minAgeStr,
                    onValueChange = {
                        minAgeStr = it
                        localValidationError = null
                    },
                    label = { Text("Edad mínima requerida (años) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("policy_min_age")
                )

                OutlinedTextField(
                    value = maxNameLengthStr,
                    onValueChange = {
                        maxNameLengthStr = it
                        localValidationError = null
                    },
                    label = { Text("Longitud máxima de nombres y apellidos *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("policy_max_name_length")
                )
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Requisitos de Contraseña Válida",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = passwordMinLengthStr,
                        onValueChange = {
                            passwordMinLengthStr = it
                            localValidationError = null
                        },
                        label = { Text("Long. Mínima *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("policy_pwd_min_len")
                    )

                    OutlinedTextField(
                        value = passwordMaxLengthStr,
                        onValueChange = {
                            passwordMaxLengthStr = it
                            localValidationError = null
                        },
                        label = { Text("Long. Máxima *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("policy_pwd_max_len")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Requerir mayúscula (A-Z)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = requireUppercase, onCheckedChange = { requireUppercase = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Requerir minúscula (a-z)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = requireLowercase, onCheckedChange = { requireLowercase = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Requerir número (0-9)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = requireNumber, onCheckedChange = { requireNumber = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Requerir carácter especial (@#$%&*!?)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = requireSpecialChar, onCheckedChange = { requireSpecialChar = it })
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    maxLoginAttemptsStr = "3"
                    lockoutSecondsStr = "300"
                    minAgeStr = "18"
                    maxNameLengthStr = "40"
                    passwordMinLengthStr = "6"
                    passwordMaxLengthStr = "32"
                    requireUppercase = true
                    requireLowercase = true
                    requireNumber = true
                    requireSpecialChar = true
                    localValidationError = null
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restablecer")
            }

            Button(
                onClick = {
                    val maxAtt = maxLoginAttemptsStr.toIntOrNull()
                    val lockSecs = lockoutSecondsStr.toIntOrNull()
                    val minAge = minAgeStr.toIntOrNull()
                    val maxName = maxNameLengthStr.toIntOrNull()
                    val pwdMin = passwordMinLengthStr.toIntOrNull()
                    val pwdMax = passwordMaxLengthStr.toIntOrNull()

                    if (maxAtt == null || maxAtt <= 0) {
                        localValidationError = "El número de intentos de inicio de sesión no puede ser negativo ni 0."
                        return@Button
                    }
                    if (lockSecs == null || lockSecs <= 0) {
                        localValidationError = "El tiempo de bloqueo no puede ser negativo ni 0."
                        return@Button
                    }
                    if (minAge == null || minAge <= 0) {
                        localValidationError = "La edad mínima no puede ser negativa ni 0."
                        return@Button
                    }
                    if (maxName == null || maxName <= 0) {
                        localValidationError = "La longitud máxima de nombres no puede ser negativa ni 0."
                        return@Button
                    }
                    if (pwdMin == null || pwdMin < 4) {
                        localValidationError = "La longitud mínima de contraseña debe ser de al menos 4 caracteres."
                        return@Button
                    }
                    if (pwdMax == null || pwdMax < pwdMin) {
                        localValidationError = "La longitud máxima de contraseña no puede ser menor a la longitud mínima."
                        return@Button
                    }

                    localValidationError = null
                    val updated = currentPolicy.copy(
                        maxLoginAttempts = maxAtt,
                        lockoutDurationSeconds = lockSecs,
                        minAge = minAge,
                        maxNameLength = maxName,
                        passwordMinLength = pwdMin,
                        passwordMaxLength = pwdMax,
                        requireUppercase = requireUppercase,
                        requireLowercase = requireLowercase,
                        requireNumber = requireNumber,
                        requireSpecialChar = requireSpecialChar
                    )
                    onSavePolicy(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f).testTag("btn_save_security_policy")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar Directivas", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Sección de Categorías del Sistema.
 */
@Composable
fun CategoriesManagementSection(
    categories: List<CategoryEntity>,
    onDeleteCategory: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (cat.type == "INCOME") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (cat.type == "INCOME") "↑" else "↓",
                                color = if (cat.type == "INCOME") FinanceSuccess else FinanceError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            )
                            Text(
                                text = if (cat.type == "INCOME") "Tipo: Ingreso" else "Tipo: Gasto",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                            )
                        }
                    }

                    if (cat.isCustom) {
                        IconButton(onClick = { onDeleteCategory(cat.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = FinanceError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FinanceSecondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sistema",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinancePrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para Agregar una Nueva Categoría de Ingreso o Gasto.
 */
@Composable
fun AddCategoryDialog(
    lang: com.example.util.AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva Categoría",
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
                            selectedContainerColor = Color(0xFFDCFCE7),
                            selectedLabelColor = FinanceSuccess
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("Nombre de la categoría") },
                    placeholder = { Text("ej. Mascotas, Gimnasio") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "El nombre de la categoría no puede estar vacío."
                        return@Button
                    }
                    onConfirm(name.trim(), type)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
            ) {
                Text("Crear Categoría", fontWeight = FontWeight.Bold)
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
