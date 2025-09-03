package com.example.splitwiseclone.roomdb.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(currentUser: CurrentUser)

    @Update
    suspend fun updateUser(currentUser: CurrentUser)

    @Query("SELECT * FROM CurrentUser LIMIT 1;\n")
    fun getUser(): Flow<CurrentUser?>

    @Query("DELETE FROM CurrentUser")
    suspend fun deleteCurrentUser()
}