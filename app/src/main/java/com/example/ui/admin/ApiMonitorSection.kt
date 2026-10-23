package com.example.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ApiStatusItem
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess

/**
 * Sección de Monitor de APIs de la Interfaz de Admin.
 * 
 * Qué hace:
 * Ofrece un panel de supervisión de salud, latencia, tiempo de actividad (uptime)
 * y tasa de error de todos los servicios y APIs integrados en FinanzIQ.
 * 
 * Por qué se implementa de esta forma:
 * Responde textualmente a los 3 requerimientos del usuario:
 * 1. Botón para consultas de los servicios de API integrados para auditar latencia, tasa de errores y uso.
 * 2. Visualización semafórica en verde (óptimo), amarillo (degradado) o rojo (crítico),
 *    con latencia promedio, porcentaje de uptime y métricas de tráfico.
 * 3. Detección automática de tasa de error >= 15% en los últimos 15 minutos emitiendo
 *    la alerta visual exacta: "Fallo detectado en API externa".
 * 
 * Cómo se relaciona con el resto del código:
 * Se conecta con los métodos de diagnóstico y la lista reactiva de métricas del FinanceViewModel.
 */
@Composable
fun ApiMonitorSection(
    apiStatusList: List<ApiStatusItem>,
    apiErrorAlert: String?,
    isDiagnosing: Boolean,
    onQueryApis: () -> Unit,
    onSimulateFailure: () -> Unit,
    onResetMetrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Alerta visual destacada requerida ante fallo >= 15%
        AnimatedVisibility(
            visible = apiErrorAlert != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, FinanceError),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_external_failure_alert")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = FinanceError,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fallo detectado en API externa",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = apiErrorAlert ?: "Uno o más servicios superan el margen del 15% de error en los últimos 15 minutos.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D))
                        )
                    }
                }
            }
        }

        // Panel de Control con Botón de Consulta de APIs
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FinanceSecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Monitor de Rendimiento de APIs",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Latencia, tasa de errores y disponibilidad en vivo",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                }

                // Botón principal de consultas y botones de prueba/simulación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onQueryApis,
                        enabled = !isDiagnosing,
                        colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("btn_query_api_services")
                    ) {
                        if (isDiagnosing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consultando...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consultar APIs", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onSimulateFailure,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceError),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_simulate_api_failure")
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simular Error", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onResetMetrics,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_reset_api_metrics")
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Listado de APIs con su estado semafórico (Verde, Amarillo, Rojo)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(apiStatusList, key = { it.apiName }) { item ->
                ApiStatusCard(item = item)
            }
        }
    }
}

/**
 * Tarjeta individual para mostrar el semáforo y las métricas de una API específica.
 */
@Composable
fun ApiStatusCard(
    item: ApiStatusItem,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth().testTag("api_card_${item.apiName}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabecera: Nombre de la API y Semáforo de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.apiName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Indicador Semafórico (Verde, Amarillo o Rojo)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (item.statusColor) {
                                "GREEN" -> Color(0xFFDCFCE7)
                                "YELLOW" -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFEE2E2)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (item.statusColor) {
                                        "GREEN" -> FinanceSuccess
                                        "YELLOW" -> Color(0xFFD97706)
                                        else -> FinanceError
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (item.statusColor) {
                                "GREEN" -> "Óptimo (Verde)"
                                "YELLOW" -> "Degradado (Amarillo)"
                                else -> "Crítico (Rojo)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (item.statusColor) {
                                    "GREEN" -> Color(0xFF15803D)
                                    "YELLOW" -> Color(0xFFB45309)
                                    else -> FinanceError
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cuadrícula de Métricas Clave (Latencia, Uptime, Tasa de Errores, Peticiones)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "Latencia Promedio",
                    value = "${item.averageLatencyMs} ms",
                    containerColor = Color(0xFFF8FAFC),
                    valueColor = if (item.averageLatencyMs > 400) Color(0xFFD97706) else Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Tiempo Activo",
                    value = "${String.format(java.util.Locale.US, "%.1f", item.uptimePercentage)}%",
                    containerColor = Color(0xFFF8FAFC),
                    valueColor = if (item.uptimePercentage >= 99.0) Color(0xFF15803D) else Color(0xFFB45309),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "Tasa de Errores",
                    value = "${String.format(java.util.Locale.US, "%.1f", item.errorRatePercentage)}%",
                    containerColor = if (item.errorRatePercentage >= 15.0) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                    valueColor = if (item.errorRatePercentage >= 15.0) FinanceError else Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Uso de API",
                    value = "${item.totalRequests} llamadas",
                    containerColor = Color(0xFFF8FAFC),
                    valueColor = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Última auditoría: ${item.lastCheckFormatted}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
                if (item.recent15MinErrorRate > 0) {
                    Text(
                        text = "Últimos 15 min: ${String.format(java.util.Locale.US, "%.1f", item.recent15MinErrorRate)}% error",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.recent15MinErrorRate >= 15.0) FinanceError else Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            )
        }
    }
}
