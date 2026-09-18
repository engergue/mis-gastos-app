package com.misgastos.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misgastos.app.data.CATEGORIES
import com.misgastos.app.data.MESES
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private fun fmt(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
    nf.maximumFractionDigits = 0
    return "$" + nf.format(value)
}

@Composable
fun EntryScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    var selectedMonth by remember { mutableStateOf(viewModel.monthsWithData(expenses).lastOrNull() ?: MESES.first()) }
    var menuExpanded by remember { mutableStateOf(false) }
    val fieldValues = remember { mutableStateMapOf<String, String>() }
    var showSaved by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMonth, expenses) {
        val current = viewModel.expensesForMonth(selectedMonth)
        CATEGORIES.forEach { cat ->
            fieldValues[cat.key] = (current[cat.key] ?: 0.0).let { if (it == 0.0) "0" else it.toLong().toString() }
        }
    }

    LaunchedEffect(showSaved) {
        if (showSaved) {
            delay(2000)
            showSaved = false
        }
    }

    val total = fieldValues.values.sumOf { it.toDoubleOrNull() ?: 0.0 }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedMonth, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Elegir mes")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            MESES.forEach { mes ->
                                DropdownMenuItem(
                                    text = { Text(mes) },
                                    onClick = {
                                        selectedMonth = mes
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Text(text = "Total: ${fmt(total)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CATEGORIES) { cat ->
                OutlinedTextField(
                    value = fieldValues[cat.key] ?: "0",
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() } || newVal.isEmpty()) {
                            fieldValues[cat.key] = newVal
                        }
                    },
                    label = { Text(cat.label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Button(
                    onClick = {
                        val values = CATEGORIES.associate { cat ->
                            cat.key to (fieldValues[cat.key]?.toDoubleOrNull() ?: 0.0)
                        }
                        viewModel.saveMonth(selectedMonth, values)
                        showSaved = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Guardar $selectedMonth")
                }
                if (showSaved) {
                    Snackbar(modifier = Modifier.padding(top = 8.dp)) {
                        Text("✓ Gastos guardados")
                    }
                }
            }
        }
    }
}
