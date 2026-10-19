package com.agencia.atenas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Contacto principal elegido para una oportunidad (como máximo uno en este formulario).
 * La oportunidad puede tener organización sin contacto; esta fila existe solo al elegirlo.
 * Se separa porque Contacto ya determina su organización: así no duplicamos esa dependencia
 * dentro de la fila de oportunidad. Repository comprueba que ambas organizaciones coincidan.
 */
@Entity(
    tableName = "oportunidad_contacto",
    foreignKeys = [
        ForeignKey(entity = GestionOportunidadEntity::class, parentColumns = ["id"],
            childColumns = ["oportunidadId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ContactoEntity::class, parentColumns = ["id"],
            childColumns = ["contactoId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("contactoId")]
)
data class OportunidadContactoEntity(
    @PrimaryKey val oportunidadId: Int,
    val contactoId: Int
)
