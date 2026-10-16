package com.agencia.atenas.data

/**
 * Estados posibles de una gestión de oportunidad.
 * Se usa en GestionOportunidadEntity y se persiste en Room
 * como String mediante Converters.kt.
 */
enum class EstadoOportunidad(val etiqueta: String) {
    IDENTIFICADO("Evento identificado"),
    CONTACTO_PENDIENTE("Contacto pendiente"),
    CONTACTO_REALIZADO("Contacto realizado"),
    PROPUESTA_ENVIADA("Propuesta enviada"),
    EN_ESPERA("En espera de respuesta"),
    CONFIRMADO("Participación confirmada"),
    DESCARTADO("Oportunidad descartada"),
    REQUIERE_SEGUIMIENTO("Requiere seguimiento"),
    ACTIVACION_REALIZADA("Activación realizada"),
    CERRADO("Oportunidad cerrada")
}