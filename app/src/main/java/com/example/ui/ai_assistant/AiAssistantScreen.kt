package com.example.ui.ai_assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatMessage
import com.example.ui.FinanceViewModel
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.util.AppLanguage
import com.example.util.CurrencyConversionUtil
import com.example.util.CurrencyOption
import com.example.util.Localization

/**
 * Interfaz de Asistente Virtual Inteligente de FinanzIQ.
 * 
 * Qué hace:
 * 1. Proporciona una interfaz conversacional fluida para consultar finanzas personales.
 * 2. Soporta la generación de gráficos interactivos de Ingresos vs Gastos por Categorías, Semanas o Meses.
 * 3. Muestra una lista de textos estructurados con recomendaciones financieras personalizadas
 *    según el resultado específico que arroje el gráfico.
 * 4. Valida y notifica si el usuario no tiene datos suficientes en el filtro pedido, entregando
 *    consejos financieros predeterminados en su lugar.
 * 5. Genera tablas ordenadas de meses según el año en curso indicando ingresos, gastos y balance final.
 * 6. Valida y advierte si el rango de fechas es inválido (fecha de fin mayor a la actual o sin datos).
 * 7. Garantiza que todos los datos corresponden estrictamente al usuario específico autenticado.
 * 
 * Por qué se implementa de esa forma:
 * Satisface cada una de las directrices y reglas del usuario sobre analítica en el asistente,
 * utilizando componentes Jetpack Compose de Material 3 con total adaptación al modo oscuro y claro.
 * 
 * Cómo se relaciona con el resto del código:
 * Observa reactivamente el flujo de mensajes de FinanceViewModel, interactúa con AiFinancialAnalyticsEngine,
 * y formatea importes usando CurrencyConversionUtil para respetar las preferencias de divisa del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isAiLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Prompts sugeridos con accesos directos a gráficos, tablas y validaciones analíticas
    val samplePrompts = if (currentLang == AppLanguage.ENGLISH) {
        listOf(
            "📊 Chart of income vs expenses by category",
            "📈 Chart by months",
            "🗓️ Chart by weeks",
            "📋 Monthly table of current year",
            "⚠️ Test future date range (invalid)",
            Localization.t("ai_prompt_1", currentLang),
            Localization.t("ai_prompt_2", currentLang)
        )
    } else {
        listOf(
            "📊 Gráfico de ingresos vs gastos por categorías",
            "📈 Gráfico por meses",
            "🗓️ Gráfico por semanas",
            "📋 Tabla de meses del año en curso",
            "⚠️ Probar rango de fechas futuras (inválido)",
            Localization.t("ai_prompt_1", currentLang),
            Localization.t("ai_prompt_2", currentLang)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(FinanceSecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FinancePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Localization.t("ai_assistant_title", currentLang),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = Localization.t("ai_assistant_subtitle", currentLang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize().imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Historial de mensajes y tarjetas analíticas
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages) { msg ->
                    ChatBubbleItem(
                        msg = msg,
                        selectedCurrency = selectedCurrency,
                        isEnglish = currentLang == AppLanguage.ENGLISH
                    )
                }

                if (isAiLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = FinancePrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Localization.t("ai_analyzing", currentLang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preguntas sugeridas dinámicas según el idioma y funciones analíticas
            Text(
                text = Localization.t("ai_prompt_suggestion_header", currentLang),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                items(samplePrompts) { prompt ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.sendAiMessage(prompt)
                        },
                        label = { Text(prompt, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Campo de entrada de consulta
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = {
                        Text(Localization.t("ai_input_placeholder", currentLang))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputPrompt.isNotBlank()) {
                                viewModel.sendAiMessage(inputPrompt)
                                inputPrompt = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FinancePrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputPrompt.isNotBlank()) {
                            viewModel.sendAiMessage(inputPrompt)
                            inputPrompt = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FinancePrimary)
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = Localization.t("ai_send", currentLang),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renderizador de burbujas de mensaje para la interfaz del Asistente IA.
 * 
 * Qué hace:
 * Despliega los mensajes del usuario o del asistente.
 * Si el mensaje contiene un gráfico analítico (`chartData`), renderiza el componente visual de barras
 * o la tabla ordenada de meses.
 * Si contiene recomendaciones financieras (`recommendations`), las presenta en un bloque estructurado.
 * Si es una advertencia de rango de fechas erróneo (`isErrorNotice`), muestra un cuadro de alerta destacado.
 */
