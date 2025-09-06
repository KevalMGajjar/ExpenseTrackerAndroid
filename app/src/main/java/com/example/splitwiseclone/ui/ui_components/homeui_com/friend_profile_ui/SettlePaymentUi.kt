package com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui_viewmodels.PaymentDirection
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
    val paymentDirection by viewModel.paymentDirection.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settle up") },
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
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                friend?.let {
                    PaymentDirectionSelector(
                        userName = "You",
                        userImageUrl = currentUser?.profileUrl ?: "",
                        friendName = it.username ?: "Friend",
                        friendImageUrl = it.profilePic,
                        direction = paymentDirection,
                        onDirectionChange = { newDirection -> viewModel.setPaymentDirection(newDirection) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
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
                                navController.navigate("friendsUi")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SettleUpButtonColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                    enabled = (amount.toDoubleOrNull() ?: 0.0) > 0.0
                ) {
                    Text("RECORD PAYMENT", fontSize = 16.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                NumericKeypad(onKeyPress = viewModel::onKeypadPress)
            }
        }
    }
}

@Composable
fun PaymentDirectionSelector(
    userName: String, userImageUrl: String,
    friendName: String, friendImageUrl: String,
    direction: PaymentDirection?,
    onDirectionChange: (PaymentDirection) -> Unit
) {
    // FIX: The rotation of the arrow is the ONLY thing that should change based on the direction state.
    // The positions of the user chips are now static.
    val rotation by animateFloatAsState(
        targetValue = if (direction == PaymentDirection.USER_PAYS_FRIEND) 0f else 180f,
        animationSpec = tween(300),
        label = "ArrowRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // A single click toggles the direction
                    val newDirection = if (direction == PaymentDirection.USER_PAYS_FRIEND) PaymentDirection.FRIEND_PAYS_USER else PaymentDirection.USER_PAYS_FRIEND
                    onDirectionChange(newDirection)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // FIX: The user on the left is ALWAYS "You".
            UserInfoChip(name = userName, imageUrl = userImageUrl)

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Pays",
                modifier = Modifier.rotate(rotation).size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // FIX: The user on the right is ALWAYS the friend.
            UserInfoChip(name = friendName, imageUrl = friendImageUrl)
        }
    }
}

// A simple, reusable chip to display a user's avatar and name
@Composable
fun UserInfoChip(name: String, imageUrl: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = imageUrl, contentDescription = name,
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun AmountDisplay(amount: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Enter the amount", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$", fontSize = 36.sp, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
            Text(text = amount, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }
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
        modifier = Modifier.fillMaxWidth().background(KeypadBackgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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