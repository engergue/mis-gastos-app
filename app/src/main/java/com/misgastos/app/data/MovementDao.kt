package com.misgastos.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovementDao {

    @Query("SELECT * FROM movements ORDER BY dateMillis DESC, id DESC")
    fun getAll(): Flow<List<Movement>>

    @Query("SELECT COUNT(*) FROM movements")
    suspend fun count(): Int

    @Insert
    suspend fun insert(movement: Movement): Long

    @Insert
    suspend fun insertAll(movements: List<Movement>)

    @Update
    suspend fun update(movement: Movement)

    @Delete
    suspend fun delete(movement: Movement)

    @Query("SELECT * FROM recurring_templates ORDER BY id DESC")
    fun getRecurringTemplates(): Flow<List<RecurringTemplate>>

    @Query("SELECT * FROM recurring_templates WHERE active = 1")
    suspend fun getActiveRecurringTemplatesOnce(): List<RecurringTemplate>

    @Insert
    suspend fun insertRecurring(template: RecurringTemplate): Long

    @Update
    suspend fun updateRecurring(template: RecurringTemplate)

    @Delete
    suspend fun deleteRecurring(template: RecurringTemplate)

    @Query("SELECT * FROM movements WHERE recurringTemplateId = :templateId AND dateMillis BETWEEN :monthStart AND :monthEnd LIMIT 1")
    suspend fun findMovementForTemplateInRange(templateId: Long, monthStart: Long, monthEnd: Long): Movement?

    @Query("SELECT * FROM budgets")
    fun getBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE categoryKey = :categoryKey")
    suspend fun deleteBudget(categoryKey: String)
}
