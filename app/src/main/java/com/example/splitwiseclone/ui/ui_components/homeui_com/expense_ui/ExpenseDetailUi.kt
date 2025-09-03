package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.splitwiseclone.ui_viewmodels.ExpenseDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailUi(
    navController: NavHostController,
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val expense by viewModel.expense.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(expense?.description ?: "Expense Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        // Safely navigate only if the expense ID exists
                        expense?.id?.let {
                            navController.navigate("expenseEdit/$it")
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        // This is a great pattern for handling loading/success states
        expense?.let { exp ->
            LazyColumn(modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)) {
                item {
                    Text("Total Amount: ${exp.totalExpense}", style = MaterialTheme.typography.headlineSmall)
                    Text("Added by: You on ${exp.expenseDate}", style = MaterialTheme.typography.bodySmall)
                    Text("Paid by: ${exp.paidByUserIds.size} people", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Split Details:", style = MaterialTheme.typography.titleMedium)
                }

                items(exp.splits) { split ->
                    // Here you would look up the usernames from the IDs
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("${split.owedByUserId} owes ${split.owedToUserId}", modifier = Modifier.weight(1f))
                        Text("₹${split.owedAmount}") // Added a currency symbol for context
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
                        viewModel.deleteExpense {
                            navController.popBackStack()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}