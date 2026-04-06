package com.animesh.gullak.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animesh.gullak.data.datastore.UserPreferencesDataStore
import com.animesh.gullak.data.repository.FinanceRepository
import com.animesh.gullak.domain.model.CategorySpending
import com.animesh.gullak.domain.model.FinancialSummary
import com.animesh.gullak.domain.model.WeeklyTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class InsightsUiState(
    val currentMonthSummary: FinancialSummary = FinancialSummary(0.0, 0.0, 0.0, 0.0),
    val lastMonthSummary: FinancialSummary = FinancialSummary(0.0, 0.0, 0.0, 0.0),
    val categorySpending: List<CategorySpending> = emptyList(),
    val weeklyTrend: List<WeeklyTrend> = emptyList(),
    val currencySymbol: String = "₹",
    val topInsight: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            combine(
                repository.getCategorySpendingForMonth(YearMonth.now()),
                prefsDataStore.userPreferences
            ) { categorySpending, prefs ->
                val currentSummary = repository.getFinancialSummaryForMonth(YearMonth.now())
                val lastSummary = repository.getFinancialSummaryForMonth(YearMonth.now().minusMonths(1))
                val weekly = repository.getWeeklyTrend()
                val topCategory = categorySpending.firstOrNull()
                val insight = buildInsight(currentSummary, lastSummary, topCategory, prefs.currencySymbol)

                _uiState.update {
                    it.copy(
                        currentMonthSummary = currentSummary,
                        lastMonthSummary = lastSummary,
                        categorySpending = categorySpending,
                        weeklyTrend = weekly,
                        currencySymbol = prefs.currencySymbol,
                        topInsight = insight,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun buildInsight(
        current: FinancialSummary,
        last: FinancialSummary,
        topCategory: CategorySpending?,
        symbol: String
    ): String {
        return when {
            current.totalExpenses == 0.0 -> "No expenses recorded this month. Great start! 🎉"
            current.totalExpenses > last.totalExpenses && last.totalExpenses > 0 -> {
                val diff = current.totalExpenses - last.totalExpenses
                "You've spent $symbol ${"%.0f".format(diff)} more than last month. Watch your ${topCategory?.category?.label ?: "spending"}! 📈"
            }
            current.totalExpenses < last.totalExpenses && last.totalExpenses > 0 -> {
                val diff = last.totalExpenses - current.totalExpenses
                "Great job! You saved $symbol ${"%.0f".format(diff)} compared to last month. 🎉"
            }
            topCategory != null ->
                "Your top spending is ${topCategory.category.emoji} ${topCategory.category.label} at ${"%.1f".format(topCategory.percentage)}% of expenses."
            current.savingsRate > 20 ->
                "You're saving ${"%.1f".format(current.savingsRate)}% of your income. Excellent work! 💪"
            else -> "Track more transactions to unlock personalized insights."
        }
    }
}
