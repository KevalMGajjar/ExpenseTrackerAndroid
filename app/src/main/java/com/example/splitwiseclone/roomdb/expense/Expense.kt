package com.example.splitwiseclone.roomdb.expense

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    val groupId: String? = null,
    val createdById: String,
    var totalExpense: Double,
    var description: String?,
    var splitType: String,
    var splits: List<Splits>,
    val isDeleted: Boolean,
    var currencyCode: String,
    var paidByUserIds: List<String>,
    var participants: List<String>,
    val expenseDate: String
)

data class Splits(
    val id: String,
    val owedByUserId: String,
    val owedAmount: Double,
    val owedToUserId: String
)
