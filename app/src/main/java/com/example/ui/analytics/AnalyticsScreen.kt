package com.example.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryDataHierarchy
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

data class SubcategorySpending(
    val name: String,
    val icon: String,
    val amount: Double,
    val percentageOfCategory: Double
)

data class CategorySpending(
    val categoryName: String,
    val group: String,
    val icon: String,
    val totalAmount: Double,
    val percentageOfTotal: Double,
    val subcategories: List<SubcategorySpending>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Mensual") } // "Semanal", "Mensual", "Anual"
    var selectedGroupFilter by remember { mutableStateOf("Todos") } // "Todos", "Gastos Fijos", "Gastos Variables", "Pago de Deudas"
    var selectedCategoryForSubDetail by remember { mutableStateOf<String?>(null) }

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
    val periodExpenses = periodTransactions.filter { it.type == "EXPENSE" }
    val totalExpenseAmount = periodExpenses.sumOf { it.amount }
    val periodBalance = periodIncome - totalExpenseAmount

    val totalVolume = (periodIncome + totalExpenseAmount).coerceAtLeast(1.0)
    val incomeRatio = (periodIncome / totalVolume).toFloat().coerceIn(0f, 1f)
    val expenseRatio = (totalExpenseAmount / totalVolume).toFloat().coerceIn(0f, 1f)

    // Lookup helper for category emojis
    val allExpenseCategories = CategoryDataHierarchy.fixedExpenseGroup.categories +
        CategoryDataHierarchy.variableExpenseGroup.categories +
        CategoryDataHierarchy.debtPaymentGroup.categories
    val categoryIconMap = allExpenseCategories.associate { it.nameEs to it.iconEmoji }
    val subcategoryIconMap = allExpenseCategories.flatMap { it.subcategories }.associate { it.nameEs to it.iconEmoji }

    // Build hierarchical spending model
    val categorySpendings = remember(periodExpenses, selectedGroupFilter) {
        val groupedByCategory = periodExpenses.groupBy { it.category }
        groupedByCategory.map { (catName, txs) ->
            val catTotal = txs.sumOf { it.amount }
            val group = txs.firstOrNull()?.categoryGroup.orEmpty().ifBlank { "Gastos" }
            val catIcon = categoryIconMap[catName] ?: "📦"

            val groupedBySub = txs.groupBy { it.subCategory }
            val subSpendings = groupedBySub.mapNotNull { (subName, subTxs) ->
                if (subName.isBlank()) null
                else {
                    val subTotal = subTxs.sumOf { it.amount }
                    val subPct = if (catTotal > 0) (subTotal / catTotal) * 100 else 0.0
                    val subIcon = subcategoryIconMap[subName] ?: "🔹"
                    SubcategorySpending(subName, subIcon, subTotal, subPct)
                }
            }.sortedByDescending { it.amount }

            val totalPct = if (totalExpenseAmount > 0) (catTotal / totalExpenseAmount) * 100 else 0.0
            CategorySpending(catName, group, catIcon, catTotal, totalPct, subSpendings)
        }.filter {
            when (selectedGroupFilter) {
                "Gastos Fijos" -> it.group.contains("Fijo", ignoreCase = true)
                "Gastos Variables" -> it.group.contains("Variable", ignoreCase = true) || it.group.contains("Deseo", ignoreCase = true)
                "Pago de Deudas" -> it.group.contains("Deuda", ignoreCase = true)
                else -> true
            }
        }.sortedByDescending { it.totalAmount }
    }

    val maxCategoryAmount = categorySpendings.maxOfOrNull { it.totalAmount }?.coerceAtLeast(1.0) ?: 1.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Análisis Financiero",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
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

            // Income vs Expense Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Flujo de Dinero ($selectedPeriod)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (periodBalance >= 0) FinanceSuccessContainer else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = if (periodBalance >= 0) "Superávit" else "Déficit",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (periodBalance >= 0) FinanceSuccess else FinanceError,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stacked Horizontal Proportional Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            if (incomeRatio > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(incomeRatio.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(FinanceSuccess)
                                )
                            }
                            if (expenseRatio > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(expenseRatio.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(FinanceError)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(FinanceSuccess))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ingresos (${String.format(Locale.US, "%.0f", incomeRatio * 100)}%)", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                                }
                                Text(formatCordobas(periodIncome), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FinanceSuccess))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(FinanceError))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gastos (${String.format(Locale.US, "%.0f", expenseRatio * 100)}%)", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                                }
                                Text(formatCordobas(totalExpenseAmount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FinanceError))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FinanceSecondaryContainer.copy(alpha = 0.5f),
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
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
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

            // VERTICAL NESTED BAR CHART SECTION
            item {
                SectionHeader(title = "Gráfico de Barras Verticales: Categorías y Subcategorías")
            }

            // Group filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val filterOptions = listOf("Todos", "Gastos Fijos", "Gastos Variables", "Pago de Deudas")
                    items(filterOptions) { filter ->
                        FilterChip(
                            selected = selectedGroupFilter == filter,
                            onClick = {
                                selectedGroupFilter = filter
                                selectedCategoryForSubDetail = null
                            },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinanceSecondaryContainer,
                                selectedLabelColor = FinancePrimary
                            )
                        )
                    }
                }
            }

            // Vertical Nested Bar Chart Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = FinancePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Gasto por Categoría (Barras Verticales)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                            }
                            Text(
                                text = "Toca para ver subcategorías",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (categorySpendings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay registros de gastos para este filtro.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                )
                            }
                        } else {
                            // Vertical Bars Container (scrollable if many categories)
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                items(categorySpendings) { item ->
                                    val isSelected = selectedCategoryForSubDetail == item.categoryName
                                    val barHeightRatio = (item.totalAmount / maxCategoryAmount).toFloat().coerceIn(0.12f, 1.0f)
                                    val barHeightDp = (barHeightRatio * 130).dp

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(68.dp)
                                            .clickable {
                                                selectedCategoryForSubDetail = if (isSelected) null else item.categoryName
                                            }
                                    ) {
                                        // Amount label
                                        Text(
                                            text = "${String.format(Locale.US, "%.0f", item.percentageOfTotal)}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) FinancePrimary else Color(0xFF64748B),
                                                fontSize = 10.sp
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Vertical Bar with Nested Subcategory Segments
                                        Box(
                                            modifier = Modifier
                                                .width(36.dp)
                                                .height(barHeightDp)
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                                .background(if (isSelected) FinancePrimary else Color(0xFF38BDF8))
                                        ) {
                                            // If category has subcategories, stack nested visual indicators
                                            if (item.subcategories.isNotEmpty()) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.Bottom
                                                ) {
                                                    item.subcategories.take(3).forEachIndexed { subIndex, sub ->
                                                        val subRatio = (sub.percentageOfCategory / 100.0).toFloat().coerceIn(0.1f, 1.0f)
                                                        val shadeColor = when (subIndex) {
                                                            0 -> Color(0xFF0284C7)
                                                            1 -> Color(0xFF0EA5E9)
                                                            else -> Color(0xFF7DD3FC)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .weight(subRatio)
                                                                .background(shadeColor)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Category Emoji and Short Name
                                        Text(
                                            text = item.icon,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = item.categoryName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) FinancePrimary else Color(0xFF334155),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Selected Category Nested Subcategory Details Drawer
                        selectedCategoryForSubDetail?.let { selectedName ->
                            val selectedCat = categorySpendings.firstOrNull { it.categoryName == selectedName }
                            if (selectedCat != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${selectedCat.icon} Subcategorías de ${selectedCat.categoryName}:",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )
                                            )
                                            Text(
                                                text = formatCordobas(selectedCat.totalAmount),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = FinancePrimary
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        if (selectedCat.subcategories.isEmpty()) {
                                            Text(
                                                text = "No se registraron subcategorías específicas para esta categoría.",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                            )
                                        } else {
                                            // Nested mini vertical bars for subcategories
                                            LazyRow(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                val maxSubAmount = selectedCat.subcategories.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0
                                                items(selectedCat.subcategories) { sub ->
                                                    val subHeight = ((sub.amount / maxSubAmount).toFloat().coerceIn(0.15f, 1.0f) * 70).dp
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.width(60.dp)
                                                    ) {
                                                        Text(
                                                            text = "${String.format(Locale.US, "%.0f", sub.percentageOfCategory)}%",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = Color(0xFF0284C7),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 9.sp
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .width(22.dp)
                                                                .height(subHeight)
                                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                                .background(Color(0xFF0284C7))
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = sub.icon,
                                                            style = MaterialTheme.typography.labelMedium
                                                        )
                                                        Text(
                                                            text = sub.name,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                textAlign = TextAlign.Center,
                                                                color = Color(0xFF334155)
                                                            ),
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Category Spending Breakdown List
            item {
                SectionHeader(title = "Detalle Completo de Gastos")
            }

            if (categorySpendings.isEmpty()) {
                item {
                    Text(
                        text = "No hay datos de gastos registrados para mostrar distribución.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            } else {
                items(categorySpendings) { item ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = item.icon, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = item.categoryName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                        Text(
                                            text = item.group,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatCordobas(item.totalAmount),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = FinancePrimary
                                            )
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", item.percentageOfTotal)}%",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (item.percentageOfTotal / 100).toFloat().coerceIn(0f, 1f) },
                                color = FinancePrimary,
                                trackColor = Color(0xFFE2E8F0),
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            )

                            // If expanded and has subcategories, display them
                            if (isExpanded && item.subcategories.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Subcategorías:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                    )
                                    item.subcategories.forEach { sub ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${sub.icon} ${sub.name}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF1E293B))
                                            )
                                            Text(
                                                text = "${formatCordobas(sub.amount)} (${String.format(Locale.US, "%.0f", sub.percentageOfCategory)}%)",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0284C7)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
