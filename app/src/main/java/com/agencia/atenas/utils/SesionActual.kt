package com.agencia.atenas.utils

import com.agencia.atenas.data.Roles
import com.agencia.atenas.data.UsuarioEntity

/**
 * La sesión sigue siendo temporal: cerrar el proceso exige iniciar sesión otra vez.
 * CAMBIO: conserva el id y rol de la fila autenticada; no almacena contraseñas.
 * Repository vuelve a consultar el usuario en Room antes de autorizar escrituras.
 */
object SesionActual {
    var usuarioId: Int? = null
        private set
    var usuario: String = ""
        private set
    var rol: String = ""
        private set

    val esSupervisor: Boolean get() = rol == Roles.SUPERVISOR

    fun iniciar(cuenta: UsuarioEntity) {
        usuarioId = cuenta.id
        usuario = cuenta.usuario
        rol = cuenta.rol
    }

    fun cerrar() {
        usuarioId = null
        usuario = ""
        rol = ""
    }
}
