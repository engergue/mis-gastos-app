package com.misgastos.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.misgastos.app.data.MonthUtils
import com.misgastos.app.data.MovementType
import com.misgastos.app.ui.theme.Primary
import java.text.NumberFormat
import java.util.Locale

private fun fmt(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
    nf.maximumFractionDigits = 0
    return "$" + nf.format(value)
}

@Composable
fun TableScreen(viewModel: ExpenseViewModel) {
    val movements by viewModel.allMovements.collectAsState()
    val months = viewModel.monthsWithData(movements, MovementType.GASTO)
    val colWidth = 100.dp
    val labelWidth = 140.dp

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Tabla de gastos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .background(Primary.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                ) {
                    Text(
                        "Categoría",
                        modifier = Modifier.width(labelWidth),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Primary
                    )
                    months.forEach { m ->
                        Text(
                            MonthUtils.shortLabel(m),
                            modifier = Modifier.width(colWidth),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Primary
                        )
                    }
                }

                CATEGORIES.forEachIndexed { index, cat ->
                    val monthMap = months.associateWith { m -> viewModel.expensesForMonth(m, movements, MovementType.GASTO)[cat.key] ?: 0.0 }
                    val rowBg = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg, RoundedCornerShape(6.dp))
                            .padding(vertical = 6.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.width(labelWidth), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                iconForCategory(cat.key),
                                contentDescription = null,
                                tint = Color(cat.colorHex),
                                modifier = Modifier.padding(end = 6.dp).size(14.dp)
                            )
                            Text(cat.label, fontSize = 11.sp)
                        }
                        months.forEach { m ->
                            Text(fmt(monthMap[m] ?: 0.0), modifier = Modifier.width(colWidth), fontSize = 11.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .background(Primary.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                ) {
                    Text("Total", modifier = Modifier.width(labelWidth), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Primary)
                    months.forEach { m ->
                        Text(
                            fmt(viewModel.monthTotal(m, movements, MovementType.GASTO)),
                            modifier = Modifier.width(colWidth),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Primary
                        )
                    }
                }
            }
        }
    }
}
