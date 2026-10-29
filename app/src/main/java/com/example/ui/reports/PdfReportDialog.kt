package com.example.ui.reports

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceViewModel
import com.example.ui.components.ErrorWarningBox
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinanceOnPrimary
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formulario Interactivo para Generar y Exportar Reportes PDF Reales.
 * Permite seleccionar el rango de tiempo (Fecha de Inicio y Fecha de Fin) con selector de fechas o chips rápidos,
 * valida que existan movimientos en el período (mostrando mensaje de error estricto si no hay registros),
 * y compila un documento PDF estructurado en 2 tablas:
 *  1. Tabla de Balance General (Ingresos y Gastos).
 *  2. Tabla Desglosada por Billeteras y Cuentas Vinculadas con sus saldos.
 */
@Composable
fun ExportPdfReportDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.currentLanguage.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val accounts by viewModel.financialAccounts.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Fechas por defecto: Inicio de mes actual hasta el día actual
    val defaultStartCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val defaultEndCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    var startDateMillis by remember { mutableStateOf(defaultStartCal.timeInMillis) }
    var endDateMillis by remember { mutableStateOf(defaultEndCal.timeInMillis) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    // Función auxiliar para desplegar el selector de fechas nativo
    fun showDatePicker(initialMillis: Long, onDateSelected: (Long) -> Unit) {
        val c = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onDateSelected(selectedCal.timeInMillis)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = FinanceError,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (lang == com.example.util.AppLanguage.ENGLISH) "Export PDF Report" else "Generar Reporte PDF",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (lang == com.example.util.AppLanguage.ENGLISH)
                        "Select the date range for your financial report. It includes 2 detailed tables: General Balance and Breakdown by Wallets/Accounts."
                    else
                        "Selecciona el rango de tiempo para tu informe. Incluirá 2 tablas detalladas: Balance General y Desglose por Billeteras y Cuentas asociadas.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                // Mensaje de advertencia o error en caso de periodo inválido o sin movimientos
                if (errorMessage != null) {
                    ErrorWarningBox(message = errorMessage!!)
                }

                // Chips de selección rápida de rango de fechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            errorMessage = null
                            val now = Calendar.getInstance()
                            now.set(Calendar.DAY_OF_MONTH, 1)
                            now.set(Calendar.HOUR_OF_DAY, 0)
                            now.set(Calendar.MINUTE, 0)
                            now.set(Calendar.SECOND, 0)
                            startDateMillis = now.timeInMillis
                            endDateMillis = System.currentTimeMillis()
                        },
                        label = { Text("Este Mes", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FinanceSecondaryContainer)
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            errorMessage = null
                            val prevMonth = Calendar.getInstance().apply {
                                add(Calendar.MONTH, -1)
                                set(Calendar.DAY_OF_MONTH, 1)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            val prevMonthEnd = Calendar.getInstance().apply {
                                set(Calendar.DAY_OF_MONTH, 1)
                                add(Calendar.DAY_OF_MONTH, -1)
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                            }
                            startDateMillis = prevMonth.timeInMillis
                            endDateMillis = prevMonthEnd.timeInMillis
                        },
                        label = { Text("Mes Pasado", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FinanceSecondaryContainer)
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            errorMessage = null
                            val yearStart = Calendar.getInstance().apply {
                                set(Calendar.DAY_OF_YEAR, 1)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            startDateMillis = yearStart.timeInMillis
                            endDateMillis = System.currentTimeMillis()
                        },
                        label = { Text("Este Año", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FinanceSecondaryContainer)
                    )
                }

                // Selector de Fecha de Inicio
                Column {
                    Text(
                        text = if (lang == com.example.util.AppLanguage.ENGLISH) "Start Date:" else "Fecha de Inicio:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().clickable {
                        showDatePicker(startDateMillis) { selected ->
                            startDateMillis = selected
                            errorMessage = null
                        }
                    }) {
                        OutlinedTextField(
                            value = dateFormat.format(Date(startDateMillis)),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = FinancePrimary)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pdf_report_start_date")
                        )
                    }
                }

                // Selector de Fecha de Fin
                Column {
                    Text(
                        text = if (lang == com.example.util.AppLanguage.ENGLISH) "End Date:" else "Fecha de Fin:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().clickable {
                        showDatePicker(endDateMillis) { selected ->
                            // Ajustar al fin del día seleccionado
                            val endCal = Calendar.getInstance().apply {
                                timeInMillis = selected
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                            }
                            endDateMillis = endCal.timeInMillis
                            errorMessage = null
                        }
                    }) {
                        OutlinedTextField(
                            value = dateFormat.format(Date(endDateMillis)),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = FinancePrimary)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pdf_report_end_date")
                        )
                    }
                }

                // Cuadro informativo sobre la estructura del PDF (2 tablas requeridas)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceSecondaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📋 Estructura Oficial del Informe (2 Tablas):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = FinancePrimary)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• Tabla 1: Balance General (Ingresos y Gastos del período).\n• Tabla 2: Desglose por billeteras y cuentas con saldos.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF1E293B))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorMessage = null

                    // 1. Validar que la fecha de fin sea mayor o igual a la fecha de inicio
                    if (endDateMillis < startDateMillis) {
                        errorMessage = Localization.t("error_report_invalid_range", lang)
                        return@Button
                    }

                    // 2. Filtrar transacciones en el período seleccionado
                    val filteredTxs = allTransactions.filter { tx ->
                        tx.dateMillis in startDateMillis..endDateMillis
                    }

                    // 3. Regla obligatoria: En caso de que en el periodo seleccionado no exista registro alguno de movimientos,
                    // el sistema arrojará un mensaje de error indicándolo y pidiendo que ingrese un período válido
                    if (filteredTxs.isEmpty()) {
                        errorMessage = Localization.t("error_report_no_records", lang)
                        return@Button
                    }

                    // 4. Generar el documento PDF real estructurado en 2 tablas
                    try {
                        isGenerating = true
                        val userName = currentUser?.let { "${it.firstName} ${it.lastName}" } ?: "Usuario FinanzIQ"
                        val pdfFile = PdfReportGenerator.generateFinancialReportPdf(
                            context = context,
                            startDateMillis = startDateMillis,
                            endDateMillis = endDateMillis,
                            transactions = filteredTxs,
                            accounts = accounts,
                            currencySymbol = currency.symbol,
                            userName = userName
                        )

                        Toast.makeText(
                            context,
                            if (lang == com.example.util.AppLanguage.ENGLISH) "PDF Report generated successfully!" else "¡Reporte PDF generado con éxito!",
                            Toast.LENGTH_SHORT
                        ).show()

                        PdfReportGenerator.openOrSharePdf(context, pdfFile)
                        onDismiss()
                    } catch (e: Exception) {
                        errorMessage = "Error al generar PDF: ${e.localizedMessage ?: "Error desconocido"}"
                    } finally {
                        isGenerating = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary),
                enabled = !isGenerating,
                modifier = Modifier.testTag("generate_pdf_report_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = FinanceOnPrimary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (lang == com.example.util.AppLanguage.ENGLISH) "Generate & Download PDF" else "Generar y Descargar PDF",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.t("cancel", lang))
            }
        },
        shape = RoundedCornerShape(18.dp),
        // Se utiliza el color de superficie del tema para garantizar adaptabilidad perfecta tanto en modo claro como oscuro
        containerColor = MaterialTheme.colorScheme.surface
    )
}
