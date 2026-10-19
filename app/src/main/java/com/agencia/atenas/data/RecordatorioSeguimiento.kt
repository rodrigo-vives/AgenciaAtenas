package com.agencia.atenas.data

/**
 * Resultado de una consulta, no una tabla nueva. Usa el responsable ACTUAL de la
 * oportunidad: al reasignarla, sus tareas abiertas pasan al ejecutivo que la recibe.
 * El autor histórico del seguimiento permanece en SeguimientoEntity.responsableId.
 */
data class RecordatorioSeguimiento(
    val seguimientoId: Int,
    val oportunidadId: Int,
    val nombreOportunidad: String,
    val responsableId: Int,
    val responsable: String,
    val proximaAccion: String,
    val fechaProxima: String
)
