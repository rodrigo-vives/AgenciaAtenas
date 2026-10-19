package com.agencia.atenas.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Usuario local persistente. usuario es único, pero las relaciones usan id para no
 * depender del texto del login. El rol se lee de esta tabla, no del nombre "admin".
 * activo permite conservar responsables históricos sin habilitarles el acceso.
 */
@Entity(tableName = "usuarios", indices = [Index(value = ["usuario"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuario: String,
    val nombre: String,
    val passwordHash: String,
    val rol: String,
    val activo: Boolean = true
)

// Constantes para evitar que cada pantalla escriba una variante distinta del mismo rol.
object Roles {
    const val EJECUTIVO = "EJECUTIVO"
    const val SUPERVISOR = "SUPERVISOR"
}