@Composable
fun ChatBubbleItem(
    msg: ChatMessage,
    selectedCurrency: CurrencyOption,
    isEnglish: Boolean
) {
    val isUser = msg.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(FinanceSecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) FinancePrimary else MaterialTheme.colorScheme.surface
            ),
            border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.94f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // 1. Texto principal del mensaje
                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                )

                // 2. Alerta destacada si el rango de fechas es inválido o no hay datos
                if (msg.isErrorNotice) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().testTag("ai_date_error_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isEnglish) {
                                    "Date range rejected: Future dates or ranges without movements are not accepted. The table cannot be generated."
                                } else {
                                    "Validación de fechas: No se permiten fechas futuras ni rangos sin movimientos. No es posible generar la tabla."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                }

                // 3. Renderizado de Gráfico o Tabla de Meses
                if (msg.chartData != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    if (msg.chartData.isTableOnly) {
                        // Renderizado de Tabla Ordenada de Meses del Año en Curso
                        AiMonthlyTableView(
                            chartData = msg.chartData,
                            selectedCurrency = selectedCurrency,
                            isEnglish = isEnglish
                        )
                    } else {
                        // Renderizado de Gráfico de Barras Comparativo (Categorías, Semanas, Meses)
                        AiVisualBarChartView(
                            chartData = msg.chartData,
                            selectedCurrency = selectedCurrency,
                            isEnglish = isEnglish
                        )
                    }
                }

                // 4. Lista estructurada de Recomendaciones Financieras
                if (msg.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    AiRecommendationsListView(
                        recommendations = msg.recommendations,
                        isEnglish = isEnglish
                    )
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Componente visual de tabla mensual ordenada para el año en curso.
 * Muestra meses cronológicamente según los registros del usuario con:
 * Ingresos, Gastos y Balance Final.
 */
@Composable
fun AiMonthlyTableView(
    chartData: AiChartData,
    selectedCurrency: CurrencyOption,
    isEnglish: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("ai_monthly_table_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = chartData.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (chartData.subtitle.isNotBlank()) {
                        Text(
                            text = chartData.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Cabecera de la tabla
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Month" else "Mes",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = if (isEnglish) "Income" else "Ingresos",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = FinanceSuccess),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = if (isEnglish) "Expenses" else "Gastos",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = FinanceError),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = if (isEnglish) "Final Bal." else "Bal. Final",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Filas de meses ordenados cronológicamente
            chartData.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1.3f)
                    )
                    Text(
                        text = CurrencyConversionUtil.formatWithConversion(item.income, selectedCurrency),
                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceSuccess, fontSize = 11.sp),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.1f)
                    )
                    Text(
                        text = CurrencyConversionUtil.formatWithConversion(item.expense, selectedCurrency),
                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceError, fontSize = 11.sp),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.1f)
                    )
                    Text(
                        text = CurrencyConversionUtil.formatWithConversion(item.balance, selectedCurrency),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.balance >= 0) FinanceSuccess else FinanceError,
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.1f)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de totales consolidados
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Annual Balance:" else "Balance Anual:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = CurrencyConversionUtil.formatWithConversion(chartData.netBalance, selectedCurrency),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (chartData.netBalance >= 0) FinanceSuccess else FinanceError
                        )
                    )
                }
            }
        }
    }
}

/**
 * Componente visual de gráfico de barras interactivo de Ingresos vs Gastos
 * para Categorías, Semanas o Meses.
 */
@Composable
fun AiVisualBarChartView(
    chartData: AiChartData,
    selectedCurrency: CurrencyOption,
    isEnglish: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("ai_chart_view_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = chartData.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (chartData.subtitle.isNotBlank()) {
                        Text(
                            text = chartData.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Píldoras de métricas de resumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Total Ingresos
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FinanceSuccess.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnglish) "Incomes" else "Ingresos",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = FinanceSuccess)
                        )
                        Text(
                            text = CurrencyConversionUtil.formatWithConversion(chartData.totalIncome, selectedCurrency),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = FinanceSuccess, fontSize = 11.sp)
                        )
                    }
                }

                // Total Gastos
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FinanceError.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnglish) "Expenses" else "Gastos",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = FinanceError)
                        )
                        Text(
                            text = CurrencyConversionUtil.formatWithConversion(chartData.totalExpense, selectedCurrency),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = FinanceError, fontSize = 11.sp)
                        )
                    }
                }

                // Balance Neto
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (chartData.netBalance >= 0) FinanceSuccess else FinanceError).copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnglish) "Net Bal." else "Bal. Neto",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = if (chartData.netBalance >= 0) FinanceSuccess else FinanceError
                            )
                        )
                        Text(
                            text = CurrencyConversionUtil.formatWithConversion(chartData.netBalance, selectedCurrency),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (chartData.netBalance >= 0) FinanceSuccess else FinanceError,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Escala máxima para calcular anchura de barras proporcionales
            val maxAmount = chartData.items.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0

            // Barras visuales comparativas por elemento
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                chartData.items.take(8).forEach { item ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = (if (item.balance >= 0) "+" else "") + CurrencyConversionUtil.formatWithConversion(item.balance, selectedCurrency),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.balance >= 0) FinanceSuccess else FinanceError
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Barra comparativa de Ingresos (Verde)
                        if (item.income > 0) {
                            val inRatio = (item.income / maxAmount).toFloat().coerceIn(0.04f, 1f)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isEnglish) "In" else "Ing",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = FinanceSuccess),
                                    modifier = Modifier.width(24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(inRatio)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(FinanceSuccess)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = CurrencyConversionUtil.formatWithConversion(item.income, selectedCurrency),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = FinanceSuccess),
                                    modifier = Modifier.width(72.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Barra comparativa de Gastos (Rojo)
                        if (item.expense > 0) {
                            val exRatio = (item.expense / maxAmount).toFloat().coerceIn(0.04f, 1f)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isEnglish) "Exp" else "Gas",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = FinanceError),
                                    modifier = Modifier.width(24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(exRatio)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(FinanceError)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = CurrencyConversionUtil.formatWithConversion(item.expense, selectedCurrency),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = FinanceError),
                                    modifier = Modifier.width(72.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que renderiza la lista estructurada de recomendaciones financieras
 * arrojadas a partir del resultado del gráfico o de los consejos predeterminados.
 */
@Composable
fun AiRecommendationsListView(
    recommendations: List<String>,
    isEnglish: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("ai_recommendations_box")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = FinancePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnglish) "Assistant Financial Recommendations:" else "Recomendaciones Financieras del Asistente:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendations.forEach { recText ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                color = FinancePrimary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
