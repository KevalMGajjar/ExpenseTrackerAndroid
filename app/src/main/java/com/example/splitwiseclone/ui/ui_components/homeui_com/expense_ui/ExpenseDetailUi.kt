package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.ui_viewmodels.ExpenseDetailViewModel
import com.example.splitwiseclone.ui_viewmodels.ParticipantDetails
import com.example.splitwiseclone.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavHostController,
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        uiState.expense?.id?.let { navController.navigate("expenseEdit/$it") }
                    }) { Icon(Icons.Default.Edit, "Edit") }
                    IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, "Delete") }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.expense?.let { expense ->
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { ExpenseHeader(expense, uiState.userDetailsMap) }
                    item { PaidBySection(expense, uiState.userDetailsMap) }
                    item { SplitDetailsSection(expense, uiState.userDetailsMap) }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to permanently delete this expense?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense { navController.popBackStack() }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ExpenseHeader(expense: Expense, userMap: Map<String, ParticipantDetails>) {
    val creatorName = userMap[expense.createdById]?.name ?: "Someone"
    val formattedDate = expense.expenseDate.toDate()?.toFormattedString() ?: expense.expenseDate

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = getCategoryIcon(expense.description ?: ""),
            contentDescription = "Category",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(text = expense.description ?: "Expense", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = CurrencyUtils.formatCurrency(expense.totalExpense),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Added by $creatorName on $formattedDate",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun PaidBySection(expense: Expense, userMap: Map<String, ParticipantDetails>) {
    Card {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Paid by", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            expense.paidByUserIds.forEach { payerId ->
                val payerDetails = userMap[payerId]
                val amountPaid = expense.totalExpense / expense.paidByUserIds.size // Assuming equal payment for now
                DetailRow(
                    imageUrl = payerDetails?.profilePic ?: "",
                    name = payerDetails?.name ?: "Unknown",
                    detail = "paid ${CurrencyUtils.formatCurrency(amountPaid)}"
                )
            }
        }
    }
}

@Composable
fun SplitDetailsSection(expense: Expense, userMap: Map<String, ParticipantDetails>) {
    Card {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Split details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            expense.splits.forEach { split ->
                val owedBy = userMap[split.owedByUserId]?.name ?: "Someone"
                val owedTo = userMap[split.owedToUserId]?.name ?: "someone"
                DetailRow(
                    imageUrl = userMap[split.owedByUserId]?.profilePic ?: "",
                    name = owedBy,
                    detail = "owes $owedTo",
                    amount = split.owedAmount
                )
            }
        }
    }
}

@Composable
fun DetailRow(imageUrl: String, name: String, detail: String, amount: Double? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.SemiBold)
            Text(text = detail, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        amount?.let {
            Text(
                text = CurrencyUtils.formatCurrency(amount),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun getCategoryIcon(description: String): ImageVector {
    return when {
        "uber" in description.lowercase() -> Icons.Default.PlayArrow
        "grocer" in description.lowercase() -> Icons.Default.ShoppingCart
        "cinema" in description.lowercase() || "movie" in description.lowercase() -> Icons.Default.PlayArrow
        "present" in description.lowercase() || "gift" in description.lowercase() -> Icons.Default.PlayArrow
        else -> Icons.Default.Menu
    }
}

private fun String.toDate(): Date? = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
} catch (e: Exception) { null }

private fun Date.toFormattedString(): String = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(this)