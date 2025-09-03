package com.example.splitwiseclone.roomdb.groups

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GroupModule {

    @Provides
    @Singleton
    fun provideConvertor(moshi: Moshi): GroupDbConvertor{
        return GroupDbConvertor(moshi)
    }

    @Provides
    @Singleton
    fun providesDataBase(@ApplicationContext context: Context, groupDbConvertor: GroupDbConvertor): GroupDataBase {
        return Room.databaseBuilder(
            context,
            GroupDataBase::class.java,
            "Group DataBase"
        ).addTypeConverter(groupDbConvertor)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(db: GroupDataBase): GroupDao = db.getDao()

}