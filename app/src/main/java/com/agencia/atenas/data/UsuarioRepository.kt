package com.agencia.atenas.data

import androidx.room.withTransaction
import java.security.MessageDigest

/**
 * Se conserva el acceso local ficticio del curso, ahora consultando Room.
 * Las cuentas iniciales son datos de prueba: no son la condición del login.
 * Cambiar el rol o desactivar una fila cambia la autorización sin cambiar Kotlin.
 */
class UsuarioRepository(private val db: AppDatabase) {
    suspend fun autenticar(login: String, password: String): UsuarioEntity? {
        prepararCuentasDePrueba()
        val usuario = db.usuarioDao().buscarPorLogin(login.trim()) ?: return null
        if (!usuario.activo || usuario.rol !in listOf(Roles.EJECUTIVO, Roles.SUPERVISOR)) {
            return null
        }
        return usuario.takeIf { it.passwordHash == hashPassword(password) }
    }

    suspend fun prepararCuentasDePrueba() {
        // La transacción evita que dos intentos simultáneos creen datos a medias.
        // Se consulta por login: no se presupone que un usuario siempre tenga cierto id.
        db.withTransaction {
            cuentasIniciales().forEach { cuenta ->
                if (db.usuarioDao().buscarPorLogin(cuenta.usuario) == null) {
                    db.usuarioDao().insertarSiNoExiste(cuenta)
                }
            }
        }
    }

    // El perfil observa la cuenta actual, no una identidad elegida por la pantalla.
    fun observarCuentaActual(): kotlinx.coroutines.flow.Flow<UsuarioEntity?> {
        val id = checkNotNull(com.agencia.atenas.utils.SesionActual.usuarioId) { "Inicia sesión nuevamente" }
        return db.usuarioDao().observar(id)
    }

    fun ejecutivos() = db.usuarioDao().observarEjecutivos()
}

fun cuentasIniciales(): List<UsuarioEntity> = listOf(
    UsuarioEntity(usuario = "admin", nombre = "Supervisor de prueba",
        passwordHash = hashPassword("1234"), rol = Roles.SUPERVISOR),
    UsuarioEntity(usuario = "ejecutivo1", nombre = "Ejecutivo de prueba 1",
        passwordHash = hashPassword("1234"), rol = Roles.EJECUTIVO),
    UsuarioEntity(usuario = "ejecutivo2", nombre = "Ejecutivo de prueba 2",
        passwordHash = hashPassword("1234"), rol = Roles.EJECUTIVO),
    UsuarioEntity(usuario = "ejecutivo3", nombre = "Ejecutivo de prueba 3",
        passwordHash = hashPassword("1234"), rol = Roles.EJECUTIVO)
)

// No se guarda el texto de la contraseña en la fila. Este hash sencillo es SOLO para
// cuentas ficticias locales; una autenticación real requiere otro diseño de seguridad.
fun hashPassword(password: String): String = MessageDigest.getInstance("SHA-256")
    .digest(password.toByteArray(Charsets.UTF_8))
    .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
