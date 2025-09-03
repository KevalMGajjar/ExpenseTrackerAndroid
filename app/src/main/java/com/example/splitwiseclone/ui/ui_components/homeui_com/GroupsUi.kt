package com.example.splitwiseclone.ui.ui_components.homeui_com

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.roomdb.groups.Group
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.AddGroupMemberViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GroupsUi(navHostController: NavHostController, groupRoomViewModel: GroupRoomViewModel, groupApiViewModel: GroupApiViewModel, currentUserViewModel: CurrentUserViewModel, addGroupMemberViewModel: AddGroupMemberViewModel, groupViewModel: GroupViewModel) {

    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val groups by groupRoomViewModel.allGroups.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            Row {
                CustomGroupTopBar(navHostController)
            }
            Spacer(modifier = Modifier.height(5.dp))

            CustomGroupsLazyColumn(groups, navHostController, addGroupMemberViewModel, groupViewModel)
        }
}

@Composable
fun CustomGroupTopBar(navHostController: NavHostController){

    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
        IconButton(onClick = {navHostController.navigate("addNewGroupUi")}, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(imageVector = Icons.Default.AddCircle,
                contentDescription = "add new friend")
        }
        Spacer(modifier = Modifier.width(15.dp))
        Text(text="Groups", modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun CustomGroupsLazyColumn(groups: List<Group>, navHostController: NavHostController, addGroupMemberViewModel: AddGroupMemberViewModel, groupViewModel: GroupViewModel){

    LazyColumn {
        items(groups){ group ->
            GroupItem(group, navHostController, addGroupMemberViewModel, groupViewModel)
        }

    }
}

@Composable
fun GroupItem(group: Group, navHostController: NavHostController, addGroupMemberViewModel: AddGroupMemberViewModel, groupViewModel: GroupViewModel) {

    Card(onClick = {navHostController.navigate("groupsOuterProfileUi")
                    groupViewModel.storeCurrentGroup(group)}) {
        Row {
            AsyncImage(
                model = group.profilePicture,
                contentDescription = "group profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(96.dp)
                    .background(Color.Gray)
            )
            Column {
                Text(text = group.groupName)
            }

        }
    }

}