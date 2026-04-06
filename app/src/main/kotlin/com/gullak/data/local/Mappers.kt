package com.animesh.gullak.data.local

import com.animesh.gullak.data.local.entity.MonthlyBudgetEntity
import com.animesh.gullak.data.local.entity.NoSpendChallengeEntity
import com.animesh.gullak.data.local.entity.SavingsGoalEntity
import com.animesh.gullak.data.local.entity.TransactionEntity
import com.animesh.gullak.domain.model.MonthlyBudget
import com.animesh.gullak.domain.model.NoSpendChallenge
import com.animesh.gullak.domain.model.SavingsGoal
import com.animesh.gullak.domain.model.Transaction
import com.animesh.gullak.domain.model.TransactionCategory
import com.animesh.gullak.domain.model.TransactionType
import java.time.LocalDate

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = TransactionCategory.valueOf(category),
    date = LocalDate.parse(date),
    note = note,
    createdAt = createdAt
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category.name,
    date = date.toString(),
    note = note,
    createdAt = createdAt
)

fun SavingsGoalEntity.toDomain() = SavingsGoal(
    id = id,
    title = title,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    deadline = deadline?.let { LocalDate.parse(it) },
    emoji = emoji,
    isCompleted = isCompleted
)

fun SavingsGoal.toEntity() = SavingsGoalEntity(
    id = id,
    title = title,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    deadline = deadline?.toString(),
    emoji = emoji,
    isCompleted = isCompleted
)

fun NoSpendChallengeEntity.toDomain() = NoSpendChallenge(
    id = id,
    startDate = LocalDate.parse(startDate),
    durationDays = durationDays,
    currentStreak = currentStreak,
    isActive = isActive
)

fun NoSpendChallenge.toEntity() = NoSpendChallengeEntity(
    id = id,
    startDate = startDate.toString(),
    durationDays = durationDays,
    currentStreak = currentStreak,
    isActive = isActive
)

fun MonthlyBudgetEntity.toDomain() = MonthlyBudget(
    id = id,
    category = TransactionCategory.valueOf(category),
    limitAmount = limitAmount,
    month = month,
    year = year
)

fun MonthlyBudget.toEntity() = MonthlyBudgetEntity(
    id = id,
    category = category.name,
    limitAmount = limitAmount,
    month = month,
    year = year
)
