package com.agencia.atenas.ui

import com.agencia.atenas.data.*

/**
 * Guarda la oportunidad consultada, el historial y el borrador de una nueva gestión.
 * Los errores se relacionan con claves de campos para mostrarlos debajo del campo correcto.
 */

data class DetalleUiState(
    val item: OportunidadDetalle? = null,
    val resultados: List<ResultadoActivacionDetalle> = emptyList(),
    val historial: List<SeguimientoDetalle> = emptyList(),
    val cargando: Boolean = true,
    val ocupado: Boolean = false,
    val mensaje: String = "",
    val esError: Boolean = false,
    val accion: String = "",
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProxima: String = "",
    val observaciones: String = "",
    val errores: Map<String, String> = emptyMap(),
    // null indica gestión libre. Un ID indica que el resultado resolverá esa tarea.
    val pendienteEnResolucionId: Int? = null,
    val fechaAccionProgramada: String = ""
)
