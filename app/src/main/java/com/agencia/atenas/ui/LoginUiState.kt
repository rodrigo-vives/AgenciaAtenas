package com.agencia.atenas.ui

/**
 * Estado de la pantalla de login.
 * Guarda lo que el usuario escribe y los errores de validación.
 */
data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val errorUsuario: Boolean = false,
    val errorPassword: Boolean = false,
    val mensajeError: String = "",
    val cargando: Boolean = false
)