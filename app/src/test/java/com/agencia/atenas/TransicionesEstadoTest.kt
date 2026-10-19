package com.agencia.atenas

import com.agencia.atenas.data.EstadoOportunidad
import com.agencia.atenas.data.estadosPermitidos
import org.junit.Assert.*
import org.junit.Test

class TransicionesEstadoTest {
    @Test fun ejecutivoPuedeSaltarEntreEstadosDeGestion() {
        val opciones = estadosPermitidos(EstadoOportunidad.CONTACTO_PENDIENTE, false)
        assertTrue(opciones.contains(EstadoOportunidad.PROPUESTA_ENVIADA))
        assertTrue(opciones.contains(EstadoOportunidad.EN_ESPERA))
        assertTrue(estadosPermitidos(EstadoOportunidad.EN_ESPERA, false)
            .contains(EstadoOportunidad.CONTACTO_REALIZADO))
    }

    @Test fun ejecutivoNoPuedeConfirmarDescartarNiSaltarLaConfirmacion() {
        EstadoOportunidad.entries.forEach { actual ->
            val opciones = estadosPermitidos(actual, false)
            assertFalse(opciones.contains(EstadoOportunidad.CONFIRMADO))
            assertFalse(opciones.contains(EstadoOportunidad.DESCARTADO))
        }
        assertFalse(estadosPermitidos(EstadoOportunidad.EN_ESPERA, false)
            .contains(EstadoOportunidad.ACTIVACION_REALIZADA))
        assertEquals(listOf(EstadoOportunidad.ACTIVACION_REALIZADA),
            estadosPermitidos(EstadoOportunidad.CONFIRMADO, false))
    }

    @Test fun supervisorResuelvePeroNoReabreUnaOportunidadCerrada() {
        val opciones = estadosPermitidos(EstadoOportunidad.EN_ESPERA, true)
        assertTrue(opciones.contains(EstadoOportunidad.CONFIRMADO))
        assertTrue(opciones.contains(EstadoOportunidad.DESCARTADO))
        assertTrue(estadosPermitidos(EstadoOportunidad.CERRADO, true).isEmpty())
        assertTrue(estadosPermitidos(EstadoOportunidad.CERRADO, false).isEmpty())
        assertEquals(listOf(EstadoOportunidad.CERRADO),
            estadosPermitidos(EstadoOportunidad.DESCARTADO, true))
    }

    @Test fun noSeOfreceElEstadoActualNiSeReabreUnResultado() {
        EstadoOportunidad.entries.forEach { actual ->
            assertFalse(estadosPermitidos(actual, true).contains(actual))
            assertFalse(estadosPermitidos(actual, false).contains(actual))
        }
        assertEquals(listOf(EstadoOportunidad.CERRADO),
            estadosPermitidos(EstadoOportunidad.ACTIVACION_REALIZADA, false))
    }
}
