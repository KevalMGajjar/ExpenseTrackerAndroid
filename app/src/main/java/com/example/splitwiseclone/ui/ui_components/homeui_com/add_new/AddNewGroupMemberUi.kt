package com.example.splitwiseclone.ui.ui_components.homeui_com.add_new

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.groups.Member
import com.example.splitwiseclone.ui_viewmodels.AddGroupMemberViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupViewModel

@Composable
fun AddNewGroupMemberUi(
    navHostController: NavHostController,
    friendsRoomViewModel: FriendsRoomViewModel,
    addGroupMemberViewModel: AddGroupMemberViewModel,
    groupApiViewModel: GroupApiViewModel,
    groupViewModel: GroupViewModel,
    groupRoomViewModel: GroupRoomViewModel
) {

    val friendsList by friendsRoomViewModel.allUser.collectAsState()
    val selectedFriends by addGroupMemberViewModel.selectedFriends.collectAsState()
    val isSelected by addGroupMemberViewModel.isSelected.collectAsState()
    val context = LocalContext.current
    val currentGroup by groupViewModel.currentGroup.collectAsState()
    val newMembers = currentGroup.members?.toMutableList()

    Column {
        Row {
            //Search Bar
        }
        Box {
            LazyColumn {
                items(friendsList) { friend ->
                    Card(
                        onClick = {
                            addGroupMemberViewModel.toggleSelectedFriend(friend)
                            if (friend in selectedFriends) {
                                addGroupMemberViewModel.setIsSelected(true)
                            }
                        },
                        colors = CardDefaults.cardColors(
                            contentColor = if (isSelected) Color.Blue else Color.White
                        )
                    ) {
                        Row {
                            AsyncImage(
                                model = friend.profilePic,
                                contentDescription = "friend profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )
                            Column {
                                friend.username?.let { Text(text = it) }
                                Spacer(modifier = Modifier.height(2.dp))
                                friend.phoneNumber?.let { Text(text = it) }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(onClick = {
                if (selectedFriends.isNotEmpty()) {
                    groupApiViewModel.addMembers(
                        selectedFriends,
                        currentGroup.id,
                        onSuccess = {
                            selectedFriends.forEach { friend ->
                                newMembers?.add(
                                    Member(
                                        userId = friend.friendId,
                                        role = "member",
                                        username = friend.username!!,
                                        email = friend.email,
                                        profilePicture = friend.profilePic
                                    )
                                )
                            }
                            groupRoomViewModel.updateGroup(
                                currentGroup,
                                onSuccess = { navHostController.navigate("groupsUi") })
                        })
                } else {
                    Toast.makeText(context, "Please select a Friend", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add members"
                )
            }
        }
    }
}