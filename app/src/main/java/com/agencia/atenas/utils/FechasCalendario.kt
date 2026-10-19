package com.agencia.atenas.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class IntervaloCalendario(val inicio: Long, val termino: Long, val todoElDia: Boolean)

/**
 * Convierte las fechas ISO guardadas en Room a milisegundos que entiende Calendar.
 * Sin horario, exportamos días completos: no inventamos una hora de reunión.
 * Calendar exige que el fin de un evento de día completo sea EXCLUSIVO y esté en UTC.
 * Con horario, usamos la zona del dispositivo, como al escribir una cita manualmente.
 */
fun intervaloCalendario(fechaInicio: String, fechaTermino: String, horario: String,
    zona: TimeZone = TimeZone.getDefault()): IntervaloCalendario {
    val todoElDia = horario.isBlank()
    val zonaEvento = if (todoElDia) TimeZone.getTimeZone("UTC") else zona
    fun fecha(valor: String, hora: String): Long {
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(valor)) { "Revisa las fechas del evento" }
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply {
            isLenient = false
            timeZone = zonaEvento
        }
        return requireNotNull(formato.parse("$valor $hora")) { "Fecha no válida" }.time
    }
    val fin = fechaTermino.ifBlank { fechaInicio }
    require(fin >= fechaInicio) { "El término no puede ser anterior al inicio" }
    if (todoElDia) {
        val inicio = fecha(fechaInicio, "00:00")
        val siguienteDia = Calendar.getInstance(zonaEvento).apply {
            timeInMillis = fecha(fin, "00:00")
            add(Calendar.DAY_OF_MONTH, 1)
        }
        return IntervaloCalendario(inicio, siguienteDia.timeInMillis, true)
    }
    require(Regex("([01][0-9]|2[0-3]):[0-5][0-9] a ([01][0-9]|2[0-3]):[0-5][0-9]").matches(horario.trim())) {
        "Revisa el horario: HH:mm a HH:mm"
    }
    val horas = horario.trim().split(" a ")
    val inicio = fecha(fechaInicio, horas[0])
    val termino = fecha(fin, horas[1])
    require(termino > inicio) { "La hora de término debe ser posterior al inicio" }
    return IntervaloCalendario(inicio, termino, false)
}
