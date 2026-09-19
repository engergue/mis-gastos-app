package com.misgastos.app.data

import kotlinx.coroutines.flow.Flow

class MovementRepository(private val dao: MovementDao) {

    val allMovements: Flow<List<Movement>> = dao.getAll()
    val recurringTemplates: Flow<List<RecurringTemplate>> = dao.getRecurringTemplates()
    val budgets: Flow<List<Budget>> = dao.getBudgets()

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.insertAll(SeedData.toMovementList())
        }
    }

    suspend fun addMovement(movement: Movement) = dao.insert(movement)
    suspend fun updateMovement(movement: Movement) = dao.update(movement)
    suspend fun deleteMovement(movement: Movement) = dao.delete(movement)

    suspend fun addRecurring(template: RecurringTemplate) = dao.insertRecurring(template)
    suspend fun updateRecurring(template: RecurringTemplate) = dao.updateRecurring(template)
    suspend fun deleteRecurring(template: RecurringTemplate) = dao.deleteRecurring(template)

    suspend fun upsertBudget(categoryKey: String, monthlyLimit: Double) {
        if (monthlyLimit <= 0.0) {
            dao.deleteBudget(categoryKey)
        } else {
            dao.upsertBudget(Budget(categoryKey, monthlyLimit))
        }
    }

    /**
     * Revisa las plantillas recurrentes activas y, si el mes actual todavía no tiene
     * un movimiento generado por esa plantilla, lo crea automáticamente (fechado hoy).
     */
    suspend fun applyRecurringForCurrentMonth() {
        val monthKey = MonthUtils.currentMonthKey()
        val start = MonthUtils.startOfMonthMillis(monthKey)
        val end = MonthUtils.endOfMonthMillis(monthKey)
        val templates = dao.getActiveRecurringTemplatesOnce()
        templates.forEach { template ->
            val existing = dao.findMovementForTemplateInRange(template.id, start, end)
            if (existing == null) {
                dao.insert(
                    Movement(
                        type = template.type,
                        categoryKey = template.categoryKey,
                        amount = template.amount,
                        dateMillis = MonthUtils.nowMillis(),
                        note = template.label,
                        recurringTemplateId = template.id
                    )
                )
            }
        }
    }
}
