package com.agencia.atenas.utils

import com.agencia.atenas.data.EstadoOportunidad
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Devuelve los días restantes cuando corresponde avisar; null significa que no hay alerta.
 * Se propone una ventana de hoy a siete días inclusive porque el Word no fija un plazo.
 * hoy se recibe como parámetro opcional para poder probar fechas fijas sin depender del reloj.
 */
fun diasDeAlertaEvento(
    fechaInicio: String,
    estado: EstadoOportunidad,
    hoy: String = Fechas.hoy()
): Int? {
    // No tiene sentido pedir preparación para una oportunidad descartada o ya finalizada.
    val estadosFinalizados = setOf(
        EstadoOportunidad.DESCARTADO,
        EstadoOportunidad.ACTIVACION_REALIZADA,
        EstadoOportunidad.CERRADO
    )
    if (estado in estadosFinalizados) {
        return null
    }

    // Room guarda AAAA-MM-DD. Se exige ese formato completo para no interpretar textos
    // antiguos o incompletos como fechas válidas y producir una alerta equivocada.
    val patron = Regex("\\d{4}-\\d{2}-\\d{2}")
    if (!patron.matches(fechaInicio) || !patron.matches(hoy)) {
        return null
    }

    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        // Son fechas sin hora. UTC evita que un cambio de horario de verano convierta
        // dos días consecutivos en intervalos de 23 o 25 horas al calcular la diferencia.
        // La fecha de hoy ya se obtuvo en la zona del dispositivo mediante Fechas.hoy().
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val dias = try {
        val inicio = formato.parse(fechaInicio) ?: return null
        val fechaActual = formato.parse(hoy) ?: return null
        (inicio.time - fechaActual.time) / 86_400_000L
    } catch (_: Exception) {
        // Una fecha imposible no debe impedir que se muestre el resto de la lista.
        return null
    }

    // Los negativos ya pasaron; los mayores que siete todavía no son próximos.
    return if (dias in 0L..7L) dias.toInt() else null
}
