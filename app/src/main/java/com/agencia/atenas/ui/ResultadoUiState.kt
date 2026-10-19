package com.agencia.atenas.ui

/** Borrador: los números son texto para permitir valores desconocidos sin inventar ceros. */
data class ResultadoUiState(
    val fechaRealizacion: String = "",
    val asistentes: String = "",
    val contactosGenerados: String = "",
    val interesados: String = "",
    val inscripciones: String = "",
    val evaluacion: String = "",
    val observaciones: String = "",
    val aprendizajes: String = "",
    val errores: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val mensaje: String = "",
    val esError: Boolean = false
)
