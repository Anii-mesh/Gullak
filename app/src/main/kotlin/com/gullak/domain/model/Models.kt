package com.animesh.gullak.domain.model

import java.time.LocalDate

enum class TransactionType { INCOME, EXPENSE }

enum class TransactionCategory(val label: String, val emoji: String) {
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚌"),
    SHOPPING("Shopping", "🛍️"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health", "💊"),
    BILLS("Bills", "💡"),
    EDUCATION("Education", "📚"),
    TRAVEL("Travel", "✈️"),
    SALARY("Salary", "💼"),
    FREELANCE("Freelance", "💻"),
    INVESTMENT("Investment", "📈"),
    GIFT("Gift", "🎁"),
    OTHER("Other", "📦")
}

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val date: LocalDate,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SavingsGoal(
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: LocalDate?,
    val emoji: String = "🎯",
    val isCompleted: Boolean = false
)

data class NoSpendChallenge(
    val id: Long = 0,
    val startDate: LocalDate,
    val durationDays: Int,
    val currentStreak: Int = 0,
    val isActive: Boolean = true
)

data class MonthlyBudget(
    val id: Long = 0,
    val category: TransactionCategory,
    val limitAmount: Double,
    val month: Int,
    val year: Int
)

data class FinancialSummary(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val savingsRate: Double
)

data class CategorySpending(
    val category: TransactionCategory,
    val amount: Double,
    val percentage: Float
)

data class WeeklyTrend(
    val dayLabel: String,
    val amount: Double
)
