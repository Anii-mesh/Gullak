package com.animesh.gullak.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animesh.gullak.data.datastore.UserPreferencesDataStore
import com.animesh.gullak.data.repository.FinanceRepository
import com.animesh.gullak.domain.model.NoSpendChallenge
import com.animesh.gullak.domain.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class GoalsUiState(
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val activeChallenge: NoSpendChallenge? = null,
    val currencySymbol: String = "₹",
    val isLoading: Boolean = true,
    val showAddGoalDialog: Boolean = false,
    val showChallengeDialog: Boolean = false,
    val monthlyExpenses: Double = 0.0
)

data class AddGoalForm(
    val title: String = "",
    val targetAmount: String = "",
    val emoji: String = "🎯",
    val hasDeadline: Boolean = false,
    val deadline: LocalDate = LocalDate.now().plusMonths(3)
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val _addGoalForm = MutableStateFlow(AddGoalForm())
    val addGoalForm: StateFlow<AddGoalForm> = _addGoalForm.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllGoals(),
                repository.getActiveChallenge(),
                prefsDataStore.userPreferences
            ) { goals, challenge, prefs ->
                val now = YearMonth.now()
                val summary = repository.getFinancialSummaryForMonth(now)
                _uiState.update {
                    it.copy(
                        savingsGoals = goals,
                        activeChallenge = challenge,
                        currencySymbol = prefs.currencySymbol,
                        monthlyExpenses = summary.totalExpenses,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    // ── Add Goal ─────────────────────────────────────────────────────────────

    fun showAddGoalDialog() = _uiState.update { it.copy(showAddGoalDialog = true) }
    fun hideAddGoalDialog() {
        _uiState.update { it.copy(showAddGoalDialog = false) }
        _addGoalForm.value = AddGoalForm()
    }

    fun updateGoalTitle(t: String) = _addGoalForm.update { it.copy(title = t) }
    fun updateGoalAmount(a: String) = _addGoalForm.update { it.copy(targetAmount = a) }
    fun updateGoalEmoji(e: String) = _addGoalForm.update { it.copy(emoji = e) }
    fun toggleDeadline(b: Boolean) = _addGoalForm.update { it.copy(hasDeadline = b) }
    fun updateDeadline(d: LocalDate) = _addGoalForm.update { it.copy(deadline = d) }

    fun saveGoal() {
        val form = _addGoalForm.value
        val amount = form.targetAmount.toDoubleOrNull() ?: return
        if (form.title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoal(
                    title = form.title,
                    targetAmount = amount,
                    emoji = form.emoji,
                    deadline = if (form.hasDeadline) form.deadline else null
                )
            )
            hideAddGoalDialog()
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }

    fun addSavingsToGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val newAmount = (goal.savedAmount + amount).coerceAtMost(goal.targetAmount)
            val updated = goal.copy(
                savedAmount = newAmount,
                isCompleted = newAmount >= goal.targetAmount
            )
            repository.updateGoal(updated)
        }
    }

    // ── No Spend Challenge ───────────────────────────────────────────────────

    fun showChallengeDialog() = _uiState.update { it.copy(showChallengeDialog = true) }
    fun hideChallengeDialog() = _uiState.update { it.copy(showChallengeDialog = false) }

    fun startChallenge(durationDays: Int) {
        viewModelScope.launch {
            repository.startChallenge(
                NoSpendChallenge(
                    startDate = LocalDate.now(),
                    durationDays = durationDays,
                    currentStreak = 0
                )
            )
            hideChallengeDialog()
        }
    }

    fun markChallengeDay(challenge: NoSpendChallenge) {
        viewModelScope.launch {
            repository.updateChallenge(
                challenge.copy(currentStreak = challenge.currentStreak + 1)
            )
        }
    }

    fun endChallenge(challenge: NoSpendChallenge) {
        viewModelScope.launch {
            repository.updateChallenge(challenge.copy(isActive = false))
        }
    }
}
