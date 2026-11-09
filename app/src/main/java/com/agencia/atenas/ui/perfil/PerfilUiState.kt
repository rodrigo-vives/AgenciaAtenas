package com.agencia.atenas.ui.perfil

import com.agencia.atenas.data.usuarios.UsuarioEntity

/** Datos que observa la pantalla; no realiza consultas ni escribe en la base. */
data class PerfilUiState(
    val usuario: UsuarioEntity? = null,
    val oportunidadesVisibles: Int = 0,
    val cargando: Boolean = true,
    val mensaje: String = ""
)
