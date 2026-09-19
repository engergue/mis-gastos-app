package com.misgastos.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** Utilidades para agrupar movimientos por mes calendario ("yyyy-MM") y formatearlos en español. */
object MonthUtils {
    private val zone = ZoneId.systemDefault()
    private val locale = Locale("es", "ES")

    fun monthKey(millis: Long): String {
        val ym = YearMonth.from(Instant.ofEpochMilli(millis).atZone(zone))
        return ym.toString() // "2026-09"
    }

    fun currentMonthKey(): String = YearMonth.now(zone).toString()

    private fun parse(monthKey: String): YearMonth = YearMonth.parse(monthKey)

    fun startOfMonthMillis(monthKey: String): Long =
        parse(monthKey).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfMonthMillis(monthKey: String): Long =
        parse(monthKey).atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

    /** "Septiembre 2026" */
    fun fullLabel(monthKey: String): String {
        val ym = parse(monthKey)
        val name = ym.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
        return "$name ${ym.year}"
    }

    /** "Sep" — para ejes de gráfico */
    fun shortLabel(monthKey: String): String {
        val ym = parse(monthKey)
        val name = ym.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
        return name.trimEnd('.')
    }

    /** "dd MMM yyyy" para mostrar la fecha de un movimiento puntual */
    fun dayLabel(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val month = date.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }.trimEnd('.')
        return "${date.dayOfMonth} $month ${date.year}"
    }

    fun nowMillis(): Long = System.currentTimeMillis()

    fun toMillis(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun toLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    /**
     * Lista de meses para elegir en el selector: desde [monthsBack] meses atrás hasta
     * [monthsForward] meses adelante del mes actual, del más reciente al más antiguo.
     * Si se pasa [mustInclude], ese mes se agrega aunque quede fuera del rango.
     */
    fun monthOptions(monthsBack: Int = 18, monthsForward: Int = 2, mustInclude: String? = null): List<String> {
        val current = YearMonth.now(zone)
        val options = (-monthsBack..monthsForward).map { current.plusMonths(it.toLong()).toString() }.toMutableSet()
        mustInclude?.let { options.add(it) }
        return options.sortedDescending()
    }
}
