package com.agencia.atenas.ui

import com.agencia.atenas.data.UsuarioEntity

/** Datos que observa la pantalla; no realiza consultas ni escribe en la base. */
data class PerfilUiState(
    val usuario: UsuarioEntity? = null,
    val oportunidadesVisibles: Int = 0,
    val cargando: Boolean = true,
    val mensaje: String = ""
)
