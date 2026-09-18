package com.misgastos.app.data

import androidx.room.Entity

@Entity(tableName = "expenses", primaryKeys = ["month", "categoryKey"])
data class Expense(
    val month: String,
    val categoryKey: String,
    val amount: Double
)
