package com.agencia.atenas.ui

/**
 * Datos temporales del formulario de organización/contacto. organizacionId es la clave
 * de la organización elegida para asociar un contacto, no su nombre escrito libremente.
 */

data class CatalogoUiState(
    val nombre: String = "",
    val tipo: String = "",
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = "",
    val organizacionId: Int = 0,
    val cargo: String = "",
    val area: String = "",
    val correo: String = "",
    val telefono: String = "",
    val medio: String = "",
    val errores: Map<String, String> = emptyMap(),
    val mensaje: String = "",
    val esError: Boolean = false,
    val guardando: Boolean = false
)
