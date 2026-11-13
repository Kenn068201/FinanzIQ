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
import com.example.util.AppLanguage

/**
 * Diálogo de Notificación y Forzado de Actualizaciones del Sistema en la Interfaz de Usuario.
 * 
 * Qué hace:
 * Despliega un diálogo emergente modal cuando la versión instalada en la Interfaz de Usuario
 * difiere de la versión más reciente publicada por el Administrador.
 * Si la actualización es obligatoria, bloquea el cierre del diálogo hasta que el usuario pulse
 * "Actualizar Ahora (Obligatorio)".
 * 
 * Por qué se implementa de esta forma:
 * Sincroniza textualmente y con exactitud los mensajes entre la Interfaz de Administrador y la Interfaz de Usuario.
 * Muestra el aviso idéntico registrado en la administración:
 * "Actualización Obligatoria: Los usuarios con versiones anteriores no podrán usar la app hasta actualizar."
 * asegurando coherencia total en la experiencia del usuario y soporte en español e inglés.
 * 
 * Cómo se relaciona con el resto del código:
 * Se invoca desde AuthenticatedAppScaffold en MainAppNavigation. Al confirmar la actualización,
 * ejecuta clientUpdateAppVersion en FinanceViewModel para persistir la versión aceptada por el usuario.
 */
@Composable
fun AppUpdatePromptDialog(
    latestVersion: AppVersionEntity?,
    clientAppVersion: String,
    currentLanguage: AppLanguage = AppLanguage.SPANISH,
    onUpdateApp: () -> Unit,
    onContinueWithoutUpdate: () -> Unit
) {
    if (latestVersion == null || latestVersion.versionNumber == clientAppVersion) {
        return
    }

    val isMandatory = latestVersion.isMandatory
    val isSpanish = currentLanguage == AppLanguage.SPANISH
    val scrollState = rememberScrollState()

    // Textos sincronizados exactamente con la Interfaz de Administrador
    val titleText = if (isMandatory) {
        if (isSpanish) "Actualización OBLIGATORIA" else "MANDATORY Update"
    } else {
        if (isSpanish) "Actualización OPCIONAL" else "OPTIONAL Update"
    }

    val subtitleText = if (isSpanish) {
        "Versión instalada: v$clientAppVersion • Nueva versión: v${latestVersion.versionNumber}"
    } else {
        "Installed version: v$clientAppVersion • New version: v${latestVersion.versionNumber}"
    }

    val warningBannerText = if (isMandatory) {
        if (isSpanish) {
            "Actualización Obligatoria: Los usuarios con versiones anteriores no podrán usar la app hasta actualizar. Su versión actual (v$clientAppVersion) es obsoleta y debe actualizar obligatoriamente la aplicación a la versión v${latestVersion.versionNumber}."
        } else {
            "Mandatory Update: Users with previous versions cannot use the app until updating. Your current version (v$clientAppVersion) is obsolete and you must update the application to version v${latestVersion.versionNumber}."
        }
    } else {
        if (isSpanish) {
            "Actualización Opcional: Hay mejoras y optimizaciones disponibles para FinanzIQ. Puedes actualizar ahora o seguir usando la app sin actualizar."
        } else {
            "Optional Update: Enhancements available for FinanzIQ. You can update now or continue using the app without updating."
        }
    }

    val changelogTitle = if (isSpanish) {
        "Notas de Cambio / Novedades (v${latestVersion.versionNumber}):"
    } else {
        "Changelog / Release Notes (v${latestVersion.versionNumber}):"
    }

    val confirmButtonText = if (isMandatory) {
        if (isSpanish) "Actualizar Ahora (Obligatorio)" else "Update Now (Mandatory)"
    } else {
        if (isSpanish) "Actualizar Ahora" else "Update Now"
    }

    val dismissButtonText = if (isSpanish) {
        "Seguir usando la app sin actualizar"
    } else {
        "Continue using the app without updating"
    }

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
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = subtitleText,
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
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMandatory) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = warningBannerText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isMandatory) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Text(
                    text = changelogTitle,
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
                    text = confirmButtonText,
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
                        text = dismissButtonText,
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
