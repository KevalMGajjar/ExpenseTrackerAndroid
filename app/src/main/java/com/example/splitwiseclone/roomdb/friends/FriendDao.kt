package com.example.splitwiseclone.roomdb.friends

import android.icu.text.MessagePattern
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Update
    suspend fun updateFriend(friend: Friend)

    @Query("SELECT * FROM Friends ORDER BY id ASC")
    fun getAllFriends(): Flow<List<Friend>>

    @Query("DELETE FROM Friends")
    suspend fun deleteAllFriends()

    @Query("SELECT * FROM Friends WHERE friendId = :id")
    fun findFriendById(id: String): Flow<Friend?>

}