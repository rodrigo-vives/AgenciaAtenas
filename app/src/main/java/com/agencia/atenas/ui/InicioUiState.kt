package com.agencia.atenas.ui

import com.agencia.atenas.data.OportunidadDetalle
import com.agencia.atenas.data.RecordatorioSeguimiento

/** Una sola instantánea mantiene coherentes las oportunidades y sus tareas visibles. */
data class InicioUiState(
    val oportunidades: List<OportunidadDetalle> = emptyList(),
    val sinSeguimiento: List<OportunidadDetalle> = emptyList(),
    val recordatorios: List<RecordatorioSeguimiento> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String = "",
    val esError: Boolean = false,
    val ocupado: Boolean = false
)
