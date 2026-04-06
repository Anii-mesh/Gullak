package com.animesh.gullak.ui.transactions

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.animesh.gullak.domain.model.TransactionCategory
import com.animesh.gullak.domain.model.TransactionType
import com.animesh.gullak.ui.components.SwipeToDeleteContainer
import com.animesh.gullak.ui.components.TransactionListItem
import java.time.format.DateTimeFormatter
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.transactionsUiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, "Add transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = uiState.filter.query,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (uiState.filter.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )
            }


            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filter.type == TransactionType.INCOME,
                        onClick = {
                            viewModel.updateTypeFilter(
                                if (uiState.filter.type == TransactionType.INCOME) null
                                else TransactionType.INCOME
                            )
                        },
                        label = { Text("Income") },
                        leadingIcon = if (uiState.filter.type == TransactionType.INCOME) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filter.type == TransactionType.EXPENSE,
                        onClick = {
                            viewModel.updateTypeFilter(
                                if (uiState.filter.type == TransactionType.EXPENSE) null
                                else TransactionType.EXPENSE
                            )
                        },
                        label = { Text("Expense") },
                        leadingIcon = if (uiState.filter.type == TransactionType.EXPENSE) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                items(TransactionCategory.entries.toList()) { category ->
                    FilterChip(
                        selected = uiState.filter.category == category,
                        onClick = {
                            viewModel.updateCategoryFilter(
                                if (uiState.filter.category == category) null else category
                            )
                        },
                        label = { Text("${category.emoji} ${category.label}") }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.transactions.isEmpty()) {
                EmptyState()
            } else {
                // Group by date
                val grouped = uiState.transactions.groupBy { it.date }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    grouped.forEach { (date, txns) ->
                        item(key = date.toString()) {
                            val dayTotal = txns.sumOf {
                                if (it.type == TransactionType.INCOME) it.amount else -it.amount
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    date.format(DateTimeFormatter.ofPattern("dd MMM, EEE")),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "${if (dayTotal >= 0) "+" else ""}${"%.2f".format(dayTotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (dayTotal >= 0) com.animesh.gullak.ui.theme.IncomeGreen
                                    else com.animesh.gullak.ui.theme.ExpenseRed
                                )
                            }
                        }
                        items(txns, key = { it.id }) { transaction ->
                            SwipeToDeleteContainer(
                                item = transaction,
                                onDelete = { viewModel.deleteTransaction(it) }
                            ) {
                                TransactionListItem(
                                    transaction = it,
                                    currencySymbol = uiState.currencySymbol,
                                    onClick = { onNavigateToEdit(it.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔍", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text("No transactions found", style = MaterialTheme.typography.titleMedium)
        Text(
            "Try adjusting your search or filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
