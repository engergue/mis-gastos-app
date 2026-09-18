package com.misgastos.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.AppDatabase
import com.misgastos.app.data.CATEGORIES
import com.misgastos.app.data.Expense
import com.misgastos.app.data.ExpenseRepository
import com.misgastos.app.data.MESES
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(
        AppDatabase.getInstance(application).expenseDao()
    )

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    fun expensesForMonth(month: String): Map<String, Double> {
        val list = allExpenses.value.filter { it.month == month }
        return CATEGORIES.associate { cat ->
            cat.key to (list.find { it.categoryKey == cat.key }?.amount ?: 0.0)
        }
    }

    fun saveMonth(month: String, values: Map<String, Double>) {
        viewModelScope.launch {
            val expenses = values.map { (key, amount) -> Expense(month, key, amount) }
            repository.upsertAll(expenses)
        }
    }

    fun monthTotal(month: String, expenses: List<Expense> = allExpenses.value): Double =
        expenses.filter { it.month == month }.sumOf { it.amount }

    fun categoryTotalAcrossMonths(key: String, expenses: List<Expense> = allExpenses.value): Double =
        expenses.filter { it.categoryKey == key }.sumOf { it.amount }

    fun monthsWithData(expenses: List<Expense> = allExpenses.value): List<String> =
        MESES.filter { m -> monthTotal(m, expenses) > 0 }
}
