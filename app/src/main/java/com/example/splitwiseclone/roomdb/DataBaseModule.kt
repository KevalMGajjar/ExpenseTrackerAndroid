package com.example.splitwiseclone.roomdb

import android.content.Context
import androidx.room.Room
import com.example.splitwiseclone.roomdb.converters.GroupDbConvertor
import com.example.splitwiseclone.roomdb.converters.ExpenseDbConvertor
import com.example.splitwiseclone.roomdb.dao.CurrentUserDao
import com.example.splitwiseclone.roomdb.dao.ExpenseDao
import com.example.splitwiseclone.roomdb.dao.FriendDao
import com.example.splitwiseclone.roomdb.dao.GroupDao
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGroupDbConvertor(moshi: Moshi): GroupDbConvertor {
        return GroupDbConvertor(moshi)
    }

    @Provides
    @Singleton
    fun provideExpenseDbConvertor(moshi: Moshi): ExpenseDbConvertor {
        return ExpenseDbConvertor(moshi)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        groupDbConvertor: GroupDbConvertor,
        expenseDbConvertor: ExpenseDbConvertor
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "splitwise_app.db"
        )
            .addTypeConverter(groupDbConvertor)
            .addTypeConverter(expenseDbConvertor)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFriendDao(db: AppDatabase): FriendDao = db.friendDao()

    @Provides
    @Singleton
    fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()

    @Provides
    @Singleton
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideCurrentUserDao(db: AppDatabase): CurrentUserDao = db.currentUserDao()
}