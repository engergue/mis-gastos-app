package com.misgastos.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
fun TableScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    val months = viewModel.monthsWithData(expenses)
    val colWidth = 100.dp
    val labelWidth = 130.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Row {
            Text("Categoría", modifier = Modifier.width(labelWidth), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            months.forEach { m ->
                Text(m.take(3), modifier = Modifier.width(colWidth), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Divider(modifier = Modifier.padding(vertical = 6.dp))

        CATEGORIES.forEach { cat ->
            val monthMap = months.associateWith { m -> viewModel.expensesForMonth(m)[cat.key] ?: 0.0 }
            // Nota: expensesForMonth recalcula desde el StateFlow cacheado (allExpenses), es rapido para listas pequenas
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text(cat.label, modifier = Modifier.width(labelWidth), fontSize = 11.sp)
                months.forEach { m ->
                    Text(fmt(monthMap[m] ?: 0.0), modifier = Modifier.width(colWidth), fontSize = 11.sp)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 6.dp))
        Row {
            Text("Total", modifier = Modifier.width(labelWidth), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            months.forEach { m ->
                Text(
                    fmt(viewModel.monthTotal(m, expenses)),
                    modifier = Modifier.width(colWidth),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
