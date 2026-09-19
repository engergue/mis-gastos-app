package com.misgastos.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.AppDatabase
import com.misgastos.app.data.Budget
import com.misgastos.app.data.Movement
import com.misgastos.app.data.MovementRepository
import com.misgastos.app.data.MovementType
import com.misgastos.app.data.RecurringTemplate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.misgastos.app.data.MonthUtils

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MovementRepository(
        AppDatabase.getInstance(application).movementDao()
    )

    val allMovements: StateFlow<List<Movement>> = repository.allMovements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recurringTemplates: StateFlow<List<RecurringTemplate>> = repository.recurringTemplates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val budgets: StateFlow<List<Budget>> = repository.budgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Compatibilidad con pantallas antiguas que aún esperan "allExpenses"
    val allExpenses: StateFlow<List<Movement>> get() = allMovements

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            repository.applyRecurringForCurrentMonth()
        }
    }

    // ---------- Movimientos ----------

    fun addMovement(type: MovementType, categoryKey: String, amount: Double, dateMillis: Long, note: String?) {
        viewModelScope.launch {
            repository.addMovement(
                Movement(type = type.name, categoryKey = categoryKey, amount = amount, dateMillis = dateMillis, note = note)
            )
        }
    }

    fun updateMovement(movement: Movement) {
        viewModelScope.launch { repository.updateMovement(movement) }
    }

    fun deleteMovement(movement: Movement) {
        viewModelScope.launch { repository.deleteMovement(movement) }
    }

    // ---------- Recurrentes ----------

    fun addRecurring(type: MovementType, categoryKey: String, amount: Double, label: String) {
        viewModelScope.launch {
            repository.addRecurring(
                RecurringTemplate(type = type.name, categoryKey = categoryKey, amount = amount, label = label)
            )
        }
    }

    fun toggleRecurring(template: RecurringTemplate) {
        viewModelScope.launch { repository.updateRecurring(template.copy(active = !template.active)) }
    }

    fun deleteRecurring(template: RecurringTemplate) {
        viewModelScope.launch { repository.deleteRecurring(template) }
    }

    // ---------- Presupuestos ----------

    fun setBudget(categoryKey: String, monthlyLimit: Double) {
        viewModelScope.launch { repository.upsertBudget(categoryKey, monthlyLimit) }
    }

    // ---------- Agregaciones ----------

    fun monthsWithData(movements: List<Movement> = allMovements.value, type: MovementType = MovementType.GASTO): List<String> =
        movements.filter { it.type == type.name }
            .map { MonthUtils.monthKey(it.dateMillis) }
            .distinct()
            .sorted()

    fun monthTotal(monthKey: String, movements: List<Movement> = allMovements.value, type: MovementType = MovementType.GASTO): Double =
        movements.filter { it.type == type.name && MonthUtils.monthKey(it.dateMillis) == monthKey }
            .sumOf { it.amount }

    fun expensesForMonth(monthKey: String, movements: List<Movement> = allMovements.value, type: MovementType = MovementType.GASTO): Map<String, Double> =
        movements.filter { it.type == type.name && MonthUtils.monthKey(it.dateMillis) == monthKey }
            .groupBy { it.categoryKey }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

    fun balanceForMonth(monthKey: String, movements: List<Movement> = allMovements.value): Double {
        val income = monthTotal(monthKey, movements, MovementType.INGRESO)
        val expense = monthTotal(monthKey, movements, MovementType.GASTO)
        return income - expense
    }

    fun spentThisMonthForCategory(categoryKey: String, movements: List<Movement> = allMovements.value): Double {
        val monthKey = MonthUtils.currentMonthKey()
        return movements.filter {
            it.type == MovementType.GASTO.name && it.categoryKey == categoryKey && MonthUtils.monthKey(it.dateMillis) == monthKey
        }.sumOf { it.amount }
    }
}
