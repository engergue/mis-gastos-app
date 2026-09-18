package com.misgastos.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.misgastos.app.ui.DashboardScreen
import com.misgastos.app.ui.EntryScreen
import com.misgastos.app.ui.ExpenseViewModel
import com.misgastos.app.ui.TableScreen
import com.misgastos.app.ui.theme.MisGastosTheme

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
    ENTRY("Registrar"),
    TABLE("Tabla")
}

@Composable
fun MisGastosApp(viewModel: ExpenseViewModel) {
    var currentTab by remember { mutableStateOf(Tab.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == Tab.DASHBOARD,
                    onClick = { currentTab = Tab.DASHBOARD },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentTab == Tab.ENTRY,
                    onClick = { currentTab = Tab.ENTRY },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    label = { Text("Registrar") }
                )
                NavigationBarItem(
                    selected = currentTab == Tab.TABLE,
                    onClick = { currentTab = Tab.TABLE },
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text("Tabla") }
                )
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                Tab.DASHBOARD -> DashboardScreen(viewModel)
                Tab.ENTRY -> EntryScreen(viewModel)
                Tab.TABLE -> TableScreen(viewModel)
            }
        }
    }
}
