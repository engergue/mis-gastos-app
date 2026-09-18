package com.misgastos.app.data

data class Category(val key: String, val label: String, val colorHex: Long)

val CATEGORIES = listOf(
    Category("arriendo", "Arriendo", 0xFF4F7CFF),
    Category("luz", "Luz", 0xFF22A55A),
    Category("agua", "Agua", 0xFF06B6D4),
    Category("gas", "Gas", 0xFFF5A524),
    Category("internet", "Internet", 0xFF8B5CF6),
    Category("tc_exito", "TC Éxito", 0xFFE5484D),
    Category("tc_rappi", "TC Rappi", 0xFFF472B6),
    Category("telefonia", "Telefonía", 0xFF84CC16),
    Category("comida", "Comida", 0xFFFB923C),
    Category("netflix", "Netflix", 0xFF14B8A6),
    Category("univ_smart", "Univ. / Smart", 0xFFA855F7),
    Category("adobe", "Adobe", 0xFFEAB308),
    Category("capcut", "Capcut", 0xFF3B82F6),
    Category("google", "Google", 0xFFEC4899),
    Category("otro", "Otro gasto", 0xFF94A3B8)
)

val MESES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)
