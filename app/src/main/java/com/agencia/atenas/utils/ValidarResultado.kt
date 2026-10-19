package com.agencia.atenas.utils

import com.agencia.atenas.ui.ResultadoUiState

/** Resultado real de la demostración ficticia: no se registra como ocurrido en el futuro. */
fun validarResultado(estado: ResultadoUiState, hoy: String = Fechas.hoy()): Map<String, String> {
    val errores = mutableMapOf<String, String>()
    val fecha = Fechas.aIso(estado.fechaRealizacion)
    if (fecha == null) {
        errores["fechaRealizacion"] = "Indica una fecha real DD/MM/AAAA"
    } else if (fecha > hoy) {
        errores["fechaRealizacion"] = "La activación todavía no puede haber ocurrido en esa fecha"
    }
    if (estado.evaluacion.isBlank()) errores["evaluacion"] = "Describe el resultado de la activación"
    for ((clave, texto) in mapOf("evaluacion" to estado.evaluacion,
        "observaciones" to estado.observaciones, "aprendizajes" to estado.aprendizajes)) {
        if (texto.length > 2000) errores[clave] = "Máximo 2000 caracteres"
    }
    // Son indicadores opcionales del caso. Desconocido se guarda como null, no como cero.
    for ((clave, texto) in mapOf("asistentes" to estado.asistentes,
        "contactosGenerados" to estado.contactosGenerados, "interesados" to estado.interesados,
        "inscripciones" to estado.inscripciones)) {
        if (texto.isNotBlank()) {
            val cantidad = texto.trim().toIntOrNull()
            if (cantidad == null || cantidad < 0) errores[clave] = "Ingresa un entero igual o mayor que cero"
        }
    }
    return errores
}
