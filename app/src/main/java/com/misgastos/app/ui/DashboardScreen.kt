package com.misgastos.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misgastos.app.data.CATEGORIES
import java.text.NumberFormat
import java.util.Locale

private fun fmt(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
    nf.maximumFractionDigits = 0
    return "$" + nf.format(value)
}

@Composable
fun DashboardScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    val months = viewModel.monthsWithData(expenses)
    val lastMonth = months.lastOrNull()
    val prevMonth = if (months.size > 1) months[months.size - 2] else null
    val lastTotal = lastMonth?.let { viewModel.monthTotal(it, expenses) } ?: 0.0
    val prevTotal = prevMonth?.let { viewModel.monthTotal(it, expenses) } ?: 0.0
    val delta = if (prevTotal > 0) (lastTotal - prevTotal) / prevTotal * 100 else 0.0
    val avg = if (months.isNotEmpty()) months.sumOf { viewModel.monthTotal(it, expenses) } / months.size else 0.0
    val grandTotal = months.sumOf { viewModel.monthTotal(it, expenses) }

    val topCategory = CATEGORIES.maxByOrNull { viewModel.categoryTotalAcrossMonths(it.key, expenses) }
    val topCategoryTotal = topCategory?.let { viewModel.categoryTotalAcrossMonths(it.key, expenses) } ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        KpiCard(
                            label = "Último mes${if (lastMonth != null) " (${lastMonth})" else ""}",
                            value = fmt(lastTotal),
                            delta = if (prevMonth != null) "${if (delta >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(delta))}%" else null,
                            deltaGood = delta < 0
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        KpiCard(label = "Promedio mensual", value = fmt(avg))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        KpiCard(label = "Total acumulado", value = fmt(grandTotal))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        KpiCard(
                            label = "Mayor categoría",
                            value = topCategory?.label ?: "-",
                            sub = fmt(topCategoryTotal)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total por mes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                    BarChart(
                        labels = months,
                        values = months.map { viewModel.monthTotal(it, expenses) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (lastMonth != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Distribución — $lastMonth", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 10.dp))
                        val monthMap = viewModel.expensesForMonth(lastMonth)
                        val values = CATEGORIES.map { monthMap[it.key] ?: 0.0 }
                        val colors = CATEGORIES.map { Color(it.colorHex) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PieChart(values = values, colors = colors)
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 10.dp))
                        CATEGORIES.forEachIndexed { i, cat ->
                            val v = monthMap[cat.key] ?: 0.0
                            if (v > 0) {
                                LegendRow(color = colors[i], label = cat.label, value = fmt(v))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, delta: String? = null, deltaGood: Boolean = true, sub: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (delta != null) {
                Text(
                    text = delta,
                    fontSize = 11.sp,
                    color = if (deltaGood) Color(0xFF22A55A) else Color(0xFFE5484D),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (sub != null) {
                Text(
                    text = sub,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
