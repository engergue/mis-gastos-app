package com.misgastos.app.data

import java.time.LocalDate
import java.time.ZoneId

object SeedData {

    // Datos reales cargados desde la hoja de calculo original (Enero - Septiembre)
    private val raw: Map<String, Map<String, Double>> = mapOf(
        "Enero" to mapOf("arriendo" to 1525000.0, "luz" to 182560.0, "agua" to 347180.0, "gas" to 53840.0, "internet" to 153330.0, "tc_exito" to 1371483.0, "tc_rappi" to 876479.0, "telefonia" to 184873.0, "comida" to 723432.0, "netflix" to 29900.0, "univ_smart" to 700000.0, "adobe" to 166600.0, "capcut" to 0.0, "google" to 8900.0, "otro" to 0.0),
        "Febrero" to mapOf("arriendo" to 1600000.0, "luz" to 157570.0, "agua" to 0.0, "gas" to 48850.0, "internet" to 148650.0, "tc_exito" to 1227825.0, "tc_rappi" to 880648.0, "telefonia" to 211470.0, "comida" to 696700.0, "netflix" to 29900.0, "univ_smart" to 293960.0, "adobe" to 166600.0, "capcut" to 0.0, "google" to 8900.0, "otro" to 0.0),
        "Marzo" to mapOf("arriendo" to 1600000.0, "luz" to 180750.0, "agua" to 392270.0, "gas" to 41360.0, "internet" to 254240.0, "tc_exito" to 1400000.0, "tc_rappi" to 1253653.0, "telefonia" to 135726.0, "comida" to 451676.0, "netflix" to 29900.0, "univ_smart" to 294100.0, "adobe" to 166600.0, "capcut" to 0.0, "google" to 8900.0, "otro" to 0.0),
        "Abril" to mapOf("arriendo" to 1600000.0, "luz" to 183980.0, "agua" to 0.0, "gas" to 45050.0, "internet" to 288183.0, "tc_exito" to 827941.0, "tc_rappi" to 807660.0, "telefonia" to 155726.0, "comida" to 697160.0, "netflix" to 0.0, "univ_smart" to 294100.0, "adobe" to 166600.0, "capcut" to 0.0, "google" to 0.0, "otro" to 24900.0),
        "Mayo" to mapOf("arriendo" to 1600000.0, "luz" to 187390.0, "agua" to 348900.0, "gas" to 41490.0, "internet" to 254240.0, "tc_exito" to 509998.0, "tc_rappi" to 834179.0, "telefonia" to 145726.0, "comida" to 689246.0, "netflix" to 29900.0, "univ_smart" to 294100.0, "adobe" to 166600.0, "capcut" to 59900.0, "google" to 8900.0, "otro" to 24900.0),
        "Junio" to mapOf("arriendo" to 1600000.0, "luz" to 204710.0, "agua" to 0.0, "gas" to 50490.0, "internet" to 261440.0, "tc_exito" to 2900000.0, "tc_rappi" to 940237.0, "telefonia" to 78734.0, "comida" to 917439.0, "netflix" to 29900.0, "univ_smart" to 294100.0, "adobe" to 166600.0, "capcut" to 59900.0, "google" to 8900.0, "otro" to 102800.0),
        "Julio" to mapOf("arriendo" to 1600000.0, "luz" to 201870.0, "agua" to 373920.0, "gas" to 58460.0, "internet" to 261533.0, "tc_exito" to 474863.0, "tc_rappi" to 902447.0, "telefonia" to 78734.0, "comida" to 789210.0, "netflix" to 29900.0, "univ_smart" to 294100.0, "adobe" to 166600.0, "capcut" to 59900.0, "google" to 8900.0, "otro" to 38890.0),
        "Agosto" to mapOf("arriendo" to 1600000.0, "luz" to 217810.0, "agua" to 0.0, "gas" to 51190.0, "internet" to 261440.0, "tc_exito" to 1147693.0, "tc_rappi" to 901630.0, "telefonia" to 78734.0, "comida" to 903894.0, "netflix" to 29900.0, "univ_smart" to 294100.0, "adobe" to 166600.0, "capcut" to 59900.0, "google" to 8900.0, "otro" to 24900.0),
        "Septiembre" to mapOf("arriendo" to 1600000.0, "luz" to 0.0, "agua" to 568590.0, "gas" to 0.0, "internet" to 148650.0, "tc_exito" to 1419260.0, "tc_rappi" to 902607.0, "telefonia" to 109980.0, "comida" to 987591.0, "netflix" to 29900.0, "univ_smart" to 294100.0, "adobe" to 166600.0, "capcut" to 59900.0, "google" to 8900.0, "otro" to 42890.0)
    )

    private val monthIndex = mapOf(
        "Enero" to 1, "Febrero" to 2, "Marzo" to 3, "Abril" to 4, "Mayo" to 5, "Junio" to 6,
        "Julio" to 7, "Agosto" to 8, "Septiembre" to 9, "Octubre" to 10, "Noviembre" to 11, "Diciembre" to 12
    )

    /** Convierte los totales de ejemplo en movimientos individuales (solo para instalaciones nuevas). */
    fun toMovementList(): List<Movement> {
        val year = LocalDate.now().year
        val list = mutableListOf<Movement>()
        raw.forEach { (month, cats) ->
            val monthNum = monthIndex[month] ?: 1
            val dateMillis = LocalDate.of(year, monthNum, 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            cats.forEach { (key, amount) ->
                if (amount > 0.0) {
                    list.add(
                        Movement(
                            type = MovementType.GASTO.name,
                            categoryKey = key,
                            amount = amount,
                            dateMillis = dateMillis,
                            note = "Ejemplo"
                        )
                    )
                }
            }
        }
        return list
    }
}
