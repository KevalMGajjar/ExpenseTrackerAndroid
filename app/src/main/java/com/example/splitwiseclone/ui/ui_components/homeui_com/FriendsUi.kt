package com.example.splitwiseclone.ui.ui_components.homeui_com

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.splitwiseclone.rest_api.api_viewmodels.ExpenseApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.FriendsUiViewModel


@Composable
fun FriendsUi(navHostController: NavHostController, friendsViewModel: FriendsRoomViewModel, currentUserViewModel: CurrentUserViewModel, friendApiViewModel: FriendApiViewModel, friendsUiViewModel: FriendsUiViewModel, expenseApiViewModel: ExpenseApiViewModel) {
    val owedAmount = 0
    val friends by friendsViewModel.allUser.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(WindowInsets.statusBars.asPaddingValues())) {
        Row {
            CustomTopBarFriends(navHostController)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)){
            Text(text="Owed Money: $owedAmount")
        }
        Spacer(modifier = Modifier.height(10.dp))
        FriendsSelectionUi(friends, navHostController, friendsUiViewModel)
    }
}

@Composable
fun CustomTopBarFriends(navHostController: NavHostController) {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
        IconButton(onClick = {navHostController.navigate("addNewFriendUi")}, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(imageVector = Icons.Default.AddCircle,
                contentDescription = "add new friend")
        }
        Spacer(modifier = Modifier.width(15.dp))
        Text(text="Friends", modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun FriendsSelectionUi(friends: List<Friend>, navHostController: NavHostController, friendsUiViewModel: FriendsUiViewModel) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Overall", "I owe", "Owns Me")

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        SingleChoiceSegmentedButton(
            selectedIndex = selectedIndex,
            options = options,
            onIndexSelected = { selectedIndex = it}
        )

        Spacer(modifier = Modifier.height(12.dp))

        val filteredFriends = when (selectedIndex) {
            1 -> friends.filter { it.balanceWithUser < 0 }
            2 -> friends.filter { it.balanceWithUser > 0 }
            else -> friends
        }

        CustomLazyFriendsList(items = filteredFriends, navHostController = navHostController, friendsUiViewModel = friendsUiViewModel)

    }
}

@Composable
fun SingleChoiceSegmentedButton(modifier: Modifier = Modifier, selectedIndex: Int, options: List<String>, onIndexSelected: (Int) -> Unit) {

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onIndexSelected(index) },
                selected = index == selectedIndex,
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun FriendItem(friend: Friend, navHostController: NavHostController, friendsUiViewModel: FriendsUiViewModel) {
    Card(onClick = { friendsUiViewModel.selectFriend(friend)
        navHostController.navigate("friendsOuterProfileUi")}) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            AsyncImage(
                model = friend.profilePic,
                contentDescription = "friend profile pic",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = friend.username ?: "Friend")
                Text(text = "Last Expense Date")
            }
            Column(horizontalAlignment = Alignment.End) {
                if (friend.balanceWithUser > 0) {
                    Text(text = "owes you")
                    Text(text = friend.balanceWithUser.toString())
                } else if (friend.balanceWithUser < 0) {
                    Text(text = "you owe money")
                    Text(text = friend.balanceWithUser.toString())
                } else {
                    Text(text = "Settled Up")
                }
            }

        }
    }
}

@Composable
fun CustomLazyFriendsList(items: List<Friend>, modifier: Modifier = Modifier, navHostController: NavHostController, friendsUiViewModel: FriendsUiViewModel) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    LazyColumn(modifier) {
        items(items) { item ->
            FriendItem(item, navHostController, friendsUiViewModel = friendsUiViewModel)
        }
    }

}




