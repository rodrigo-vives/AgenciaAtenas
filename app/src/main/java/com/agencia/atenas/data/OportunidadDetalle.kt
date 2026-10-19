package com.agencia.atenas.data

import androidx.room.Embedded
import androidx.room.Ignore

/**
 * Resultado de lectura de un JOIN, NO una tabla. Las pantallas necesitan nombres;
 * Room los obtiene de sus tablas originales y arma este objeto solo en memoria.
 * Embedded agrupa las columnas propias de oportunidad. Los getters mantienen legible
 * el código visual existente sin volver a guardar nombres en la base.
 */
data class OportunidadDetalle(
    @Embedded val oportunidad: GestionOportunidadEntity,
    val responsable: String,
    val contactoId: Int?,
    val nombreOrganizacion: String,
    val tipoOrganizacion: String,
    val rubroOrganizacion: String,
    val nombreContacto: String,
    val cargoContacto: String,
    val areaContacto: String,
    val correoContacto: String,
    val telefonoContacto: String,
    val medioContactoPreferente: String
) {
    @get:Ignore
    val responsableId: Int get() = oportunidad.responsableId
    @get:Ignore
    val organizacionId: Int? get() = oportunidad.organizacionId
    @get:Ignore
    val id: Int get() = oportunidad.id
    @get:Ignore
    val nombreOportunidad: String get() = oportunidad.nombreOportunidad
    @get:Ignore
    val tipoOportunidad: String get() = oportunidad.tipoOportunidad
    @get:Ignore
    val descripcion: String get() = oportunidad.descripcion
    @get:Ignore
    val fuenteInformacion: String get() = oportunidad.fuenteInformacion
    @get:Ignore
    val fechaDeteccion: String get() = oportunidad.fechaDeteccion
    @get:Ignore
    val prioridad: String get() = oportunidad.prioridad
    @get:Ignore
    val potencialComercial: String get() = oportunidad.potencialComercial
    @get:Ignore
    val fechaInicio: String get() = oportunidad.fechaInicio
    @get:Ignore
    val fechaTermino: String get() = oportunidad.fechaTermino
    @get:Ignore
    val horario: String get() = oportunidad.horario
    @get:Ignore
    val comuna: String get() = oportunidad.comuna
    @get:Ignore
    val region: String get() = oportunidad.region
    @get:Ignore
    val publicoEsperado: String get() = oportunidad.publicoEsperado
    @get:Ignore
    val cantidadAsistentes: Int? get() = oportunidad.cantidadAsistentes
    @get:Ignore
    val condicionesParticipacion: String get() = oportunidad.condicionesParticipacion
    @get:Ignore
    val costosReferenciales: String get() = oportunidad.costosReferenciales
    @get:Ignore
    val estado: EstadoOportunidad get() = oportunidad.estado
    @get:Ignore
    val historialObservaciones: String get() = oportunidad.historialObservaciones
    @get:Ignore
    val ubicacionGeneral: String get() = oportunidad.ubicacionGeneral
    @get:Ignore
    val publicoObjetivo: String get() = oportunidad.publicoObjetivo
    @get:Ignore
    val requisitos: String get() = oportunidad.requisitos
    @get:Ignore
    val espaciosDisponibles: String get() = oportunidad.espaciosDisponibles
    @get:Ignore
    val fechaUltimaActualizacion: String get() = oportunidad.fechaUltimaActualizacion
}
