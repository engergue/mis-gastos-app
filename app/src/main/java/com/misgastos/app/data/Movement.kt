package com.misgastos.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un movimiento individual: un gasto o un ingreso puntual, con fecha real. */
@Entity(tableName = "movements")
data class Movement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // MovementType.name -> "GASTO" | "INGRESO"
    val categoryKey: String,
    val amount: Double,
    val dateMillis: Long,
    val note: String? = null,
    val recurringTemplateId: Long? = null
)

@Entity(tableName = "recurring_templates")
data class RecurringTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val categoryKey: String,
    val amount: Double,
    val label: String,
    val active: Boolean = true
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val categoryKey: String,
    val monthlyLimit: Double
)
