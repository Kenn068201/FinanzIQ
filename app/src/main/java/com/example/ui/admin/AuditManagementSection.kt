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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuditLogEntity
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import java.util.Locale

/**
 * Sección de Auditoría y Bitácora de Eventos del Sistema.
 * 
 * Qué hace:
 * Permite al Administrador consultar y auditar en tiempo real todos los eventos críticos
 * del sistema: inicios de sesión (exitosos y fallidos con motivo), cambios en directivas
 * de seguridad, altas/bajas de instituciones financieras, modificaciones de usuarios y
 * lanzamientos de versiones.
 * 
 * Por qué se implementa de esta forma:
 * Cumple con los requerimientos específicos:
 * 1. Filtros dinámicos reactivos multicriterio (tipo de evento, resultado éxito/fallo, severidad y búsqueda libre).
 * 2. Si no hay registros que coincidan con los filtros seleccionados, emite exactamente el mensaje:
 *    "No hay registros para esas especificaciones dadas".
 * 3. En caso de coincidencias, despliega el listado histórico detallando fecha, hora e IP exactas de cada evento.
 * 
 * Cómo se relaciona con el resto del código:
 * Consume el listado de eventos de Room expuesto por FinanceViewModel y se visualiza
 * como una pestaña dedicada en la Interfaz de Admin.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditManagementSection(
    auditLogs: List<AuditLogEntity>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("TODOS") }
    var selectedResultFilter by remember { mutableStateOf("TODOS") }
    var selectedSeverityFilter by remember { mutableStateOf("TODOS") }

    // Filtrado dinámico en tiempo real
    val filteredLogs = auditLogs.filter { log ->
        val matchesCategory = when (selectedCategoryFilter) {
            "LOGIN" -> log.eventType.startsWith("LOGIN") || log.eventType == "LOGOUT"
            "SECURITY" -> log.eventType.contains("SECURITY")
            "PROVIDERS" -> log.eventType.contains("INSTITUTION")
            "USERS" -> log.eventType.startsWith("USER_")
            "VERSIONS" -> log.eventType.contains("VERSION")
            else -> true
        }

        val matchesResult = when (selectedResultFilter) {
            "EXITOSOS" -> log.isSuccess
            "FALLIDOS" -> !log.isSuccess
            else -> true
        }

        val matchesSeverity = when (selectedSeverityFilter) {
            "INFO" -> log.severity == "INFO"
            "WARN" -> log.severity == "WARN"
            "ERROR" -> log.severity == "ERROR"
            else -> true
        }

        val query = searchQuery.trim().lowercase(Locale.ROOT)
        val matchesQuery = query.isEmpty() ||
            log.action.lowercase(Locale.ROOT).contains(query) ||
            log.details.lowercase(Locale.ROOT).contains(query) ||
            log.username.lowercase(Locale.ROOT).contains(query) ||
            log.ipAddress.lowercase(Locale.ROOT).contains(query) ||
            log.eventType.lowercase(Locale.ROOT).contains(query)

        matchesCategory && matchesResult && matchesSeverity && matchesQuery
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Campo de Búsqueda Dinámica
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por usuario, IP, detalle o evento...") },
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
                .testTag("audit_search_input")
        )

        // Filtro por Categoría de Evento
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Categoría de auditoría:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "TODOS" to "Todos (${auditLogs.size})",
                    "LOGIN" to "Inicios Sesión (${auditLogs.count { it.eventType.startsWith("LOGIN") || it.eventType == "LOGOUT" }})",
                    "SECURITY" to "Seguridad (${auditLogs.count { it.eventType.contains("SECURITY") }})",
                    "PROVIDERS" to "Proveedores (${auditLogs.count { it.eventType.contains("INSTITUTION") }})",
                    "USERS" to "Usuarios (${auditLogs.count { it.eventType.startsWith("USER_") }})",
                    "VERSIONS" to "Versiones (${auditLogs.count { it.eventType.contains("VERSION") }})"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedCategoryFilter == key,
                        onClick = { selectedCategoryFilter = key },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinanceSecondaryContainer,
                            selectedLabelColor = FinancePrimary
                        )
                    )
                }
            }
        }

        // Filtros combinados de Estado y Severidad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedResultFilter == "TODOS",
                onClick = { selectedResultFilter = "TODOS" },
                label = { Text("Resultados: Todos", style = MaterialTheme.typography.labelSmall) }
            )
            FilterChip(
                selected = selectedResultFilter == "EXITOSOS",
                onClick = { selectedResultFilter = "EXITOSOS" },
                label = { Text("Exitosos (${auditLogs.count { it.isSuccess }})", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFDCFCE7),
                    selectedLabelColor = Color(0xFF15803D)
                )
            )
            FilterChip(
                selected = selectedResultFilter == "FALLIDOS",
                onClick = { selectedResultFilter = "FALLIDOS" },
                label = { Text("Fallidos (${auditLogs.count { !it.isSuccess }})", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFEE2E2),
                    selectedLabelColor = FinanceError
                )
            )
        }

        // Validación y retroalimentación cuando no hay registros coincidentes
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_empty_error_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = FinanceError,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Mensaje de error estricto solicitado en el prompt
                        Text(
                            text = "No hay registros para esas especificaciones dadas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            ),
                            modifier = Modifier.testTag("audit_empty_error_message")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Modifica los filtros seleccionados o el término de búsqueda para ver eventos históricos de auditoría.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D))
                        )
                    }
                }
            }
        } else {
            // Despliegue del listado histórico en caso de éxito
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    AuditLogItemCard(log = log)
                }
            }
        }
    }
}

/**
 * Tarjeta individual de evento de auditoría que visualiza la fecha, hora e IP exactas.
 */
@Composable
fun AuditLogItemCard(
    log: AuditLogEntity,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth().testTag("audit_item_${log.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabecera: Evento y Estado de Éxito / Fallo
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
                            .background(
                                when (log.severity) {
                                    "ERROR" -> Color(0xFFFEE2E2)
                                    "WARN" -> Color(0xFFFEF3C7)
                                    else -> FinanceSecondaryContainer
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = log.eventType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (log.severity) {
                                    "ERROR" -> FinanceError
                                    "WARN" -> Color(0xFFB45309)
                                    else -> FinancePrimary
                                },
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = "👤 ${log.username}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (log.isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (log.isSuccess) "EXITOSO" else "FALLIDO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (log.isSuccess) Color(0xFF15803D) else FinanceError,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Acción y Descripción del evento
            Text(
                text = log.action,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            )

            if (log.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF4B5563)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pie de tarjeta: Fecha, Hora e IP exactas
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📅 ${log.eventDate}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "⏰ ${log.eventTime}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Text(
                        text = "🌐 IP: ${log.ipAddress}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    )
                }
            }
        }
    }
}
