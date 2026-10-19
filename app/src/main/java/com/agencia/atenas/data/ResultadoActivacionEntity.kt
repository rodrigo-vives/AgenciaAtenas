package com.agencia.atenas.data

import androidx.room.*

/**
 * Complementa una gestión de tipo Resultado de activación: relación uno a uno.
 * Autor, oportunidad, evaluación y observaciones viven en Seguimiento; no se duplican.
 * Los indicadores opcionales conservan null cuando no se conocen.
 */
@Entity(tableName = "resultados_activacion", foreignKeys = [
    ForeignKey(entity = SeguimientoEntity::class, parentColumns = ["id"],
        childColumns = ["seguimientoId"], onDelete = ForeignKey.CASCADE)
])
data class ResultadoActivacionEntity(
    @PrimaryKey val seguimientoId: Int,
    val fechaRealizacion: String,
    val asistentes: Int?,
    val contactosGenerados: Int?,
    val interesados: Int?,
    val inscripciones: Int?,
    val aprendizajes: String
)

/** Lectura con JOIN; no constituye una segunda tabla con copias de los mismos datos. */
data class ResultadoActivacionDetalle(
    @Embedded val indicadores: ResultadoActivacionEntity,
    val oportunidadId: Int,
    val evaluacion: String,
    val observaciones: String,
    val responsable: String,
    val fechaRegistro: String
)

data class HistorialContactoItem(
    @Embedded val seguimiento: SeguimientoEntity,
    val nombreOportunidad: String,
    val responsable: String
)
