package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.FriendsUiViewModel

@Composable
fun FriendSettingsUi(navHostController: NavHostController, friendsUiViewModel: FriendsUiViewModel, friendApiViewModel: FriendApiViewModel, currentUserViewModel: CurrentUserViewModel, friendsRoomViewModel: FriendsRoomViewModel) {

    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val currentFriend by friendsUiViewModel.selectedFriend.collectAsState()

    Column {
        Row {
            AsyncImage(
                model = currentFriend?.profilePic,
                contentDescription = "friend profile photo",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
                    .background(Color.Gray)
            )
            Column {
                Text(text = currentFriend?.username ?: "", fontWeight = FontWeight.Bold)
                Text(text = currentFriend?.email ?: "")
            }
        }
        Row {
            Button(onClick = {
                friendApiViewModel.deleteFriend(
                    currentUser?.currentUserId ?: "",
                    currentFriend?.friendId ?: "",
                    onSuccess = {
                        friendsRoomViewModel.deleteFriend(
                            currentFriend!!,
                            onSuccess = { navHostController.navigate("friendsUi") })
                    }
                )
            }) {
                Text(text = "Delete Friend")
            }
        }
    }
}
