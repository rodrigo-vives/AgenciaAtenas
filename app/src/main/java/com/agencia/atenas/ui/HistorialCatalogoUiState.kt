package com.agencia.atenas.ui

import com.agencia.atenas.data.OportunidadDetalle
import com.agencia.atenas.data.HistorialContactoItem

/** Datos que observa la pantalla; no realiza consultas ni escribe en la base. */
data class HistorialCatalogoUiState(
    val nombre: String = "",
    val eventos: List<OportunidadDetalle> = emptyList(),
    val historial: List<HistorialContactoItem> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String = ""
)
