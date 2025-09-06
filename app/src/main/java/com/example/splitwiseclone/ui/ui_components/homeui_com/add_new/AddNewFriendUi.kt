package com.example.splitwiseclone.ui.ui_components.homeui_com.add_new

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.AddFriendViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

data class ContactInfo(val name: String, val numbers: List<String>)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddNewFriendUi(
    navHostController: NavHostController,
    currentUserViewModel: CurrentUserViewModel,
    friendApiViewModel: FriendApiViewModel,
    addFriendViewModel: AddFriendViewModel
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.READ_CONTACTS)
    var contacts by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    val selectedNumbers by addFriendViewModel.selectedNumbers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var contactWithMultipleNumbers by remember { mutableStateOf<ContactInfo?>(null) }
    var userNotFoundNumber by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            contacts = readContacts(context)
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Friends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedNumbers.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        friendApiViewModel.addNewFriend(
                            phoneNumbers = selectedNumbers,
                            currentUser = currentUser!!,
                            onSuccess = { navHostController.navigate("friendsUi") { popUpTo("friendsUi") { inclusive = true } } },
                            onUserNotFound = { phoneNumber -> userNotFoundNumber = phoneNumber }
                        )
                        addFriendViewModel.deleteSelectedNumbers()
                    },
                    icon = { Icon(Icons.Default.Add, "Add Friends") },
                    text = { Text("Add Friend" + if (selectedNumbers.size > 1) "s" else "") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            if (permissionState.status.isGranted) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by name or number") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                val filteredContacts = contacts.filter { contact ->
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                            contact.numbers.any { it.contains(searchQuery) }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredContacts) { contact ->
                        ContactItem(
                            contact = contact,
                            isSelected = contact.numbers.any { it in selectedNumbers },
                            onContactClicked = {
                                if (contact.numbers.size > 1) {
                                    contactWithMultipleNumbers = contact
                                } else {
                                    addFriendViewModel.toggleSelectedFriend(contact.numbers.first())
                                }
                            }
                        )
                    }
                }

            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Contact permission is required to add friends.")
                }
            }
        }
    }

    // --- Dialogs ---
    if (contactWithMultipleNumbers != null) {
        SelectNumberDialog(
            contact = contactWithMultipleNumbers!!,
            onDismiss = { contactWithMultipleNumbers = null },
            onNumberSelected = { number ->
                addFriendViewModel.toggleSelectedFriend(number)
                contactWithMultipleNumbers = null
            }
        )
    }

    if (userNotFoundNumber != null) {
        UserNotFoundDialog(
            phoneNumber = userNotFoundNumber!!,
            onDismiss = { userNotFoundNumber = null },
            onInvite = {
                // TODO: Implement email/SMS invitation logic
                userNotFoundNumber = null
            }
        )
    }
}


@Composable
fun ContactItem(contact: ContactInfo, isSelected: Boolean, onContactClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onContactClicked)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = "Contact", modifier = Modifier.size(40.dp).clip(CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = contact.name, fontWeight = FontWeight.SemiBold)
                Text(text = contact.numbers.first(), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SelectNumberDialog(contact: ContactInfo, onDismiss: () -> Unit, onNumberSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a number for ${contact.name}") },
        text = {
            LazyColumn {
                items(contact.numbers) { number ->
                    Text(
                        text = number,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNumberSelected(number) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UserNotFoundDialog(phoneNumber: String, onDismiss: () -> Unit, onInvite: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User Not Found") },
        text = { Text("The user with the phone number $phoneNumber is not on Splitwise. Would you like to send them an invitation?") },
        confirmButton = {
            Button(onClick = onInvite) { Text("Invite") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun normalizePhoneNumber(number: String): String {
    val cleaned = number.replace(Regex("[^0-9+]"), "")
    return when {
        cleaned.startsWith("+91") && cleaned.length == 13 -> cleaned
        cleaned.startsWith("0") && cleaned.length == 11 -> "+91" + cleaned.substring(1)
        cleaned.length == 10 -> "+91$cleaned"
        else -> cleaned // Return as-is if it's an unknown format
    }
}

fun readContacts(context: Context): List<ContactInfo> {
    val contactMap = mutableMapOf<String, MutableSet<String>>()
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null, null,
        ContactsContract.Contacts.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        if (nameIndex == -1 || numberIndex == -1) {
            return emptyList()
        }

        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            val rawNumber = it.getString(numberIndex).replace("\\s".toRegex(), "")
            if (name != null && rawNumber != null) {
                val normalizedNumber = normalizePhoneNumber(rawNumber)
                contactMap.getOrPut(name) { mutableSetOf() }.add(normalizedNumber)
            }
        }
    }
    return contactMap.map { ContactInfo(it.key, it.value.toList()) }
}

