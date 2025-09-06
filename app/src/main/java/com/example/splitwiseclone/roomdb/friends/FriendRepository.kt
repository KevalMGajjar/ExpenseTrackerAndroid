package com.example.splitwiseclone.roomdb.friends

import android.util.Log
import com.example.splitwiseclone.roomdb.dao.FriendDao
import com.example.splitwiseclone.roomdb.entities.Friend
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FriendRepository @Inject constructor(
    private val dao: FriendDao
) {

    val allFriends: Flow<List<Friend>> = dao.getAllFriends()

    suspend fun insertFriend(friend: Friend){
        dao.insertFriend(friend)
    }

    suspend fun deleteFriend(friend: Friend){
        dao.deleteFriend(friend)

    }

    suspend fun updateFriend(friend: Friend){
        dao.updateFriend(friend)

    }

    suspend fun deleteAllFriends() {
        Log.d("FriendRepo", "Deleting all friends")
        dao.deleteAllFriends()
        Log.d("FriendRepo", "All friends deleted")
    }

    suspend fun findFriendById(id: String): Flow<Friend?>{
       return dao.findFriendById(id)
    }

}