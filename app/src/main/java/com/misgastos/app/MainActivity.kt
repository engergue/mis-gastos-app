package com.misgastos.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.misgastos.app.ui.AddMovementDialog
import com.misgastos.app.ui.BudgetsScreen
import com.misgastos.app.ui.DashboardScreen
import com.misgastos.app.ui.ExpenseViewModel
import com.misgastos.app.ui.HistoryScreen
import com.misgastos.app.ui.MoreScreen
import com.misgastos.app.ui.theme.MisGastosTheme
import com.misgastos.app.ui.theme.Primary

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val trace = Log.getStackTraceString(throwable)
            try {
                val intent = Intent(this, CrashActivity::class.java).apply {
                    putExtra("trace", trace)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } finally {
                android.os.Process.killProcess(android.os.Process.myPid())
                kotlin.system.exitProcess(1)
            }
        }

        setContent {
            MisGastosTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MisGastosApp(viewModel)
                }
            }
        }
    }
}

private enum class Tab(val label: String) {
    DASHBOARD("Dashboard"),
    HISTORY("Historial"),
    BUDGETS("Presupuestos"),
    MORE("Más")
}

@Composable
fun MisGastosApp(viewModel: ExpenseViewModel) {
    var currentTab by remember { mutableStateOf(Tab.DASHBOARD) }
    var showAddMovement by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMovement = true },
                containerColor = Primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar movimiento", tint = Color.White)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = currentTab == Tab.DASHBOARD,
                    onClick = { currentTab = Tab.DASHBOARD },
                    icon = { Icon(Icons.Filled.DonutLarge, contentDescription = null) },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == Tab.HISTORY,
                    onClick = { currentTab = Tab.HISTORY },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == Tab.BUDGETS,
                    onClick = { currentTab = Tab.BUDGETS },
                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
                    label = { Text("Presupuestos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == Tab.MORE,
                    onClick = { currentTab = Tab.MORE },
                    icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = null) },
                    label = { Text("Más") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = Primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                Tab.DASHBOARD -> DashboardScreen(viewModel)
                Tab.HISTORY -> HistoryScreen(viewModel)
                Tab.BUDGETS -> BudgetsScreen(viewModel)
                Tab.MORE -> MoreScreen(viewModel)
            }
        }
    }

    if (showAddMovement) {
        AddMovementDialog(viewModel = viewModel, onDismiss = { showAddMovement = false })
    }
}
