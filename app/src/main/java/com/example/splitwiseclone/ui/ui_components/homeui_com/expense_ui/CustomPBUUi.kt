package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.splitwiseclone.ui_viewmodels.PaidByViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPBUUi(
    navController: NavHostController,
    parentEntry: NavBackStackEntry
) {
    val paidByViewModel: PaidByViewModel = hiltViewModel(parentEntry)
    val participants by paidByViewModel.participants.collectAsState()
    val selectedPayerId by paidByViewModel.selectedPayerId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Who paid?", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        // Pass the result back to the previous screen
                        val result = mapOf("payerIds" to listOfNotNull(selectedPayerId))
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("payer_result", result)
                        navController.popBackStack()
                    }) { Icon(Icons.Default.Check, "Done") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(participants) { participant ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { paidByViewModel.selectSinglePayer(participant.id) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = participant.id == selectedPayerId, onClick = { paidByViewModel.selectSinglePayer(participant.id) })
                        Spacer(Modifier.width(16.dp))
                        Text(participant.name)
                    }
                }
            }
            TextButton(onClick = { navController.navigate("customPaidByMultipleUi") }) {
                Text("Multiple people paid")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPaidByMultipleUi(
    navController: NavHostController,
    parentEntry: NavBackStackEntry
) {
    val paidByViewModel: PaidByViewModel = hiltViewModel(parentEntry)
    val participants by paidByViewModel.participants.collectAsState()
    val payerAmounts by paidByViewModel.payerAmounts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multiple payers", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        val finalPayers = payerAmounts
                            .filter { it.value.isNotBlank() && (it.value.toDoubleOrNull() ?: 0.0) > 0.0 }
                            .keys.toList()

                        val result = mapOf("payerIds" to finalPayers)
                        navController.getBackStackEntry(navController.graph.startDestinationId)
                            .savedStateHandle
                            .set("payer_result", result)

                        navController.popBackStack(navController.graph.startDestinationId, false)
                    }) { Icon(Icons.Default.Check, "Done") }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(participants) { participant ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(participant.name, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = payerAmounts[participant.id] ?: "",
                        onValueChange = { amount -> paidByViewModel.updatePayerAmount(participant.id, amount) },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹") }
                    )
                }
            }
        }
    }
}