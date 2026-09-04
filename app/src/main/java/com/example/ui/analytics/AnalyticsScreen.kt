package com.example.ui.analytics

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatCordobas
import com.example.ui.theme.FinanceBackground
import com.example.ui.theme.FinanceError
import com.example.ui.theme.FinancePrimary
import com.example.ui.theme.FinanceSecondaryContainer
import com.example.ui.theme.FinanceSuccess
import com.example.ui.theme.FinanceSuccessContainer
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Mensual") } // "Semanal", "Mensual", "Anual"

    // Calculate income and expense based on period
    val now = Calendar.getInstance()
    val currentWeek = now.get(Calendar.WEEK_OF_YEAR)
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)

    val periodTransactions = allTransactions.filter { tx ->
        val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
        when (selectedPeriod) {
            "Semanal" -> txCal.get(Calendar.WEEK_OF_YEAR) == currentWeek && txCal.get(Calendar.YEAR) == currentYear
            "Mensual" -> txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
            "Anual" -> txCal.get(Calendar.YEAR) == currentYear
            else -> true
        }
    }

    val periodIncome = periodTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val periodExpense = periodTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val periodBalance = periodIncome - periodExpense

    val totalVolume = (periodIncome + periodExpense).coerceAtLeast(1.0)
    val incomeRatio = (periodIncome / totalVolume).toFloat().coerceIn(0f, 1f)
    val expenseRatio = (periodExpense / totalVolume).toFloat().coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Análisis y Reportes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinanceBackground)
            )
        },
        containerColor = FinanceBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semanal", "Mensual", "Anual").forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text("Balance $period") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinanceSecondaryContainer,
                                selectedLabelColor = FinancePrimary
                            ),
                            modifier = Modifier.testTag("period_tab_$period")
                        )
                    }
                }
            }

            // Income vs Expense Chart Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FinanceSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = FinancePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gráfico Ingresos vs. Gastos",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                                Text(
                                    text = "Comparativa del periodo $selectedPeriod",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Visual Distribution Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE5E7EB))
                        ) {
                            if (incomeRatio > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(incomeRatio.coerceAtLeast(0.01f))
                                        .height(16.dp)
                                        .background(FinanceSuccess)
                                )
                            }
                            if (expenseRatio > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(expenseRatio.coerceAtLeast(0.01f))
                                        .height(16.dp)
                                        .background(FinanceError)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stat Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(FinanceSuccess))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ingresos (${String.format(Locale.US, "%.0f", incomeRatio * 100)}%)", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4B5563)))
                                }
                                Text(formatCordobas(periodIncome), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FinanceSuccess))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(FinanceError))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gastos (${String.format(Locale.US, "%.0f", expenseRatio * 100)}%)", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4B5563)))
                                }
                                Text(formatCordobas(periodExpense), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FinanceError))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FinanceSecondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Balance Neto del Periodo:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                )
                                Text(
                                    text = formatCordobas(periodBalance),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (periodBalance >= 0) FinanceSuccess else FinanceError
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Category Expense Breakdown
            item {
                SectionHeader(title = "Distribución de Gastos por Categoría")
            }

            if (categoryBreakdown.isEmpty()) {
                item {
                    Text(
                        text = "No hay datos de gastos registrados para mostrar distribución.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            } else {
                items(categoryBreakdown) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                                Text(
                                    text = "${formatCordobas(item.totalAmount)} (${String.format(Locale.US, "%.1f", item.percentage)}%)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = FinancePrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (item.percentage / 100).toFloat().coerceIn(0f, 1f) },
                                color = FinancePrimary,
                                trackColor = Color(0xFFE5E7EB),
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
