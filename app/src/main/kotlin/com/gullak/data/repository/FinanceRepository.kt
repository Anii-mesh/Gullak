package com.animesh.gullak.data.repository

import com.animesh.gullak.data.local.dao.MonthlyBudgetDao
import com.animesh.gullak.data.local.dao.NoSpendChallengeDao
import com.animesh.gullak.data.local.dao.SavingsGoalDao
import com.animesh.gullak.data.local.dao.TransactionDao
import com.animesh.gullak.data.local.toDomain
import com.animesh.gullak.data.local.toEntity
import com.animesh.gullak.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val noSpendChallengeDao: NoSpendChallengeDao,
    private val monthlyBudgetDao: MonthlyBudgetDao
) {

    // ── Transactions ──────────────────────────────────────────────────────────

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    fun getTransactionsBetweenDates(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetweenDates(start.toString(), end.toString())
            .map { list -> list.map { it.toDomain() } }

    fun searchTransactions(query: String): Flow<List<Transaction>> =
        transactionDao.searchTransactions(query).map { list -> list.map { it.toDomain() } }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(type.name).map { list -> list.map { it.toDomain() } }

    fun getTransactionsByCategory(category: TransactionCategory): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(category.name)
            .map { list -> list.map { it.toDomain() } }

    fun getCategorySpendingForMonth(yearMonth: YearMonth): Flow<List<CategorySpending>> {
        val start = yearMonth.atDay(1).toString()
        val end = yearMonth.atEndOfMonth().toString()
        return transactionDao.getCategorySpendingForPeriod(start, end).map { list ->
            val total = list.sumOf { it.total }
            list.map { item ->
                CategorySpending(
                    category = TransactionCategory.valueOf(item.category),
                    amount = item.total,
                    percentage = if (total > 0) (item.total / total * 100).toFloat() else 0f
                )
            }
        }
    }

    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction.toEntity())

    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction.toEntity())

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction.toEntity())

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    suspend fun getFinancialSummaryForMonth(yearMonth: YearMonth): FinancialSummary {
        val start = yearMonth.atDay(1).toString()
        val end = yearMonth.atEndOfMonth().toString()
        val income = transactionDao.getTotalIncomeForPeriod(start, end) ?: 0.0
        val expenses = transactionDao.getTotalExpensesForPeriod(start, end) ?: 0.0
        val balance = income - expenses
        val savingsRate = if (income > 0) ((income - expenses) / income * 100) else 0.0
        return FinancialSummary(
            totalBalance = balance,
            totalIncome = income,
            totalExpenses = expenses,
            savingsRate = savingsRate
        )
    }

    suspend fun getWeeklyTrend(): List<WeeklyTrend> {
        val today = LocalDate.now()
        return (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val start = date.toString()
            val end = date.toString()
            val amount = transactionDao.getTotalExpensesForPeriod(start, end) ?: 0.0
            WeeklyTrend(
                dayLabel = date.dayOfWeek.name.take(3),
                amount = amount
            )
        }
    }

    // ── Savings Goals ─────────────────────────────────────────────────────────

    fun getAllGoals(): Flow<List<SavingsGoal>> =
        savingsGoalDao.getAllGoals().map { list -> list.map { it.toDomain() } }

    suspend fun insertGoal(goal: SavingsGoal): Long =
        savingsGoalDao.insertGoal(goal.toEntity())

    suspend fun updateGoal(goal: SavingsGoal) =
        savingsGoalDao.updateGoal(goal.toEntity())

    suspend fun deleteGoal(goal: SavingsGoal) =
        savingsGoalDao.deleteGoal(goal.toEntity())

    // ── No Spend Challenge ───────────────────────────────────────────────────

    fun getActiveChallenge(): Flow<NoSpendChallenge?> =
        noSpendChallengeDao.getActiveChallenge().map { it?.toDomain() }

    suspend fun startChallenge(challenge: NoSpendChallenge): Long {
        val id = noSpendChallengeDao.insertChallenge(challenge.toEntity())
        noSpendChallengeDao.deactivateOthers(id)
        return id
    }

    suspend fun updateChallenge(challenge: NoSpendChallenge) =
        noSpendChallengeDao.updateChallenge(challenge.toEntity())

    // ── Monthly Budgets ──────────────────────────────────────────────────────

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<MonthlyBudget>> =
        monthlyBudgetDao.getBudgetsForMonth(month, year)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insertBudget(budget: MonthlyBudget): Long =
        monthlyBudgetDao.insertBudget(budget.toEntity())

    suspend fun deleteBudget(budget: MonthlyBudget) =
        monthlyBudgetDao.deleteBudget(budget.toEntity())
}
