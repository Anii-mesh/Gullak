package com.animesh.gullak.di

import android.content.Context
import androidx.room.Room
import com.animesh.gullak.data.local.FinanceDatabase
import com.animesh.gullak.data.local.dao.MonthlyBudgetDao
import com.animesh.gullak.data.local.dao.NoSpendChallengeDao
import com.animesh.gullak.data.local.dao.SavingsGoalDao
import com.animesh.gullak.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinanceDatabase =
        Room.databaseBuilder(
            context,
            FinanceDatabase::class.java,
            "finance_database"
        ).build()

    @Provides
    fun provideTransactionDao(db: FinanceDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideSavingsGoalDao(db: FinanceDatabase): SavingsGoalDao = db.savingsGoalDao()

    @Provides
    fun provideNoSpendChallengeDao(db: FinanceDatabase): NoSpendChallengeDao = db.noSpendChallengeDao()

    @Provides
    fun provideMonthlyBudgetDao(db: FinanceDatabase): MonthlyBudgetDao = db.monthlyBudgetDao()
}
