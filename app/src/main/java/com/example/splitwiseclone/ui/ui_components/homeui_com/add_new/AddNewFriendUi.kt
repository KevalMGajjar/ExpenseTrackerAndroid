package com.example.splitwiseclone.ui.ui_components.homeui_com.add_new

import android.content.Context
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.AddFriendViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddNewFriendUi(navHostController: NavHostController, currentUserViewModel: CurrentUserViewModel, friendApiViewModel: FriendApiViewModel, addFriendViewModel: AddFriendViewModel) {

    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.READ_CONTACTS)
    var contacts by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val selectedNumbers by addFriendViewModel.selectedNumbers.collectAsState()

    LaunchedEffect(permissionState.status.isGranted) {
        if(permissionState.status.isGranted) {
            contacts = readContacts(context)
        }else {
            permissionState.launchPermissionRequest()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(WindowInsets.statusBars.asPaddingValues())) {
        Row(modifier = Modifier.fillMaxWidth()) {
            CustomTopAppBarSearchFriend()
        }
        Row(modifier = Modifier.fillMaxWidth()) {

        }
        if (permissionState.status.isGranted) {
            Box {
                LazyColumn {
                    contacts.forEach { (name, numbers) ->
                        item {
                            Card(modifier = Modifier.padding(5.dp).fillMaxWidth()) {
                                Row {
                                    Column {
                                        Text(text = name, fontSize = 20.sp)
                                        Row {
                                            numbers.forEach { number ->
                                                Text(text = "$number ")
                                            }
                                        }
                                    }
                                    IconButton(onClick = {
                                        addFriendViewModel.toggleSelectedFriend(numbers[0])
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add new friend"
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
                FloatingActionButton(onClick = { if (selectedNumbers.isNotEmpty()) {friendApiViewModel.addNewFriend(
                    phoneNumbers = selectedNumbers,
                    currentUser = currentUser!!,
                    onSuccess = {navHostController.navigate("friendsUi") }
                )
                    addFriendViewModel.deleteSelectedNumbers()
                }else{
                    Toast.makeText(context, "Please selcted atleast one frined", Toast.LENGTH_SHORT).show()
                }}) {
                    Icon(imageVector = Icons.Filled.Add,
                        contentDescription = "ADD new friend")
                }
            }
        } else {
            Toast.makeText(context, "Permission is required", Toast.LENGTH_SHORT).show()
        }

    }

}

@Composable
fun CustomTopAppBarSearchFriend() {
    Text(text = "Add new Friend")
}

data class Contact(
    val name: String,
    val number: String
)

fun readContacts(context: Context): Map<String, List<String>> {
    val list = mutableListOf<Contact>()
    val resolver = context.contentResolver
    val uniqueContacts = mutableSetOf<String>()

    val cursor = resolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null, null, null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            val number = it.getString(phoneIndex)

            val key = "$name|$number"

            if(' ' !in number && key !in uniqueContacts && number.startsWith("+")){
                uniqueContacts.add(key)
                list.add(Contact(name, number))
            }
        }
    }
    return list.groupBy { it.name }.mapValues { entry -> entry.value.map { it.number } }
}



