package com.agencia.atenas.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
// Colores semánticos de las tarjetas. Se centralizan aquí para que ListaScreen no
// repita valores hexadecimales ni mezcle las reglas de fecha con la definición de colores.
val FondoOportunidadConfirmada = Color(0xFFC8E6C9)
val FondoOportunidadEnEspera = Color(0xFFFFF9C4)
val FondoOportunidadDescartada = Color(0xFFFFCDD2)
val FondoOportunidadNormal = Color(0xFFE0E0E0)
val FondoEventoProximo = Color(0xFFFFE0B2)
val FondoEventoHoy = Color(0xFFFFCDD2)
// Las tarjetas tienen fondo claro incluso en modo oscuro, por eso su texto es oscuro.
val TextoTarjetaOportunidad = Color(0xFF1D1B20)

// Avisos de operación: pares claros con texto oscuro, legibles también en tema oscuro.
val FondoExito = FondoOportunidadConfirmada
val TextoExito = TextoTarjetaOportunidad
val FondoAdvertencia = FondoEventoProximo
val TextoAdvertencia = TextoTarjetaOportunidad
