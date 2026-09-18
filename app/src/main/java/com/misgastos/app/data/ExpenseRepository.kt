package com.misgastos.app.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {

    val allExpenses: Flow<List<Expense>> = dao.getAll()

    suspend fun upsertAll(expenses: List<Expense>) = dao.upsertAll(expenses)

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.upsertAll(SeedData.toExpenseList())
        }
    }
}
