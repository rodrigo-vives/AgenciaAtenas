package com.agencia.atenas.utils

/**
 * Objeto singleton que guarda el usuario logueado actualmente.
 *
 * Se llena al hacer login exitoso (LoginViewModel).
 * Se lee al crear una gestión (CrearViewModel) para asignar el responsable.
 * Se limpia al cerrar sesión (ListaScreen).
 */

object SesionActual {
    var usuario: String = ""
}