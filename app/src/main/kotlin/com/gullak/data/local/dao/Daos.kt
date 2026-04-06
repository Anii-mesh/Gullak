package com.animesh.gullak.data.local.dao

import androidx.room.*
import com.animesh.gullak.data.local.entity.MonthlyBudgetEntity
import com.animesh.gullak.data.local.entity.NoSpendChallengeEntity
import com.animesh.gullak.data.local.entity.SavingsGoalEntity
import com.animesh.gullak.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE (note LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY date DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalIncomeForPeriod(startDate: String, endDate: String): Double?

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalExpensesForPeriod(startDate: String, endDate: String): Double?

    @Query("""
        SELECT category, SUM(amount) as total FROM transactions
        WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getCategorySpendingForPeriod(startDate: String, endDate: String): Flow<List<CategorySum>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

data class CategorySum(
    val category: String,
    val total: Double
)

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, id DESC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)
}

@Dao
interface NoSpendChallengeDao {

    @Query("SELECT * FROM no_spend_challenges WHERE isActive = 1 LIMIT 1")
    fun getActiveChallenge(): Flow<NoSpendChallengeEntity?>

    @Query("SELECT * FROM no_spend_challenges ORDER BY id DESC")
    fun getAllChallenges(): Flow<List<NoSpendChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: NoSpendChallengeEntity): Long

    @Update
    suspend fun updateChallenge(challenge: NoSpendChallengeEntity)

    @Query("UPDATE no_spend_challenges SET isActive = 0 WHERE id != :keepId")
    suspend fun deactivateOthers(keepId: Long)
}

@Dao
interface MonthlyBudgetDao {

    @Query("SELECT * FROM monthly_budgets WHERE month = :month AND year = :year")
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<MonthlyBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: MonthlyBudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: MonthlyBudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: MonthlyBudgetEntity)
}
