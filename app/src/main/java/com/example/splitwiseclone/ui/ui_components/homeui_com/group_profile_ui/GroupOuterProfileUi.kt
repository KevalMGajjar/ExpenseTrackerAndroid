package com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.ui_viewmodels.GroupViewModel

@Composable
fun GroupOuterProfileUi(navHostController: NavHostController, groupViewModel: GroupViewModel) {
    val currentGroup by groupViewModel.currentGroup.collectAsState()
    val members = currentGroup.members
    Column {
        Row {
            IconButton(onClick = { navHostController.navigate("groupSettingsUi")}) {
                Icon(imageVector = Icons.Default.Settings,
                    contentDescription = "Settings")
            }
        }
        Row {
            Button(onClick = {navHostController.navigate("addNewGroupMemberUi")}) {
                Text(text="Add Members")
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = members?.toList() ?: emptyList()){ member ->
                Card {
                    Row {
                        AsyncImage(
                            model = member.profilePicture,
                            contentDescription = "friend profile pic",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = member.username ?: "Member")
                            Text(text = member.email ?: "Email")
                        }
                    }
                }
            }
        }
    }

}