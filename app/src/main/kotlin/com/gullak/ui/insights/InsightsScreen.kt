package com.animesh.gullak.ui.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.animesh.gullak.domain.model.CategorySpending
import com.animesh.gullak.domain.model.FinancialSummary
import com.animesh.gullak.domain.model.WeeklyTrend
import com.animesh.gullak.ui.theme.ExpenseRed
import com.animesh.gullak.ui.theme.IncomeGreen
import com.animesh.gullak.ui.theme.SavingsBlue

val chartColors = listOf(
    Color(0xFF6C63FF), Color(0xFFFF6584), Color(0xFF43C6AC),
    Color(0xFFFFB347), Color(0xFF87CEEB), Color(0xFFDDA0DD),
    Color(0xFF98FB98), Color(0xFFFFD700), Color(0xFFFA8072)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Insights", fontWeight = FontWeight.SemiBold) })
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smart insight banner
            if (state.topInsight.isNotBlank()) {
                item {
                    InsightBanner(message = state.topInsight)
                }
            }

            // This month vs last month comparison
            item {
                MonthComparisonCard(
                    current = state.currentMonthSummary,
                    last = state.lastMonthSummary,
                    currencySymbol = state.currencySymbol
                )
            }

            // Category donut chart
            item {
                CategoryBreakdownCard(
                    categorySpending = state.categorySpending,
                    currencySymbol = state.currencySymbol
                )
            }

            // Weekly bar chart
            item {
                WeeklySpendingCard(
                    trend = state.weeklyTrend,
                    currencySymbol = state.currencySymbol
                )
            }

            // Savings rate card
            item {
                SavingsRateCard(
                    savingsRate = state.currentMonthSummary.savingsRate
                )
            }
        }
    }
}

@Composable
private fun InsightBanner(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💡", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MonthComparisonCard(
    current: FinancialSummary,
    last: FinancialSummary,
    currencySymbol: String
) {
    val expenseDiff = current.totalExpenses - last.totalExpenses
    val incomeDiff = current.totalIncome - last.totalIncome

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "This Month vs Last Month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonStat(
                    label = "Income",
                    current = current.totalIncome,
                    diff = incomeDiff,
                    symbol = currencySymbol,
                    positiveIsGood = true
                )
                Divider(
                    modifier = Modifier
                        .height(60.dp)
                        .width(1.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                ComparisonStat(
                    label = "Expenses",
                    current = current.totalExpenses,
                    diff = expenseDiff,
                    symbol = currencySymbol,
                    positiveIsGood = false
                )
                Divider(
                    modifier = Modifier
                        .height(60.dp)
                        .width(1.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                ComparisonStat(
                    label = "Savings %",
                    current = current.savingsRate,
                    diff = current.savingsRate - last.savingsRate,
                    symbol = "",
                    isPercent = true,
                    positiveIsGood = true
                )
            }
        }
    }
}

@Composable
private fun ComparisonStat(
    label: String,
    current: Double,
    diff: Double,
    symbol: String,
    isPercent: Boolean = false,
    positiveIsGood: Boolean
) {
    val trendColor = when {
        diff > 0 && positiveIsGood -> IncomeGreen
        diff > 0 && !positiveIsGood -> ExpenseRed
        diff < 0 && positiveIsGood -> ExpenseRed
        diff < 0 && !positiveIsGood -> IncomeGreen
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    val trendIcon = if (diff > 0) "↑" else if (diff < 0) "↓" else "→"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (isPercent) "${"%.1f".format(current)}%" else "$symbol${"%.0f".format(current)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "$trendIcon ${"%.0f".format(Math.abs(diff))}${if (isPercent) "%" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = trendColor,
            fontWeight = FontWeight.Medium
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CategoryBreakdownCard(
    categorySpending: List<CategorySpending>,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            if (categorySpending.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No expense data this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut chart
                    DonutChart(
                        data = categorySpending,
                        modifier = Modifier.size(140.dp)
                    )

                    Spacer(Modifier.width(16.dp))

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categorySpending.take(5).forEachIndexed { i, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(chartColors[i % chartColors.size])
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "${item.category.emoji} ${item.category.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${"%.0f".format(item.percentage)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (categorySpending.size > 5) {
                            Text(
                                "+${categorySpending.size - 5} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    data: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.percentage.toDouble() }.toFloat()
    val animatedSweep by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "donut_sweep"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            data.forEachIndexed { index, item ->
                val sweep = (item.percentage / total) * 360f * animatedSweep
                drawArc(
                    color = chartColors[index % chartColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f,
                    useCenter = false,
                    style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Butt),
                    size = Size(size.width - 28.dp.toPx(), size.height - 28.dp.toPx()),
                    topLeft = Offset(14.dp.toPx(), 14.dp.toPx())
                )
                startAngle += sweep
            }
        }
        Text(
            "${data.size}\ncats",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WeeklySpendingCard(
    trend: List<WeeklyTrend>,
    currencySymbol: String
) {
    val maxAmount = trend.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0
    val totalWeek = trend.sumOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("7-Day Spending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "$currencySymbol ${"%.0f".format(totalWeek)} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                trend.forEach { day ->
                    val fraction = (day.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                    val animatedFraction by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(700, easing = EaseOutCubic),
                        label = "bar_${day.dayLabel}"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (day.amount > 0) {
                            Text(
                                "${"%.0f".format(day.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .fillMaxHeight(animatedFraction.coerceAtLeast(0.03f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (day.amount == maxAmount) SavingsBlue
                                    else if (day.amount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(day.dayLabel, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val avg = if (trend.any { it.amount > 0 }) totalWeek / trend.count { it.amount > 0 } else 0.0
            Text(
                "Daily avg: $currencySymbol ${"%.0f".format(avg)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SavingsRateCard(savingsRate: Double) {
    val clampedRate = savingsRate.coerceIn(0.0, 100.0).toFloat()
    val animatedRate by animateFloatAsState(
        targetValue = clampedRate / 100f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "savings_rate"
    )
    val rateColor = when {
        clampedRate >= 30 -> IncomeGreen
        clampedRate >= 15 -> Color(0xFFFFB347)
        else -> ExpenseRed
    }
    val rateLabel = when {
        clampedRate >= 30 -> "Excellent! Keep it up 🚀"
        clampedRate >= 20 -> "Great savings habit 💪"
        clampedRate >= 10 -> "Room for improvement 📈"
        clampedRate > 0 -> "Try to save more 🎯"
        else -> "No savings this month"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Savings Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = rateColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedRate,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        "${"%.0f".format(clampedRate)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = rateColor
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(rateLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Financial experts recommend saving at least 20% of income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
