package com.agencia.atenas.ui


/** Rutas de fotos y progreso; no guarda bitmaps pesados ni realiza consultas. */
data class EvidenciaUiState(
    val fotos: List<String> = emptyList(),
    val ocupado: Boolean = false,
    val esError: Boolean = false,
    val mensaje: String = ""
)
