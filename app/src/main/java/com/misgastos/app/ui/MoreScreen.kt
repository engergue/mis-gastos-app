package com.misgastos.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misgastos.app.ui.theme.Primary

private enum class MoreSection { NONE, TABLE, RECURRING }

@Composable
fun MoreScreen(viewModel: ExpenseViewModel) {
    var section by remember { mutableStateOf(MoreSection.NONE) }

    when (section) {
        MoreSection.TABLE -> ScreenWithBack(title = "Tabla de gastos", onBack = { section = MoreSection.NONE }) {
            TableScreen(viewModel)
        }
        MoreSection.RECURRING -> ScreenWithBack(title = "Recurrentes", onBack = { section = MoreSection.NONE }) {
            RecurringScreen(viewModel)
        }
        MoreSection.NONE -> {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = com.misgastos.app.R.drawable.app_logo_white),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(30.dp)
                            .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(6.dp))
                            .padding(4.dp)
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("Mis Gastos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("por ENGEL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))

                MoreRow(icon = Icons.Filled.Repeat, title = "Gastos e ingresos recurrentes", subtitle = "Automatiza lo que se repite cada mes") {
                    section = MoreSection.RECURRING
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(10.dp))
                MoreRow(icon = Icons.Filled.TableChart, title = "Tabla de gastos", subtitle = "Vista tipo hoja de cálculo, mes a mes") {
                    section = MoreSection.TABLE
                }
            }
        }
    }
}

@Composable
private fun MoreRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun ScreenWithBack(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        content()
    }
}
