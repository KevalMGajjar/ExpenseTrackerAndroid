package com.example.splitwiseclone.roomdb.user

import android.content.Context
import androidx.room.Room
import com.example.splitwiseclone.roomdb.expense.ExpenseDao
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CurrentUserModule {

    @Provides
    @Singleton
    fun providesDataBase(@ApplicationContext context: Context): CurrentUserDataBase {
        return Room.databaseBuilder(
            context,
            CurrentUserDataBase::class.java,
            "Current User DataBase"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideDao(db: CurrentUserDataBase): CurrentUserDao = db.getDao()
}