package com.example.splitwiseclone.roomdb.friends

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
object FriendsDataModule {

    @Provides
    @Singleton
    fun providesDataBase(@ApplicationContext context: Context): FriendsDataBase {
        return Room.databaseBuilder(
            context,
            FriendsDataBase::class.java,
            "Friends DataBase"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideDao(db: FriendsDataBase): FriendDao = db.getDao()
}