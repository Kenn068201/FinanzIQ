package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceViewModel
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.util.AppLanguage
import com.example.util.CountryPhoneData
import com.example.util.CurrencyConversionUtil
import com.example.util.CurrencyOption

/**
 * Diálogo de Ajustes y Preferencias del Usuario Final.
 * 
 * Qué hace:
 * Se despliega al pulsar sobre el perfil en el Dashboard inicial o en la barra superior.
 * Permite modificar:
 * 1. Idioma de la aplicación (Español / Inglés).
 * 2. Moneda a utilizar (convirtiendo los importes desde Córdobas a la divisa elegida
 *    con los tipos de cambio oficiales de cada país).
 * 3. Modo oscuro o claro de la aplicación.
 * 4. Preferencias generales del sistema y botón de cerrar sesión.
 * 
 * Por qué se implementa de esta forma:
 * Satisface textualmente la solicitud del usuario final para la gestión integral de preferencias
 * desde la cabecera de perfil.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppSettingsDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentCurrency by viewModel.selectedCurrency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedLang by remember { mutableStateOf(currentLang) }
    var selectedCurr by remember { mutableStateOf(currentCurrency) }
    var isDarkTheme by remember { mutableStateOf(isDarkMode) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var hideBalancesEnabled by remember { mutableStateOf(false) }

    var showCurrencyDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (selectedLang == AppLanguage.SPANISH) "Ajustes de la Aplicación" else "App Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tarjeta de información del perfil
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceSecondaryContainer.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(FinancePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.firstName?.take(1)?.uppercase() ?: "U",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${currentUser?.firstName ?: "Usuario"} ${currentUser?.lastName ?: ""}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "@${currentUser?.username?.ifBlank { currentUser?.email } ?: ""}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                            )
                            Text(
                                text = "${currentUser?.phonePrefix} ${currentUser?.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280), fontSize = 11.sp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // 1. Selector de Idioma de la Aplicación
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedLang == AppLanguage.SPANISH) "Idioma de la Aplicación" else "App Language",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedLang == AppLanguage.SPANISH,
                            onClick = { selectedLang = AppLanguage.SPANISH },
                            label = { Text("🇪🇸 Español") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinanceSecondaryContainer,
                                selectedLabelColor = FinancePrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("settings_lang_es")
                        )
                        FilterChip(
                            selected = selectedLang == AppLanguage.ENGLISH,
                            onClick = { selectedLang = AppLanguage.ENGLISH },
                            label = { Text("🇺🇸 English") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinanceSecondaryContainer,
                                selectedLabelColor = FinancePrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("settings_lang_en")
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // 2. Selector de Moneda y Conversión con Tipos de Cambio Oficiales
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedLang == AppLanguage.SPANISH) "Moneda a Utilizar" else "Currency to Use",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "${selectedCurr.flag} ${selectedCurr.code} (${selectedCurr.symbol}) - ${selectedCurr.nameEs}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCurrencyDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Moneda")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCurrencyDropdown = true }
                                .testTag("settings_currency_selector")
                        )

                        DropdownMenu(
                            expanded = showCurrencyDropdown,
                            onDismissRequest = { showCurrencyDropdown = false }
                        ) {
                            CountryPhoneData.americanCurrencies.forEach { curr ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "${curr.flag} ${curr.code} (${curr.symbol}) • ${curr.nameEs}",
                                                fontWeight = if (curr.code == selectedCurr.code) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = CurrencyConversionUtil.getOfficialRateDescription(curr.code),
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCurr = curr
                                        showCurrencyDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Detalle del tipo de cambio oficial aplicado
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💱 " + CurrencyConversionUtil.getOfficialRateDescription(selectedCurr.code),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            if (selectedCurr.code != "NIO") {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ejemplo: C$ 1,000.00 = ${CurrencyConversionUtil.formatWithConversion(1000.0, selectedCurr)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = FinancePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // 3. Modo Oscuro o Claro
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (selectedLang == AppLanguage.SPANISH) "Tema Visual" else "Visual Theme",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isDarkTheme,
                            onClick = {
                                isDarkTheme = false
                                viewModel.setDarkMode(false)
                            },
                            label = { Text(if (selectedLang == AppLanguage.SPANISH) "Modo Claro" else "Light Mode") },
                            leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinanceSecondaryContainer,
                                selectedLabelColor = FinancePrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("settings_theme_light")
                        )

                        FilterChip(
                            selected = isDarkTheme,
                            onClick = {
                                isDarkTheme = true
                                viewModel.setDarkMode(true)
                            },
                            label = { Text(if (selectedLang == AppLanguage.SPANISH) "Modo Oscuro" else "Dark Mode") },
                            leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF334155),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).testTag("settings_theme_dark")
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // 4. Preferencias Generales
                Text(
                    text = if (selectedLang == AppLanguage.SPANISH) "Preferencias Generales" else "General Preferences",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedLang == AppLanguage.SPANISH) "Notificaciones y Alertas" else "Notifications & Alerts",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = if (selectedLang == AppLanguage.SPANISH) "Avisos de facturas y presupuestos" else "Bill and budget alerts",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                        )
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedLang == AppLanguage.SPANISH) "Ocultar saldos por privacidad" else "Hide balances for privacy",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = if (selectedLang == AppLanguage.SPANISH) "Enmascara los números en pantalla" else "Mask numbers on screen",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                        )
                    }
                    Switch(checked = hideBalancesEnabled, onCheckedChange = { hideBalancesEnabled = it })
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Botón de Cerrar Sesión
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onLogout()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceError),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("settings_logout_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = FinanceError, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedLang == AppLanguage.SPANISH) "Cerrar Sesión" else "Log Out",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateAppPreferences(
                        currency = selectedCurr,
                        lang = selectedLang,
                        isDark = isDarkTheme
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("settings_save_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedLang == AppLanguage.SPANISH) "Guardar Ajustes" else "Save Settings",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (selectedLang == AppLanguage.SPANISH) "Cancelar" else "Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
