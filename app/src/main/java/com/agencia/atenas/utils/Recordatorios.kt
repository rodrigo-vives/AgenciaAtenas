package com.agencia.atenas.utils

/**
 * Una acción sin resolver es atrasada si su fecha es anterior a hoy; la de hoy
 * vence hoy y las posteriores siguen pendientes. No usamos el estado comercial
 * (En espera, Confirmado...) para suponer que una llamada ya fue realizada.
 */
enum class SituacionRecordatorio { ATRASADO, HOY, PENDIENTE }

fun situacionRecordatorio(
    fechaProxima: String,
    resuelto: Boolean = false,
    hoy: String = Fechas.hoy()
): SituacionRecordatorio? {
    if (resuelto) return null
    // Validar ISO antes de comparar texto. Fechas.visible+aIso comprueba fechas reales
    // y evita presentar como atrasado un valor antiguo que estaba mal escrito.
    if (Fechas.aIso(Fechas.visible(fechaProxima)) != fechaProxima || fechaProxima.isBlank()) return null
    if (Fechas.aIso(Fechas.visible(hoy)) != hoy || hoy.isBlank()) return null
    return when {
        fechaProxima < hoy -> SituacionRecordatorio.ATRASADO
        fechaProxima == hoy -> SituacionRecordatorio.HOY
        else -> SituacionRecordatorio.PENDIENTE
    }
}
