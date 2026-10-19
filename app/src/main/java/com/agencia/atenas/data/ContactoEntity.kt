package com.agencia.atenas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/*
 * Por qué hay id y organizacionId:
 * id identifica al contacto. organizacionId identifica su organización y permite relacionarlos
 * sin copiar el nombre de la empresa en cada contacto. El índice acelera búsquedas por esa clave.
 * La clave foránea hace que SQLite rechace un contacto cuyo organizacionId no existe.
 */


/**
 * Tabla de contactos. organizacionId es una clave foránea: el contacto debe pertenecer
 * a una organización existente. RESTRICT evita borrar una organización que aún tiene contactos.
 */

@Entity(tableName = "contactos",
    foreignKeys = [ForeignKey(
            entity = OrganizacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("organizacionId")])

data class ContactoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizacionId: Int,
    val nombre: String,
    val cargo: String,
    val area: String,
    val correo: String,
    val telefono: String,
    val medio: String
)
