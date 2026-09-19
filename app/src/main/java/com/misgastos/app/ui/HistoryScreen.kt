package com.misgastos.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misgastos.app.data.MonthUtils
import com.misgastos.app.data.Movement
import com.misgastos.app.data.MovementType
import com.misgastos.app.data.categoryByKey
import com.misgastos.app.ui.theme.Green
import com.misgastos.app.ui.theme.Primary
import com.misgastos.app.ui.theme.Red
import java.text.NumberFormat
import java.util.Locale

private fun fmt(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
    nf.maximumFractionDigits = 0
    return "$" + nf.format(value)
}

private data class AddTarget(val categoryKey: String, val monthKey: String, val type: MovementType)

@Composable
fun HistoryScreen(viewModel: ExpenseViewModel) {
    val movements by viewModel.allMovements.collectAsState()
    var editingMovement by remember { mutableStateOf<Movement?>(null) }
    var addTarget by remember { mutableStateOf<AddTarget?>(null) }

    val byMonth = movements
        .sortedByDescending { it.dateMillis }
        .groupBy { MonthUtils.monthKey(it.dateMillis) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (movements.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aún no tienes movimientos", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    "Toca el botón + para agregar tu primer gasto o ingreso.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Historial", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.padding(top = 6.dp))
                }
                byMonth.keys.sortedDescending().forEach { monthKey ->
                    val monthMovements = byMonth[monthKey].orEmpty()
                    val income = monthMovements.filter { it.type == MovementType.INGRESO.name }.sumOf { it.amount }
                    val expense = monthMovements.filter { it.type == MovementType.GASTO.name }.sumOf { it.amount }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                MonthUtils.fullLabel(monthKey).replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                "Balance: ${fmt(income - expense)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Agrupar dentro del mes por categoría — cada grupo es la categoría "global"
                    // y sus movimientos internos son los sub-pagos (ej. Movistar + Claro en Internet).
                    val byCategory = monthMovements
                        .groupBy { it.categoryKey }
                        .toList()
                        .sortedByDescending { (_, list) -> list.sumOf { it.amount } }

                    byCategory.forEach { (categoryKey, catMovements) ->
                        val categoryType = MovementType.valueOf(catMovements.first().type)
                        item {
                            CategoryGroup(
                                categoryKey = categoryKey,
                                type = categoryType,
                                movements = catMovements,
                                onEdit = { editingMovement = it },
                                onAddSub = { addTarget = AddTarget(categoryKey, monthKey, categoryType) }
                            )
                        }
                    }
                }
            }
        }
    }

    editingMovement?.let { m ->
        AddMovementDialog(viewModel = viewModel, initialMovement = m, onDismiss = { editingMovement = null })
    }
    addTarget?.let { t ->
        AddMovementDialog(
            viewModel = viewModel,
            initialCategoryKey = t.categoryKey,
            initialMonthKey = t.monthKey,
            onDismiss = { addTarget = null }
        )
    }
}

@Composable
private fun CategoryGroup(
    categoryKey: String,
    type: MovementType,
    movements: List<Movement>,
    onEdit: (Movement) -> Unit,
    onAddSub: () -> Unit
) {
    val category = categoryByKey(categoryKey)
    val color = category?.let { Color(it.colorHex) } ?: Color.Gray
    val isIncome = type == MovementType.INGRESO
    val total = movements.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconForCategory(categoryKey), contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    category?.label ?: categoryKey,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = (if (isIncome) "+ " else "- ") + fmt(total),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Green else Red
                )
                IconButton(onClick = onAddSub, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar sub-pago", tint = Primary, modifier = Modifier.size(16.dp))
                }
            }

            // Sub-pagos: si hay más de uno, o si el único tiene un concepto con nombre, los mostramos.
            if (movements.size > 1 || movements.any { !it.note.isNullOrBlank() }) {
                Spacer(modifier = Modifier.height(6.dp))
                movements.sortedByDescending { it.amount }.forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 44.dp, top = 3.dp, bottom = 3.dp)
                            .clickable { onEdit(m) },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            m.note?.takeIf { it.isNotBlank() } ?: "Pago",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        Text(
                            fmt(m.amount),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            } else if (movements.size == 1) {
                // Un solo pago sin nombre: tocar la fila completa para editarlo directamente.
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(movements.first()) }
                        .padding(start = 44.dp)
                ) {
                    Text(
                        "Toca para editar",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}
