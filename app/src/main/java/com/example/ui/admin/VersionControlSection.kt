package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppVersionEntity
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.ui.theme.FinanceSuccessContainer

/**
 * Sección de Control de Versiones de la Aplicación.
 * 
 * Qué hace:
 * Permite al Administrador registrar nuevos lanzamientos de versiones con su número
 * y notas de cambio, definiendo si la actualización es obligatoria u opcional para el usuario final.
 * 
 * Por qué se implementa de esta forma:
 * Satisface los requerimientos:
 * 1. Validación estricta: tanto el número de versión como las notas de cambio no pueden ser nulos ni vacíos.
 * 2. Selección de obligatoriedad: si es obligatoria, se sincroniza con el diálogo de la app para
 *    bloquear versiones obsoletas; si es opcional, permite al usuario posponer la actualización.
 * 3. Bitácora histórica de versiones publicadas.
 * 
 * Cómo se relaciona con el resto del código:
 * Llama a viewModel.publishAppVersion(...) que persiste la entidad en Room y registra
 * la auditoría del cambio en el sistema.
 */
@Composable
fun VersionControlSection(
    versions: List<AppVersionEntity>,
    latestVersion: AppVersionEntity?,
    clientAppVersion: String,
    onPublishVersion: (versionNumber: String, releaseNotes: String, isMandatory: Boolean, (Boolean, String?) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var newVersionNumber by remember { mutableStateOf("") }
    var releaseNotes by remember { mutableStateOf("") }
    var isMandatory by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Formulario de Publicación de Nueva Versión
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FinanceSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Publicar Nueva Versión",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Versión en ejecución: v$clientAppVersion • Última registrada: v${latestVersion?.versionNumber ?: "1.0.0"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }

                if (validationError != null) {
                    ErrorWarningBox(message = validationError!!)
                }

                if (successMessage != null) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinanceSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF166534), fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = newVersionNumber,
                    onValueChange = {
                        newVersionNumber = it
                        validationError = null
                        successMessage = null
                    },
                    label = { Text("Número de Versión *") },
                    placeholder = { Text("ej. 1.1.0 o 2.0.0") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("version_number_input")
                )

                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = {
                        releaseNotes = it
                        validationError = null
                        successMessage = null
                    },
                    label = { Text("Notas de Cambio / Novedades *") },
                    placeholder = { Text("Describe las mejoras, correcciones y funciones añadidas en este lanzamiento...") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("version_release_notes_input")
                )

                // Interruptor de Obligatoriedad
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMandatory) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMandatory) "Actualización OBLIGATORIA" else "Actualización OPCIONAL",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMandatory) FinanceError else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isMandatory)
                                    "Los usuarios con versiones anteriores no podrán usar la app hasta actualizar."
                                else
                                    "Los usuarios recibirán aviso pero podrán continuar usando la app sin actualizar.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = isMandatory,
                            onCheckedChange = { isMandatory = it },
                            modifier = Modifier.testTag("version_mandatory_switch")
                        )
                    }
                }

                Button(
                    onClick = {
                        // Validación estricta requerida: las notas de cambio y número de versiones no pueden ser nulas ni vacías
                        if (newVersionNumber.trim().isEmpty()) {
                            validationError = "El número de versión no puede ser nulo ni vacío."
                            return@Button
                        }
                        if (releaseNotes.trim().isEmpty()) {
                            validationError = "Las notas de cambios no pueden ser nulas ni vacías."
                            return@Button
                        }

                        onPublishVersion(newVersionNumber.trim(), releaseNotes.trim(), isMandatory) { success, error ->
                            if (success) {
                                // Notificación confirmatoria para el administrador indicando el envío del anuncio al usuario final
                                successMessage = "Versión v${newVersionNumber.trim()} publicada exitosamente. El anuncio de actualización fue enviado a los usuarios finales con éxito."
                                validationError = null
                                newVersionNumber = ""
                                releaseNotes = ""
                                isMandatory = false
                            } else {
                                validationError = error ?: "Error al publicar la versión."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_publish_version")
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publicar Versión al Sistema", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Historial de Versiones Publicadas
        Text(
            text = "Historial de Versiones Registradas (${versions.size}):",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(versions, key = { it.id }) { ver ->
                val isLatest = latestVersion?.id == ver.id

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("version_card_${ver.versionNumber}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "v${ver.versionNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                if (isLatest) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "VIGENTE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (ver.isMandatory) MaterialTheme.colorScheme.errorContainer else FinanceSuccessContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (ver.isMandatory) "OBLIGATORIA" else "OPCIONAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (ver.isMandatory) MaterialTheme.colorScheme.onErrorContainer else FinanceSuccess,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lanzada el: ${ver.releaseDate}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ver.releaseNotes,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
