package com.agencia.atenas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla plana que guarda una gestión completa:
 * oportunidad + evento + organización + contacto + estado + seguimiento.
 *
 * No hay relaciones ni tablas secundarias. Todo vive aquí.
 */
@Entity(tableName = "gestiones_oportunidad")
data class GestionOportunidadEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // --- Datos de la oportunidad ---
    val nombreOportunidad: String,
    val tipoOportunidad: String,
    val descripcion: String,
    val fuenteInformacion: String,
    val fechaDeteccion: String,
    val responsable: String,
    val prioridad: String,
    val potencialComercial: String,

    // --- Datos del evento ---
    val fechaInicio: String,
    val fechaTermino: String,
    val horario: String,
    val comuna: String,
    val region: String,
    val publicoEsperado: String,
    val cantidadAsistentes: Int,
    val condicionesParticipacion: String,
    val costosReferenciales: String,

    // --- Datos de la organización ---
    val nombreOrganizacion: String,
    val tipoOrganizacion: String,
    val rubroOrganizacion: String,

    // --- Datos del contacto ---
    val nombreContacto: String,
    val cargoContacto: String,
    val areaContacto: String,
    val correoContacto: String,
    val telefonoContacto: String,
    val medioContactoPreferente: String,

    // --- Estado y seguimiento ---
    val estado: EstadoOportunidad = EstadoOportunidad.IDENTIFICADO,
    val historialObservaciones: String = "",
    val fechaUltimaActualizacion: String
)