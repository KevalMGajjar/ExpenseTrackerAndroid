package com.example.splitwiseclone.central
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

/**
 * A Moshi adapter that tells Moshi how to convert a BigDecimal
 * to a JSON string and vice-versa.
 */
class BigDecimalAdapter {
    @ToJson
    fun toJson(value: BigDecimal): String {
        return value.toPlainString() // Convert BigDecimal to a plain string
    }

    @FromJson
    fun fromJson(value: String): BigDecimal {
        return BigDecimal(value) // Convert string from JSON back to BigDecimal
    }
}