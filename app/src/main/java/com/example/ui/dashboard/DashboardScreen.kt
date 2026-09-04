package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatCordobas
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceBorderSky
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinancePrimaryContainer
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.ui.theme.FinanceWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onOpenAddTransaction: (type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val bills by viewModel.bills.collectAsState()

    val pendingBills = bills.filter { !it.isPaid }

    Surface(
        color = FinanceBackground,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Sleek Header (Bienvenido + User Avatar badge)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BIENVENIDO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFF94A3B8)
                            )
                        )
                        Text(
                            text = "${currentUser?.firstName ?: "Usuario"} ${currentUser?.lastName ?: ""}".trim(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }

                    // Sleek Avatar with Double Ring/Square styling
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE0F2FE))
                            .border(2.dp, Color(0xFFF0F9FF), RoundedCornerShape(16.dp))
                            .clickable { onNavigateToAiAssistant() },
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = ((currentUser?.firstName?.take(1) ?: "U") + (currentUser?.lastName?.take(1) ?: "")).uppercase()
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FinancePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials.ifBlank { "U" },
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Sleek Hero Balance Card (bg-[#E3F2FD] with border-sky-100)
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = FinancePrimaryContainer),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(FinanceBorderSky),
                        width = 1.dp
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_balance_card")
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Balance Mensual",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF075985),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF0369A1),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.6.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = formatCordobas(summary.balance),
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = Color(0xFF0C4A6E),
                                fontWeight = FontWeight.ExtraBold
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Income Pill Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.55f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "INGRESOS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF0369A1),
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+ ${formatCordobas(summary.totalIncome)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = FinanceSuccess,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Expense Pill Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.55f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "GASTOS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF0369A1),
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "- ${formatCordobas(summary.totalExpense)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = FinanceError,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sleek AI Assistant Card (White card with sparkles badge and quote)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0F2FE)),
                        width = 1.5.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAiAssistant() }
                        .testTag("ai_assistant_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✨", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ASISTENTE FINANCIERO IA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val topInsight = summary.automatedInsights.firstOrNull()
                                ?: "Revisa tus metas y mantén un balance saludable este mes."
                            Text(
                                text = "\"$topInsight\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF334155),
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // Quick Actions Bar (Sleek Pills)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onOpenAddTransaction("INCOME") },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceSecondaryContainer),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("add_income_quick_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ingreso", color = FinancePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = { onOpenAddTransaction("EXPENSE") },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE4E6)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("add_expense_quick_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = FinanceError, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gasto", color = FinanceError, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = onNavigateToSavings,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(46.dp)
                            .testTag("simulate_savings_button")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = FinancePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulador", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Metas y Presupuesto Section (Sleek slate-50 Card with clean progress bar)
            item {
                SectionHeader(
                    title = "Metas y Presupuesto",
                    actionText = "Ver Todo",
                    onActionClick = onNavigateToSavings
                )
            }

            item {
                val primaryGoal = savingsGoals.firstOrNull()
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF1F5F9)),
                        width = 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSavings() }
                        .testTag("savings_summary_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        val goalTitle = primaryGoal?.title ?: "Meta: Ahorro 1 Año"
                        val currentAmt = primaryGoal?.currentAmount ?: 4500.0
                        val targetAmt = primaryGoal?.targetAmount ?: 6000.0
                        val progressRatio = (currentAmt / targetAmt).coerceIn(0.0, 1.0).toFloat()
                        val progressPercent = (progressRatio * 100).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goalTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            )
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sleek Progress Bar
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            color = FinancePrimary,
                            trackColor = Color(0xFFE2E8F0),
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (primaryGoal != null) "Ahorrado: ${formatCordobas(currentAmt)} de ${formatCordobas(targetAmt)}" else "Si ahorras C$500 al mes, en un año tendrás C$6,000.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Spending Prediction & Savings Rate Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month End Spending Prediction
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF1F5F9)),
                            width = 1.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("spending_prediction_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "PREDICCIÓN MES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = Color(0xFF0369A1)
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = FinancePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatCordobas(summary.projectedMonthEndExpense),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = "Proyección al cierre",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Savings Rate Indicator
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF1F5F9)),
                            width = 1.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("savings_rate_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TASA DE AHORRO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = Color(0xFF065F46)
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.Percent,
                                    contentDescription = null,
                                    tint = FinanceSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", summary.savingsRatePercentage)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = "Del total de ingresos",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Pending Bills Due Alert (if any)
            if (pendingBills.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A)),
                            width = 1.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToBills() }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFDE68A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = FinanceWarning,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${pendingBills.size} Pagos Próximos a Vencer",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF78350F)
                                        )
                                    )
                                    Text(
                                        text = "Total pendiente: ${formatCordobas(pendingBills.sumOf { it.amount })}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF92400E)
                                        )
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Ver", tint = Color(0xFF78350F))
                        }
                    }
                }
            }

            // Movimientos Recientes Section
            item {
                SectionHeader(
                    title = "Movimientos Recientes",
                    actionText = "Ver Todos",
                    onActionClick = onNavigateToTransactions
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF1F5F9)),
                            width = 1.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aún no tienes movimientos registrados",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onOpenAddTransaction("EXPENSE") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FinancePrimary)
                            ) {
                                Text("Registrar Primer Movimiento")
                            }
                        }
                    }
                }
            } else {
                items(transactions.take(5)) { tx ->
                    SleekTransactionItemRow(tx = tx)
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun SleekTransactionItemRow(
    tx: TransactionEntity,
    modifier: Modifier = Modifier
) {
    val isIncome = tx.type == "INCOME"
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.dateMillis))

    // Emoji or tinted icon based on category
    val (iconBg, emoji) = when {
        isIncome -> Color(0xFFD1FAE5) to "💰"
        tx.category.contains("Aliment", ignoreCase = true) || tx.category.contains("Comida", ignoreCase = true) -> Color(0xFFFFEDD5) to "🍔"
        tx.category.contains("Luz", ignoreCase = true) || tx.category.contains("Servicio", ignoreCase = true) || tx.category.contains("Agua", ignoreCase = true) -> Color(0xFFE0F2FE) to "💡"
        tx.category.contains("Transp", ignoreCase = true) || tx.category.contains("Gas", ignoreCase = true) -> Color(0xFFEDE9FE) to "🚗"
        tx.category.contains("Salud", ignoreCase = true) || tx.category.contains("Farm", ignoreCase = true) -> Color(0xFFFFE4E6) to "🏥"
        tx.category.contains("Educ", ignoreCase = true) -> Color(0xFFFEF3C7) to "📚"
        else -> Color(0xFFF1F5F9) to "🏷️"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF1F5F9)),
            width = 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.category,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = FinancePrimary,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = " • $dateStr",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = "${if (isIncome) "+ " else "- "}${formatCordobas(tx.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) FinanceSuccess else FinanceError
                )
            )
        }
    }
}
