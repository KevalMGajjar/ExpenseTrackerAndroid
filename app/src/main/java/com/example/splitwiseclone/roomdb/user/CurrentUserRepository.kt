package com.example.splitwiseclone.roomdb.user

import com.example.splitwiseclone.roomdb.friends.Friend
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CurrentUserRepository @Inject constructor(
    private val dao: CurrentUserDao
) {

    val currentUser: Flow<CurrentUser?> = dao.getUser()

    suspend fun insertUser(currentUser: CurrentUser){
        dao.insertUser(currentUser)
    }

    suspend fun updateUser(currentUser: CurrentUser){
        dao.updateUser(currentUser)
    }

    suspend fun deleteCurrentUser() {
        dao.deleteCurrentUser()
    }
}