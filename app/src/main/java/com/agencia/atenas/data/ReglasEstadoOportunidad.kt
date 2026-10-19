package com.agencia.atenas.data

import com.agencia.atenas.utils.SesionActual

/**
 * Opciones de cambio para la UI y autorización para el Repository.
 *
 * CAMBIO: los estados de gestión ya no forman una escalera obligatoria. Puede haberse
 * realizado una llamada o enviado una propuesta antes de actualizar la app. Por eso se
 * permite seleccionar directamente otro estado de gestión sin registrar pasos ficticios.
 *
 * Se conservan restricciones comerciales: solo el supervisor confirma/descarta, una
 * activación debe estar confirmada antes de marcarla realizada y no se reabre lo cerrado.
 * Esta política es una decisión del proyecto y debe poder explicarse en la defensa.
 */
fun estadosPermitidos(
    actual: EstadoOportunidad,
    esSupervisor: Boolean = SesionActual.esSupervisor
): List<EstadoOportunidad> {
    // Estados de trabajo que cualquiera de los dos roles puede actualizar mientras
    // gestiona una oportunidad. El orden de la lista es solo el orden visual del menú.
    val estadosDeGestion = listOf(
        EstadoOportunidad.IDENTIFICADO,
        EstadoOportunidad.CONTACTO_PENDIENTE,
        EstadoOportunidad.CONTACTO_REALIZADO,
        EstadoOportunidad.PROPUESTA_ENVIADA,
        EstadoOportunidad.EN_ESPERA,
        EstadoOportunidad.REQUIERE_SEGUIMIENTO
    )

    val opciones = when (actual) {
        // No ofrecemos una reapertura implícita: requeriría una regla propia y autorización.
        EstadoOportunidad.CERRADO -> emptyList()

        // Ya existe un resultado. Se permite cerrar, no volver a fases de contacto.
        EstadoOportunidad.DESCARTADO,
        EstadoOportunidad.ACTIVACION_REALIZADA -> listOf(EstadoOportunidad.CERRADO)

        EstadoOportunidad.CONFIRMADO -> {
            // Los ejecutivos pueden registrar que la activación confirmada se realizó.
            // Descartar sigue siendo una decisión reservada al supervisor.
            if (esSupervisor) {
                listOf(EstadoOportunidad.ACTIVACION_REALIZADA, EstadoOportunidad.DESCARTADO)
            } else {
                listOf(EstadoOportunidad.ACTIVACION_REALIZADA)
            }
        }

        else -> {
            if (esSupervisor) {
                estadosDeGestion + listOf(
                    EstadoOportunidad.CONFIRMADO,
                    EstadoOportunidad.DESCARTADO
                )
            } else {
                estadosDeGestion
            }
        }
    }

    // No mostrar el estado actual evita escrituras e historial sin un cambio real.
    // Repository vuelve a comprobar esta misma lista con el rol vigente leído de Room.
    return opciones.filter { destino -> destino != actual }
}
