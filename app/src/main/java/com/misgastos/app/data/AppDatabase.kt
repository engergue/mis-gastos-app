package com.misgastos.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.ZoneId

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS movements (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                categoryKey TEXT NOT NULL,
                amount REAL NOT NULL,
                dateMillis INTEGER NOT NULL,
                note TEXT,
                recurringTemplateId INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recurring_templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                categoryKey TEXT NOT NULL,
                amount REAL NOT NULL,
                label TEXT NOT NULL,
                active INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS budgets (
                categoryKey TEXT PRIMARY KEY NOT NULL,
                monthlyLimit REAL NOT NULL
            )
            """.trimIndent()
        )

        // Migrar los totales mensuales antiguos ("expenses") a movimientos individuales,
        // fechados el día 1 de cada mes del año en curso, para no perder el historial.
        val monthIndex = mapOf(
            "Enero" to 1, "Febrero" to 2, "Marzo" to 3, "Abril" to 4, "Mayo" to 5, "Junio" to 6,
            "Julio" to 7, "Agosto" to 8, "Septiembre" to 9, "Octubre" to 10, "Noviembre" to 11, "Diciembre" to 12
        )
        val year = LocalDate.now().year
        val cursor = db.query("SELECT month, categoryKey, amount FROM expenses")
        cursor.use {
            while (it.moveToNext()) {
                val month = it.getString(0)
                val categoryKey = it.getString(1)
                val amount = it.getDouble(2)
                if (amount > 0.0) {
                    val monthNum = monthIndex[month] ?: 1
                    val dateMillis = LocalDate.of(year, monthNum, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    db.execSQL(
                        "INSERT INTO movements (type, categoryKey, amount, dateMillis, note, recurringTemplateId) VALUES (?, ?, ?, ?, ?, NULL)",
                        arrayOf("GASTO", categoryKey, amount, dateMillis, "Migrado")
                    )
                }
            }
        }
        db.execSQL("DROP TABLE IF EXISTS expenses")
    }
}

@Database(entities = [Movement::class, RecurringTemplate::class, Budget::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movementDao(): MovementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "misgastos.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
