package com.misgastos.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.misgastos.app.data.Category
import com.misgastos.app.data.MonthUtils
import com.misgastos.app.data.Movement
import com.misgastos.app.data.MovementType
import com.misgastos.app.data.categoriesFor
import com.misgastos.app.data.categoryByKey
import com.misgastos.app.ui.theme.Primary
import com.misgastos.app.ui.theme.Red
import java.text.NumberFormat
import java.util.Locale

private fun fmtMoney(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
    nf.maximumFractionDigits = 0
    return "$" + nf.format(value)
}

/** Grilla de íconos de categoría — tocar un ícono la selecciona. Reutilizada en varios diálogos. */
@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        categories.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { cat ->
                    val color = Color(cat.colorHex)
                    val selected = cat.key == selectedKey
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(cat.key) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (selected) color else color.copy(alpha = 0.15f))
                                .then(
                                    if (selected) Modifier.border(2.dp, color.copy(alpha = 0.5f), CircleShape) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                iconForCategory(cat.key),
                                contentDescription = cat.label,
                                tint = if (selected) Color.White else color,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.label,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Selector simple de mes (como el dropdown de mes de la versión anterior de la app). */
@Composable
private fun MonthSelector(monthKey: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(MonthUtils.fullLabel(monthKey), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Elegir mes", modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { mk ->
                DropdownMenuItem(text = { Text(MonthUtils.fullLabel(mk)) }, onClick = {
                    onSelect(mk)
                    expanded = false
                })
            }
        }
    }
}

/** Diálogo tipo hoja para crear o editar un movimiento (gasto o ingreso), con soporte de sub-pagos. */
@Composable
fun AddMovementDialog(
    viewModel: ExpenseViewModel,
    initialMovement: Movement? = null,
    initialCategoryKey: String? = null,
    initialMonthKey: String? = null,
    onDismiss: () -> Unit
) {
    val allMovements by viewModel.allMovements.collectAsState()

    var type by remember {
        mutableStateOf(initialMovement?.let { MovementType.valueOf(it.type) } ?: MovementType.GASTO)
    }
    var selectedCategory by remember { mutableStateOf(initialMovement?.categoryKey ?: initialCategoryKey) }
    var selectedMonthKey by remember {
        mutableStateOf(
            initialMovement?.let { MonthUtils.monthKey(it.dateMillis) }
                ?: initialMonthKey
                ?: MonthUtils.currentMonthKey()
        )
    }
    var amountText by remember {
        mutableStateOf(initialMovement?.amount?.let { if (it == 0.0) "" else it.toLong().toString() } ?: "")
    }
    var conceptText by remember { mutableStateOf(initialMovement?.note ?: "") }

    val categories = categoriesFor(type)
    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val canSave = selectedCategory != null && amountValue > 0.0
    val monthOptions = remember(selectedMonthKey) { MonthUtils.monthOptions(mustInclude = selectedMonthKey) }

    // Sub-pagos ya registrados para esta categoría + este mes (para verlos antes de agregar otro).
    val existingForSelection = remember(allMovements, selectedCategory, selectedMonthKey, type) {
        if (selectedCategory == null) emptyList()
        else allMovements.filter {
            it.categoryKey == selectedCategory &&
                it.type == type.name &&
                MonthUtils.monthKey(it.dateMillis) == selectedMonthKey &&
                it.id != (initialMovement?.id ?: -1L)
        }
    }
    val existingTotal = existingForSelection.sumOf { it.amount }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (initialMovement == null) "Nuevo movimiento" else "Editar movimiento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector Gasto / Ingreso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    listOf(MovementType.GASTO to "Gasto", MovementType.INGRESO to "Ingreso").forEach { (t, label) ->
                        val selected = type == t
                        val bg = if (selected) (if (t == MovementType.GASTO) Red else Primary) else Color.Transparent
                        Text(
                            text = label,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .clickable {
                                    type = t
                                    selectedCategory = null
                                }
                                .padding(vertical = 9.dp),
                            textAlign = TextAlign.Center,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Mes al que pertenece",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                MonthSelector(monthKey = selectedMonthKey, options = monthOptions, onSelect = { selectedMonthKey = it })

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "Elige una categoría",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                CategoryGrid(categories = categories, selectedKey = selectedCategory, onSelect = { selectedCategory = it })

                // ---------- Ya registrado en esta categoría + mes (los "sub-pagos") ----------
                if (existingForSelection.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val catLabel = selectedCategory?.let { categoryByKey(it)?.label } ?: ""
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Ya registrado en $catLabel — ${MonthUtils.fullLabel(selectedMonthKey)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            existingForSelection.forEach { m ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(m.note?.takeIf { it.isNotBlank() } ?: "Sin nombre", fontSize = 12.sp)
                                    Text(fmtMoney(m.amount), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(fmtMoney(existingTotal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        "Agrega otro pago para sumarlo a esta categoría (ej. otra factura).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = conceptText,
                    onValueChange = { conceptText = it },
                    label = { Text("Concepto (ej. Movistar, Claro, Recibo 1)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v -> if (v.all { it.isDigit() } || v.isEmpty()) amountText = v },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (initialMovement != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteMovement(initialMovement)
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Eliminar")
                        }
                    }
                    Button(
                        onClick = {
                            val cat = selectedCategory ?: return@Button
                            val finalNote = if (conceptText.isBlank()) null else conceptText
                            val dateMillis = MonthUtils.startOfMonthMillis(selectedMonthKey)
                            if (initialMovement == null) {
                                viewModel.addMovement(type, cat, amountValue, dateMillis, finalNote)
                            } else {
                                viewModel.updateMovement(
                                    initialMovement.copy(
                                        type = type.name,
                                        categoryKey = cat,
                                        amount = amountValue,
                                        dateMillis = dateMillis,
                                        note = finalNote
                                    )
                                )
                            }
                            onDismiss()
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
