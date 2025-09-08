package com.example.splitwiseclone.ui.ui_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUi(
    navHostController: NavHostController,
    currentUserViewModel: CurrentUserViewModel = hiltViewModel(),
    userApiViewModel: UserApiViewModel = hiltViewModel()
) {
    val currentUser by currentUserViewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        currentUser?.let { user ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                UserInfoHeader(user = user, onEditClick = { navHostController.navigate("editProfile") })
                Spacer(modifier = Modifier.height(32.dp))

                SettingsActionRow(
                    title = "Logout",
                    icon = Icons.Default.ExitToApp,
                    isDestructive = true,
                    onClick = { showLogoutDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsActionRow(
                    title = "Delete Account",
                    icon = Icons.Default.Delete,
                    isDestructive = true,
                    onClick = { showDeleteDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Logout?",
            text = "Are you sure you want to log out?",
            confirmButtonText = "Logout",
            onConfirm = {
                showLogoutDialog = false
                currentUserViewModel.logoutCurrentUser {
                    navHostController.navigate("welcome") {
                        popUpTo(navHostController.graph.startDestinationId) { inclusive = true }
                    }
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Account?",
            text = "Are you sure you want to permanently delete your account? This action cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                currentUser?.let { user ->
                    userApiViewModel.deleteUserAccount(user.currentUserId) {
                        currentUserViewModel.logoutCurrentUser {
                            navHostController.navigate("welcome") {
                                popUpTo(navHostController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun UserInfoHeader(user: CurrentUser, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = user.profileUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier.size(96.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Text(user.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Edit Profile")
        }
    }
}

@Composable
private fun SettingsActionRow(title: String, icon: ImageVector, isDestructive: Boolean = false, onClick: () -> Unit) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = contentColor)
        Spacer(Modifier.width(16.dp))
        Text(title, color = contentColor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}