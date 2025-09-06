package com.example.splitwiseclone.roomdb.groups

import com.example.splitwiseclone.roomdb.dao.GroupDao
import com.example.splitwiseclone.roomdb.entities.Group
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

    fun getGroupById(id: String): Flow<Group> {
        return dao.getGroupById(id)
    }
}