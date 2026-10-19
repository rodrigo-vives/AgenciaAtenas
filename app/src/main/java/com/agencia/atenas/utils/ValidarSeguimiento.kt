package com.agencia.atenas.utils

/**
 * Comparte las reglas entre ViewModel y Repository. Así un guardado no depende
 * solamente de que la pantalla haya validado. No consulta Room ni modifica el borrador.
 */
fun validarSeguimiento(
    accion: String,
    resultado: String,
    proximaAccion: String,
    fechaProxima: String,
    observaciones: String
): Map<String, String> {
    val errores = mutableMapOf<String, String>()
    if (accion.isBlank()) {
        errores["accion"] = "Indica la acción realizada"
    }
    if (resultado.isBlank()) {
        errores["resultado"] = "Indica el resultado"
    }

    val textos = mapOf(
        "accion" to accion,
        "resultado" to resultado,
        "proximaAccion" to proximaAccion,
        "observaciones" to observaciones
    )
    for ((clave, texto) in textos) {
        if (texto.length > 2000) {
            errores[clave] = "Máximo 2000 caracteres"
        }
    }

    if (fechaProxima.isNotBlank() && Fechas.aIso(fechaProxima) == null) {
        errores["fechaProxima"] = "Usa una fecha real DD/MM/AAAA"
    }
    // Una tarea programada necesita tanto qué hacer como cuándo; ambas pueden omitirse
    // si solo se está dejando constancia de una gestión que ya ocurrió.
    if (proximaAccion.isNotBlank() && fechaProxima.isBlank()) {
        errores["fechaProxima"] = "Indica cuándo realizarla"
    }
    if (fechaProxima.isNotBlank() && proximaAccion.isBlank()) {
        errores["proximaAccion"] = "Indica qué acción realizar"
    }
    // Se permiten fechas pasadas para registrar gestiones atrasadas reales.
    return errores
}
