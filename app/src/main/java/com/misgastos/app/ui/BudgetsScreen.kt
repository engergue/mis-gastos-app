package com.misgastos.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.misgastos.app.data.Budget
import com.misgastos.app.data.Category
import com.misgastos.app.data.EXPENSE_CATEGORIES
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

@Composable
fun BudgetsScreen(viewModel: ExpenseViewModel) {
    val movements by viewModel.allMovements.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val budgetMap = budgets.associateBy { it.categoryKey }

    var editingCategory by remember { mutableStateOf<Category?>(null) }

    val spentByCategory = EXPENSE_CATEGORIES.associate { it.key to viewModel.spentThisMonthForCategory(it.key, movements) }
    val totalSpent = spentByCategory.values.sum()
    val totalBudgeted = budgets.sumOf { it.monthlyLimit }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Presupuestos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                "Define un límite mensual por categoría y sigue tu avance.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.padding(top = 10.dp))
        }
        if (totalBudgeted > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gastado este mes", fontSize = 12.sp)
                            Text("${fmt(totalSpent)} / ${fmt(totalBudgeted)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.padding(top = 6.dp))
                        val progress = if (totalBudgeted > 0) (totalSpent / totalBudgeted).toFloat() else 0f
                        ProgressBar(
                            progress = progress,
                            color = progressColor(progress),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        items(EXPENSE_CATEGORIES) { cat ->
            val color = Color(cat.colorHex)
            val spent = spentByCategory[cat.key] ?: 0.0
            val limit = budgetMap[cat.key]?.monthlyLimit ?: 0.0
            val progress = if (limit > 0) (spent / limit).toFloat() else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingCategory = cat },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(iconForCategory(cat.key), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cat.label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (limit > 0) "${fmt(spent)} / ${fmt(limit)}" else "${fmt(spent)} · sin límite",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (limit > 0) {
                            Spacer(modifier = Modifier.padding(top = 6.dp))
                            ProgressBar(progress = progress, color = progressColor(progress), modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }

    editingCategory?.let { cat ->
        BudgetEditDialog(
            category = cat,
            currentLimit = budgetMap[cat.key]?.monthlyLimit ?: 0.0,
            onSave = { newLimit -> viewModel.setBudget(cat.key, newLimit) },
            onDismiss = { editingCategory = null }
        )
    }
}

private fun progressColor(progress: Float): Color = when {
    progress >= 1f -> Red
    progress >= 0.7f -> Color(0xFFF5A524)
    else -> Green
}

@Composable
private fun BudgetEditDialog(
    category: Category,
    currentLimit: Double,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(if (currentLimit > 0) currentLimit.toLong().toString() else "") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Límite mensual — ${category.label}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.padding(top = 12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { v -> if (v.all { it.isDigit() } || v.isEmpty()) text = v },
                    label = { Text("Monto mensual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(top = 14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onSave(0.0); onDismiss() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Quitar")
                    }
                    Button(
                        onClick = { onSave(text.toDoubleOrNull() ?: 0.0); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
