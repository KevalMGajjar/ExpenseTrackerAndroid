package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.SplitDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class SplitType { EQUALLY, UNEQUALLY }

@HiltViewModel
class SplitOptionsViewModel @Inject constructor() : ViewModel() {

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants = _participants.asStateFlow()

    private val _totalAmount = MutableStateFlow(BigDecimal.ZERO)
    val totalAmount = _totalAmount.asStateFlow()

    private val _splitType = MutableStateFlow(SplitType.EQUALLY)
    val splitType = _splitType.asStateFlow()

    private val _unequalSplitAmounts = MutableStateFlow<Map<String, String>>(emptyMap())
    val unequalSplitAmounts = _unequalSplitAmounts.asStateFlow()
    val amountSplit: StateFlow<BigDecimal> = unequalSplitAmounts.map { amounts ->
        amounts.values.sumOf { it.toBigDecimalOrNull() ?: BigDecimal.ZERO }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BigDecimal.ZERO
    )

    val amountLeft: StateFlow<BigDecimal> = combine(amountSplit, totalAmount) { split, total ->
        total - split
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BigDecimal.ZERO
    )

    val finalSplits: List<SplitDto>
        get() {
            val payerId = _participants.value.firstOrNull()?.id ?: return emptyList()

            return if (_splitType.value == SplitType.EQUALLY) {
                val participantCount = _participants.value.size
                if (participantCount == 0) return emptyList()
                val equalAmount = _totalAmount.value.divide(BigDecimal(participantCount), 2, RoundingMode.HALF_UP)

                _participants.value.map { participant ->
                    SplitDto(
                        owedByUserId = participant.id,
                        owedAmount = equalAmount.toDouble(),
                        owedToUserId = payerId
                    )
                }
            } else {
                _unequalSplitAmounts.value.mapNotNull { (userId, amountStr) ->
                    val amount = amountStr.toBigDecimalOrNull() ?: return@mapNotNull null
                    SplitDto(
                        owedByUserId = userId,
                        owedAmount = amount.toDouble(),
                        owedToUserId = payerId
                    )
                }
            }
        }


    fun setInitialData(participants: List<Participant>, totalAmount: String) {
        if (_participants.value.isEmpty()) {
            _participants.value = participants
            _totalAmount.value = totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }
    }

    fun selectSplitType(type: SplitType) {
        _splitType.value = type
    }

    fun updateUnequalAmount(userId: String, amount: String) {
        _unequalSplitAmounts.value = _unequalSplitAmounts.value.toMutableMap().apply {
            this[userId] = amount
        }
    }
}