package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.local.AppVersionEntity
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSuccess

/**
 * Diálogo de Notificación y Forzado de Actualizaciones del Sistema.
 * 
 * Qué hace:
 * Evalúa si la versión instalada en el cliente es anterior a la última versión publicada
 * por el Administrador en la base de datos.
 * 
 * Por qué se implementa de esta forma:
 * Satisface textualmente los requisitos del Control de Versiones:
 * 1. Si la versión está marcada como OBLIGATORIA (isMandatory = true):
 *    El diálogo no se puede descartar (onDismissRequest vacío), y muestra el aviso explícito:
 *    "su versión es obsoleta y debe actualizar obligatoriamente la app".
 * 2. Si la versión está marcada como OPCIONAL:
 *    Permite al usuario la opción de "Seguir usando la app sin actualizar" o actualizar inmediatamente.
 * 
 * Cómo se relaciona con el resto del código:
 * Se invoca en la raíz de la interfaz de la aplicación (MainAppNavigation) cuando el usuario inicia sesión.
 */
@Composable
fun AppUpdatePromptDialog(
    latestVersion: AppVersionEntity?,
    clientAppVersion: String,
    onUpdateApp: () -> Unit,
    onContinueWithoutUpdate: () -> Unit
) {
    if (latestVersion == null || latestVersion.versionNumber == clientAppVersion) {
        return
    }

    val isMandatory = latestVersion.isMandatory
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            if (!isMandatory) {
                onContinueWithoutUpdate()
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isMandatory) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMandatory) Icons.Default.Warning else Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = if (isMandatory) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isMandatory) "Actualización Obligatoria" else "Nueva Versión Disponible",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Versión instalada: v$clientAppVersion • Nueva: v${latestVersion.versionNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isMandatory) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "su versión es obsoleta y debe actualizar obligatoriamente la app",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hay mejoras y optimizaciones disponibles para FinanzIQ. Puedes actualizar ahora o seguir usando la aplicación.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Text(
                    text = "Notas de la versión v${latestVersion.versionNumber}:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = latestVersion.releaseNotes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdateApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMandatory) FinanceError else FinancePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_app_update")
            ) {
                Text(
                    text = if (isMandatory) "Actualizar Ahora (Obligatorio)" else "Actualizar Ahora",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!isMandatory) {
                OutlinedButton(
                    onClick = onContinueWithoutUpdate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_skip_optional_update")
                ) {
                    Text(
                        text = "Seguir usando la app sin actualizar",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
