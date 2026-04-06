package com.animesh.gullak.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.animesh.gullak.domain.model.WeeklyTrend
import com.animesh.gullak.ui.components.TransactionListItem
import com.animesh.gullak.ui.theme.ExpenseRed
import com.animesh.gullak.ui.theme.IncomeGreen
import com.animesh.gullak.ui.theme.SavingsBlue

@Composable
fun HomeScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // Header / Balance Card
            BalanceCard(
                userName = uiState.userName,
                currencySymbol = uiState.currencySymbol,
                balance = uiState.summary.totalBalance,
                income = uiState.summary.totalIncome,
                expenses = uiState.summary.totalExpenses,
                savingsRate = uiState.summary.savingsRate
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            // Quick action buttons
            QuickActions(onAddTransaction = onNavigateToAddTransaction)
        }

        item {
            Spacer(Modifier.height(16.dp))
            // Weekly spending bar chart
            WeeklyTrendCard(
                trend = uiState.weeklyTrend,
                currencySymbol = uiState.currencySymbol
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToTransactions) {
                    Text("See all")
                }
            }
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                EmptyTransactionsPlaceholder(onAddTransaction = onNavigateToAddTransaction)
            }
        } else {
            items(uiState.recentTransactions) { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    currencySymbol = uiState.currencySymbol,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    userName: String,
    currencySymbol: String,
    balance: Double,
    income: Double,
    expenses: Double,
    savingsRate: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                "Hello, $userName 👋",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "This Month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "$currencySymbol ${"%,.2f".format(balance)}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Net Balance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BalanceStat("Income", income, currencySymbol, IncomeGreen)
                BalanceStat("Expenses", expenses, currencySymbol, ExpenseRed)
                BalanceStat("Savings", savingsRate, "%", SavingsBlue, isPercent = true)
            }
        }
    }
}

@Composable
private fun BalanceStat(
    label: String,
    amount: Double,
    symbol: String,
    color: Color,
    isPercent: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                if (isPercent) "${"%.1f".format(amount)}$symbol"
                else "$symbol ${"%.0f".format(amount)}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun QuickActions(onAddTransaction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            label = "Add Income",
            icon = Icons.Default.Add,
            color = IncomeGreen,
            modifier = Modifier.weight(1f),
            onClick = onAddTransaction
        )
        QuickActionButton(
            label = "Add Expense",
            icon = Icons.Default.Remove,
            color = ExpenseRed,
            modifier = Modifier.weight(1f),
            onClick = onAddTransaction
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = color.copy(alpha = 0.05f))
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun WeeklyTrendCard(
    trend: List<WeeklyTrend>,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Weekly Spending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            if (trend.isEmpty() || trend.all { it.amount == 0.0 }) {
                Text(
                    "No spending data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                SimpleBarChart(trend = trend, currencySymbol = currencySymbol)
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    trend: List<WeeklyTrend>,
    currencySymbol: String
) {
    val maxAmount = trend.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0
    val barColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        trend.forEach { day ->
            val fraction = (day.amount / maxAmount).toFloat().coerceIn(0f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // animated bar
                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(600, easing = EaseOutCubic),
                    label = "bar_${day.dayLabel}"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(animatedFraction.coerceAtLeast(0.04f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (day.amount > 0) barColor else barColor.copy(alpha = 0.2f)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    day.dayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsPlaceholder(onAddTransaction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💸", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Add your first income or expense\nto start tracking",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddTransaction) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Transaction")
        }
    }
}
