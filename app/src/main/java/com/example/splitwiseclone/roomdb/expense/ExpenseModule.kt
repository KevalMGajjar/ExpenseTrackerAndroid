package com.example.splitwiseclone.roomdb.expense

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpenseModule {

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context, expenseDbConvertor: ExpenseDbConvertor): ExpenseDataBase {
        return Room.databaseBuilder(
            context,
            ExpenseDataBase::class.java,
            "expense Database"
        ).addTypeConverter(expenseDbConvertor)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(db: ExpenseDataBase): ExpenseDao = db.getDao()

    @Provides
    fun provideExpenseRepository(expenseDao: ExpenseDao): ExpenseRepository {
        return ExpenseRepository(expenseDao)
    }
}