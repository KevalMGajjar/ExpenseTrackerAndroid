package com.example.splitwiseclone.roomdb.groups

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Update
    suspend fun updateGroup(group: Group)

    @Query("SELECT * FROM `Groups` ORDER BY id ASC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("DELETE FROM `Groups`")
    suspend fun deleteAllGroups()

    @Query("SELECT * FROM 'Groups' WHERE id = :id")
    fun getGroupById(id: String): Flow<Group>
}