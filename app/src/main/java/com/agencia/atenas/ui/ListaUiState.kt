package com.agencia.atenas.ui

import com.agencia.atenas.data.GestionOportunidadEntity

/**
 * Estado de la pantalla de lista.
 * Guarda la lista de gestiones, el texto de búsqueda, el filtro por estado
 * y un mensaje de error opcional.
 */
data class ListaUiState(
    val gestiones: List<GestionOportunidadEntity> = emptyList(),
    val textoBusqueda: String = "",
    val filtroEstado: String = "",
    val cargando: Boolean = false,
    val mensajeError: String = ""
)