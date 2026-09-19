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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.misgastos.app.data.MovementType
import com.misgastos.app.data.RecurringTemplate
import com.misgastos.app.data.categoriesFor
import com.misgastos.app.data.categoryByKey
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
fun RecurringScreen(viewModel: ExpenseViewModel) {
    val templates by viewModel.recurringTemplates.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Recurrentes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    "Se agregan automáticamente cada mes, sin que tengas que escribirlos de nuevo.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.padding(top = 8.dp))
            }
            if (templates.isEmpty()) {
                item {
                    Text(
                        "Aún no tienes gastos o ingresos recurrentes.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            items(templates) { template ->
                RecurringRow(
                    template = template,
                    onToggle = { viewModel.toggleRecurring(template) },
                    onDelete = { viewModel.deleteRecurring(template) }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            containerColor = Primary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar recurrente", tint = Color.White)
        }
    }

    if (showAdd) {
        AddRecurringDialog(viewModel = viewModel, onDismiss = { showAdd = false })
    }
}

@Composable
private fun RecurringRow(template: RecurringTemplate, onToggle: () -> Unit, onDelete: () -> Unit) {
    val category = categoryByKey(template.categoryKey)
    val color = category?.let { Color(it.colorHex) } ?: Color.Gray
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForCategory(template.categoryKey), contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${category?.label ?: template.categoryKey} · ${fmt(template.amount)} / mes",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = template.active,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedTrackColor = Primary)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Red, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun AddRecurringDialog(viewModel: ExpenseViewModel, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf(MovementType.GASTO) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var amountText by remember { mutableStateOf("") }
    var labelText by remember { mutableStateOf("") }
    val categories = categoriesFor(type)
    val canSave = selectedCategory != null && (amountText.toDoubleOrNull() ?: 0.0) > 0.0 && labelText.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nuevo recurrente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Cerrar") }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    listOf(MovementType.GASTO to "Gasto", MovementType.INGRESO to "Ingreso").forEach { (t, label) ->
                        val selected = type == t
                        Text(
                            text = label,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Primary else Color.Transparent)
                                .clickable { type = t; selectedCategory = null }
                                .padding(vertical = 9.dp),
                            textAlign = TextAlign.Center,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Nombre (ej. Netflix, Sueldo)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v -> if (v.all { it.isDigit() } || v.isEmpty()) amountText = v },
                    label = { Text("Monto mensual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Categoría", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                CategoryGrid(categories = categories, selectedKey = selectedCategory, onSelect = { selectedCategory = it })
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val cat = selectedCategory ?: return@Button
                        viewModel.addRecurring(type, cat, amountText.toDoubleOrNull() ?: 0.0, labelText)
                        onDismiss()
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
