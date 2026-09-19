package com.misgastos.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misgastos.app.data.CATEGORIES
import com.misgastos.app.data.MonthUtils
import com.misgastos.app.data.MovementType
import com.misgastos.app.ui.theme.HeaderDark
import com.misgastos.app.ui.theme.HeaderDarkVariant
import java.text.NumberFormat
import java.util.Locale

private fun fmt(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
    nf.maximumFractionDigits = 0
    return "$" + nf.format(value)
}

@Composable
fun DashboardScreen(viewModel: ExpenseViewModel) {
    val movements by viewModel.allMovements.collectAsState()
    val monthKeys = viewModel.monthsWithData(movements, MovementType.GASTO)
        .toMutableSet()
        .apply { addAll(viewModel.monthsWithData(movements, MovementType.INGRESO)) }
        .sorted()

    var selectedMonth by remember(monthKeys) { mutableStateOf(monthKeys.lastOrNull()) }
    var monthMenuExpanded by remember { mutableStateOf(false) }

    val currentMonth = selectedMonth ?: monthKeys.lastOrNull()
    val monthIndex = monthKeys.indexOf(currentMonth)
    val prevMonth = if (monthIndex > 0) monthKeys[monthIndex - 1] else null

    val currentExpense = currentMonth?.let { viewModel.monthTotal(it, movements, MovementType.GASTO) } ?: 0.0
    val currentIncome = currentMonth?.let { viewModel.monthTotal(it, movements, MovementType.INGRESO) } ?: 0.0
    val currentBalance = currentIncome - currentExpense
    val prevExpense = prevMonth?.let { viewModel.monthTotal(it, movements, MovementType.GASTO) } ?: 0.0
    val delta = if (prevExpense > 0) (currentExpense - prevExpense) / prevExpense * 100 else 0.0

    val expenseMonths = viewModel.monthsWithData(movements, MovementType.GASTO)
    val avg = if (expenseMonths.isNotEmpty()) expenseMonths.sumOf { viewModel.monthTotal(it, movements, MovementType.GASTO) } / expenseMonths.size else 0.0
    val grandTotal = expenseMonths.sumOf { viewModel.monthTotal(it, movements, MovementType.GASTO) }

    val monthMap = currentMonth?.let { viewModel.expensesForMonth(it, movements, MovementType.GASTO) } ?: emptyMap()
    val categoryEntries = CATEGORIES
        .map { cat -> Triple(cat, monthMap[cat.key] ?: 0.0, Color(cat.colorHex)) }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ---------- Header oscuro con selector de mes + dona con el total ----------
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(HeaderDark, HeaderDarkVariant)
                        )
                    )
                    .padding(top = 20.dp, bottom = 22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = com.misgastos.app.R.drawable.app_logo_white),
                            contentDescription = "Logo",
                            modifier = Modifier.height(26.dp)
                        )
                        Text(
                            text = "  Mis Gastos",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { monthMenuExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentMonth?.let { MonthUtils.fullLabel(it) } ?: "Sin datos",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Elegir mes",
                                tint = Color.White,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                        DropdownMenu(expanded = monthMenuExpanded, onDismissRequest = { monthMenuExpanded = false }) {
                            monthKeys.sortedDescending().forEach { mk ->
                                DropdownMenuItem(text = { Text(MonthUtils.fullLabel(mk)) }, onClick = {
                                    selectedMonth = mk
                                    monthMenuExpanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    if (categoryEntries.isNotEmpty()) {
                        DonutChart(
                            values = categoryEntries.map { it.second },
                            colors = categoryEntries.map { it.third },
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        DonutChart(
                            values = listOf(1.0),
                            colors = listOf(Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Gastos del mes",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = fmt(currentExpense),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (prevMonth != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (delta >= 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (delta < 0) Color(0xFF34D399) else Color(0xFFF87171),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = " ${"%.1f".format(kotlin.math.abs(delta))}% vs mes anterior",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- Ingresos / Gastos / Balance ----------
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BalancePill(label = "Ingresos", value = fmt(currentIncome), color = Color(0xFF34D399))
                    BalancePill(label = "Gastos", value = fmt(currentExpense), color = Color(0xFFF87171))
                    BalancePill(
                        label = "Balance",
                        value = fmt(currentBalance),
                        color = if (currentBalance >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                }
            }
        }

        // ---------- Chips de resumen rápido ----------
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniStat(label = "Promedio mensual", value = fmt(avg))
                }
                Box(modifier = Modifier.weight(1f)) {
                    MiniStat(label = "Acumulado", value = fmt(grandTotal))
                }
            }
        }

        // ---------- Lista de categorías con ícono + barra de progreso ----------
        if (categoryEntries.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Gastos por categoría",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val maxVal = categoryEntries.maxOf { it.second }
                        categoryEntries.forEach { (cat, value, color) ->
                            CategoryProgressRow(
                                icon = iconForCategory(cat.key),
                                color = color,
                                label = cat.label,
                                value = fmt(value),
                                progress = (value / maxVal).toFloat()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        // ---------- Barra: total por mes ----------
        if (expenseMonths.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total por mes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        BarChart(
                            labels = expenseMonths.map { MonthUtils.shortLabel(it) },
                            values = expenseMonths.map { viewModel.monthTotal(it, movements, MovementType.GASTO) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalancePill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoryProgressRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    label: String,
    value: String,
    progress: Float
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(progress = progress, color = color, modifier = Modifier.fillMaxWidth())
        }
    }
}
