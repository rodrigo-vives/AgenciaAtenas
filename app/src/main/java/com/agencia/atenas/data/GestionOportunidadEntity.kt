package com.agencia.atenas.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Oportunidad y datos de su evento. Cada fila representa una gestión comercial.
 * CAMBIO: responsableId y organizacionId reemplazan nombres y datos copiados.
 * Las claves foráneas exigen registros existentes y los índices ayudan a consultarlos.
 * RESTRICT evita eliminar accidentalmente una organización/usuario que aún se utiliza.
 */
@Entity(
    tableName = "gestiones_oportunidad",
    foreignKeys = [
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"],
            childColumns = ["responsableId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = OrganizacionEntity::class, parentColumns = ["id"],
            childColumns = ["organizacionId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("responsableId"), Index("organizacionId")]
)
data class GestionOportunidadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreOportunidad: String,
    val tipoOportunidad: String,
    val descripcion: String,
    val fuenteInformacion: String,
    val fechaDeteccion: String,
    val prioridad: String,
    val potencialComercial: String,
    val fechaInicio: String,
    val fechaTermino: String,
    val horario: String,
    val comuna: String,
    val region: String,
    val publicoEsperado: String,
    val cantidadAsistentes: Int?,
    val condicionesParticipacion: String,
    val costosReferenciales: String,
    val estado: EstadoOportunidad = EstadoOportunidad.IDENTIFICADO,
    val historialObservaciones: String = "",
    val ubicacionGeneral: String = "",
    val publicoObjetivo: String = "",
    val requisitos: String = "",
    val espaciosDisponibles: String = "",
    val fechaUltimaActualizacion: String,
    // El responsable es obligatorio. La organización puede desconocerse al detectar el evento.
    val responsableId: Int,
    val organizacionId: Int?
)
