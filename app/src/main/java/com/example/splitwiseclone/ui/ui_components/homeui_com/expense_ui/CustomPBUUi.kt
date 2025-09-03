package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.splitwiseclone.ui_viewmodels.AddExpenseViewModel
import com.example.splitwiseclone.ui_viewmodels.PaidByViewModel

@Composable
fun CustomPBSUUi(
    navController: NavHostController,
    paidByViewModel: PaidByViewModel,
    addExpenseViewModel: AddExpenseViewModel
) {
    val participants by paidByViewModel.participants.collectAsState()
    val selectedPayerId by paidByViewModel.selectedPayerId.collectAsState()

    Column {
        // Top Bar
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Who paid?", modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = {
                val payer = participants.find { it.id == selectedPayerId }
                addExpenseViewModel.commitPayerSelection(
                    payers = listOf(selectedPayerId!!),
                    text = "Paid by ${payer?.name ?: "1 person"}"
                )
                navController.popBackStack()
            }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Default.Check, contentDescription = "Done")
            }
        }

        LazyColumn {
            items(participants) { participant ->
                Card(onClick = { paidByViewModel.selectSinglePayer(participant.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = participant.id == selectedPayerId,
                            onClick = { paidByViewModel.selectSinglePayer(participant.id) }
                        )
                        Text(participant.name)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "Other options",
            modifier = Modifier.clickable { navController.navigate("customPaidByMultipleUi") },
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CustomPaidByMultipleUi(
    navController: NavHostController,
    paidByViewModel: PaidByViewModel,
    addExpenseViewModel: AddExpenseViewModel
) {
    val participants by paidByViewModel.participants.collectAsState()
    val payerAmounts by paidByViewModel.payerAmounts.collectAsState()

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Enter paid amounts", modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = {
                val finalPayers = payerAmounts.filter {
                    it.value.isNotBlank() && (it.value.toDoubleOrNull() ?: 0.0) > 0.0
                }
                addExpenseViewModel.commitPayerSelection(
                    payers = finalPayers.keys.toList(),
                    text = "Paid by ${finalPayers.size} people"
                )
                navController.popBackStack("addExpense", inclusive = false)
            }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Default.Check, contentDescription = "Done")
            }
        }

        LazyColumn {
            items(participants) { participant ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(participant.name, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = payerAmounts[participant.id] ?: "",
                        onValueChange = { amount -> paidByViewModel.updatePayerAmount(participant.id, amount) },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
    }
}