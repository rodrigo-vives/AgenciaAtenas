package com.agencia.atenas

import com.agencia.atenas.data.EstadoOportunidad
import com.agencia.atenas.utils.diasDeAlertaEvento
import org.junit.Assert.*
import org.junit.Test

class AlertasEventoTest {
    private val activo = EstadoOportunidad.IDENTIFICADO

    @Test fun incluyeHoyYElSeptimoDiaPeroNoElOctavo() {
        assertEquals(0, diasDeAlertaEvento("2026-10-17", activo, "2026-10-17"))
        assertEquals(1, diasDeAlertaEvento("2026-10-18", activo, "2026-10-17"))
        assertEquals(7, diasDeAlertaEvento("2026-10-24", activo, "2026-10-17"))
        assertNull(diasDeAlertaEvento("2026-10-25", activo, "2026-10-17"))
        assertNull(diasDeAlertaEvento("2026-10-16", activo, "2026-10-17"))
    }

    @Test fun omiteFechasInvalidasYEstadosTerminados() {
        assertNull(diasDeAlertaEvento("2026-02-31", activo, "2026-02-28"))
        assertNull(diasDeAlertaEvento("", activo, "2026-10-17"))
        assertNull(diasDeAlertaEvento("17/10/2026", activo, "2026-10-17"))
        listOf(EstadoOportunidad.DESCARTADO, EstadoOportunidad.CERRADO,
            EstadoOportunidad.ACTIVACION_REALIZADA).forEach { estado ->
            assertNull(diasDeAlertaEvento("2026-10-18", estado, "2026-10-17"))
        }
    }

    @Test fun calculaDiasAlCruzarMesYAnio() {
        assertEquals(1, diasDeAlertaEvento("2027-01-01", activo, "2026-12-31"))
        assertEquals(2, diasDeAlertaEvento("2024-03-01", activo, "2024-02-28"))
    }
}
