package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.SettleUpViewModel

val SettleUpButtonColor = Color(0xFF4DB6AC)
val KeypadBackgroundColor = Color(0xFFF1F3F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    navController: NavHostController,
    viewModel: SettleUpViewModel = hiltViewModel(),
    currentUserViewModel: CurrentUserViewModel = hiltViewModel()
) {
    val friend by viewModel.friend.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* No title */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                friend?.let {
                    AsyncImage(
                        model = it.profilePic,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "To: ${it.username}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Change",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text("Type the Amount to be settled.", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                AmountDisplay(amount)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Button(
                    onClick = {
                        currentUser?.let { user ->
                            viewModel.settlePayment(user) {
                                // Corrected function name
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SettleUpButtonColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp)
                ) {
                    Text("SETTLE PAYMENT", fontSize = 16.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                NumericKeypad(onKeyPress = viewModel::onKeypadPress)
            }
        }
    }
}

@Composable
fun AmountDisplay(amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Clear, contentDescription = "Decrement", tint = Color.Gray)
        Spacer(modifier = Modifier.width(24.dp))
        Text(text = "$", fontSize = 36.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = amount, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(24.dp))
        Icon(Icons.Default.Add, contentDescription = "Increment", tint = Color.Gray)
    }
}

@Composable
fun NumericKeypad(onKeyPress: (String) -> Unit) {
    val keys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        ".", "0", "x"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeypadBackgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.chunked(3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    KeypadButton(key, onClick = { onKeyPress(key) })
                }
            }
        }
    }
}

@Composable
fun KeypadButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Medium)
    }
}



