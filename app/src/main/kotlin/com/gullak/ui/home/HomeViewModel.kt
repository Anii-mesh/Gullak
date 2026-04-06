package com.animesh.gullak.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animesh.gullak.data.datastore.UserPreferencesDataStore
import com.animesh.gullak.data.repository.FinanceRepository
import com.animesh.gullak.domain.model.FinancialSummary
import com.animesh.gullak.domain.model.Transaction
import com.animesh.gullak.domain.model.WeeklyTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "Friend",
    val currencySymbol: String = "₹",
    val summary: FinancialSummary = FinancialSummary(0.0, 0.0, 0.0, 0.0),
    val recentTransactions: List<Transaction> = emptyList(),
    val weeklyTrend: List<WeeklyTrend> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                prefsDataStore.userPreferences,
                repository.getAllTransactions()
            ) { prefs, transactions ->
                val summary = repository.getFinancialSummaryForMonth(YearMonth.now())
                val weekly = repository.getWeeklyTrend()
                _uiState.update {
                    it.copy(
                        userName = prefs.userName,
                        currencySymbol = prefs.currencySymbol,
                        summary = summary,
                        recentTransactions = transactions.take(5),
                        weeklyTrend = weekly,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }
}
