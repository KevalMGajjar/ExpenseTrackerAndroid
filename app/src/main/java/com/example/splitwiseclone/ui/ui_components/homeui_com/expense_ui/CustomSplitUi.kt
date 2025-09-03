package com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.splitwiseclone.ui_viewmodels.AddExpenseViewModel
import com.example.splitwiseclone.ui_viewmodels.SplitOptionsViewModel
import com.example.splitwiseclone.ui_viewmodels.SplitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSplitUi(
    navController: NavHostController,
    splitOptionsViewModel: SplitOptionsViewModel,
    addExpenseViewModel: AddExpenseViewModel
) {
    val participants by splitOptionsViewModel.participants.collectAsState()
    val splitType by splitOptionsViewModel.splitType.collectAsState()
    val unequalAmounts by splitOptionsViewModel.unequalSplitAmounts.collectAsState()
    val amountSplit by splitOptionsViewModel.amountSplit.collectAsState()
    val amountLeft by splitOptionsViewModel.amountLeft.collectAsState()
    val totalAmount by splitOptionsViewModel.totalAmount.collectAsState()

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Adjust split", modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = {
                val splitText = if(splitType == SplitType.EQUALLY) "Split Equally" else "Split Unequally"
                addExpenseViewModel.commitSplitSelection(
                    splits = splitOptionsViewModel.finalSplits,
                    text = splitText
                )
                navController.popBackStack()
            }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Default.Check, contentDescription = "Done")
            }
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = splitType == SplitType.EQUALLY,
                onClick = { splitOptionsViewModel.selectSplitType(SplitType.EQUALLY) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Equally") }
            SegmentedButton(
                selected = splitType == SplitType.UNEQUALLY,
                onClick = { splitOptionsViewModel.selectSplitType(SplitType.UNEQUALLY) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Unequally") }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(participants) { participant ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = null) // Always checked
                    Text(participant.name, modifier = Modifier.weight(1f))
                    if (splitType == SplitType.UNEQUALLY) {
                        OutlinedTextField(
                            value = unequalAmounts[participant.id] ?: "",
                            onValueChange = { amount -> splitOptionsViewModel.updateUnequalAmount(participant.id, amount) }
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("$amountSplit of $totalAmount")
            Spacer(Modifier.weight(1f))
            Text("$amountLeft left")
        }
    }
}