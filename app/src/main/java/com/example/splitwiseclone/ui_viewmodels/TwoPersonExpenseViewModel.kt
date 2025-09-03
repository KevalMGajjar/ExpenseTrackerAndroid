package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import com.example.splitwiseclone.rest_api.SplitDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class TwoPersonExpenseViewModel @Inject constructor(

): ViewModel(){

    private var _selectedSplit = MutableStateFlow("1")
    val selectedSplit = _selectedSplit

    private var _selectedSplitText = MutableStateFlow("You Paid, Split Equally")
    val selectedSplitText = _selectedSplitText

    fun selectSplitText(splitText: String){
        _selectedSplitText.value = splitText
    }

    fun selectSplit(split: String){
        _selectedSplit.value = split
    }

}