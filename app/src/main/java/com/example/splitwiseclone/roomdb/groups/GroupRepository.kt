package com.example.splitwiseclone.roomdb.groups

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GroupRepository @Inject constructor(
    private val dao: GroupDao
) {

    val allGroups: Flow<List<Group>> = dao.getAllGroups()

    suspend fun insertGroup(group: Group){
        dao.insertGroup(group)
    }

    suspend fun deleteGroup(group: Group){
        dao.deleteGroup(group)
    }

    suspend fun updateGroup(group: Group){
        dao.updateGroup(group)
    }

    suspend fun deleteAllGroups(){
        dao.deleteAllGroups()
    }
}