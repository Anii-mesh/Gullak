package com.animesh.gullak.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.animesh.gullak.data.local.dao.MonthlyBudgetDao
import com.animesh.gullak.data.local.dao.NoSpendChallengeDao
import com.animesh.gullak.data.local.dao.SavingsGoalDao
import com.animesh.gullak.data.local.dao.TransactionDao
import com.animesh.gullak.data.local.entity.MonthlyBudgetEntity
import com.animesh.gullak.data.local.entity.NoSpendChallengeEntity
import com.animesh.gullak.data.local.entity.SavingsGoalEntity
import com.animesh.gullak.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        SavingsGoalEntity::class,
        NoSpendChallengeEntity::class,
        MonthlyBudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun noSpendChallengeDao(): NoSpendChallengeDao
    abstract fun monthlyBudgetDao(): MonthlyBudgetDao
}
