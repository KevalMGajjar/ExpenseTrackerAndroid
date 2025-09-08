package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.splitwiseclone.ui_viewmodels.SplitOptionsViewModel
import com.example.splitwiseclone.ui_viewmodels.SplitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSplitScreen(
    navController: NavHostController,
    parentEntry: NavBackStackEntry
) {
    val splitOptionsViewModel: SplitOptionsViewModel = hiltViewModel(parentEntry)

    val participants by splitOptionsViewModel.participants.collectAsState()
    val splitType by splitOptionsViewModel.splitType.collectAsState()
    val unequalAmounts by splitOptionsViewModel.unequalSplitAmounts.collectAsState()
    val amountSplit by splitOptionsViewModel.amountSplit.collectAsState()
    val amountLeft by splitOptionsViewModel.amountLeft.collectAsState()
    val totalAmount by splitOptionsViewModel.totalAmount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjust split", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("split_result", splitOptionsViewModel.finalSplits)
                        navController.popBackStack()
                    }) { Icon(Icons.Default.Check, "Done") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(selected = splitType == SplitType.EQUALLY, onClick = { splitOptionsViewModel.selectSplitType(SplitType.EQUALLY) }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)) { Text("Equally") }
                SegmentedButton(selected = splitType == SplitType.UNEQUALLY, onClick = { splitOptionsViewModel.selectSplitType(SplitType.UNEQUALLY) }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)) { Text("Unequally") }
            }

            LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(participants) { participant ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = true, onCheckedChange = null)
                        Text(participant.name, modifier = Modifier.weight(1f))
                        if (splitType == SplitType.UNEQUALLY) {
                            OutlinedTextField(
                                value = unequalAmounts[participant.id] ?: "",
                                onValueChange = { amount -> splitOptionsViewModel.updateUnequalAmount(participant.id, amount) },
                                prefix = { Text("₹") }
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Total: $$amountSplit of $$totalAmount")
                    Spacer(Modifier.weight(1f))
                    val amountLeftColor = if (amountLeft.toDouble() == 0.0) Color.Green else MaterialTheme.colorScheme.error
                    Text("$$amountLeft left", color = amountLeftColor)
                }
            }
        }
    }
}