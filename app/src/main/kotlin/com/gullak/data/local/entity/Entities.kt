package com.animesh.gullak.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // TransactionType name
    val category: String, // TransactionCategory name
    val date: String, // ISO date string yyyy-MM-dd
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: String?, // nullable ISO date
    val emoji: String = "🎯",
    val isCompleted: Boolean = false
)

@Entity(tableName = "no_spend_challenges")
data class NoSpendChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: String,
    val durationDays: Int,
    val currentStreak: Int = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val limitAmount: Double,
    val month: Int,
    val year: Int
)
