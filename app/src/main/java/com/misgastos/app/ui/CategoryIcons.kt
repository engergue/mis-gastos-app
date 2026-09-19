package com.misgastos.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Devuelve un ícono representativo para cada categoría (de gasto o de ingreso). */
fun iconForCategory(key: String): ImageVector = when (key) {
    "arriendo" -> Icons.Filled.Home
    "luz" -> Icons.Filled.Bolt
    "agua" -> Icons.Filled.WaterDrop
    "gas" -> Icons.Filled.LocalFireDepartment
    "internet" -> Icons.Filled.Wifi
    "tc_exito" -> Icons.Filled.CreditCard
    "tc_rappi" -> Icons.Filled.DeliveryDining
    "telefonia" -> Icons.Filled.Smartphone
    "comida" -> Icons.Filled.Restaurant
    "netflix" -> Icons.Filled.Tv
    "univ_smart" -> Icons.Filled.School
    "adobe" -> Icons.Filled.Palette
    "capcut" -> Icons.Filled.MovieCreation
    "google" -> Icons.Filled.Language
    "sueldo" -> Icons.Filled.AttachMoney
    "freelance" -> Icons.Filled.Work
    "ventas" -> Icons.Filled.Sell
    "regalo" -> Icons.Filled.CardGiftcard
    "otro_ingreso" -> Icons.Filled.MoreHoriz
    else -> Icons.Filled.MoreHoriz
}
