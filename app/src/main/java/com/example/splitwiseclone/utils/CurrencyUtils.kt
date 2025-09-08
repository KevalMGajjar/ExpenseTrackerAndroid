package com.example.splitwiseclone.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object CurrencyUtils {

    private val inrFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun formatCurrency(amount: Double, withSign: Boolean = false): String {
        if (abs(amount) < 0.01 && !withSign) {
            return "Settled Up"
        }

        val formattedAmount = inrFormatter.format(abs(amount))

        return when {
            amount > 0.01 && withSign -> "+ $formattedAmount"
            amount < -0.01 -> "- $formattedAmount"
            else -> formattedAmount
        }
    }
}