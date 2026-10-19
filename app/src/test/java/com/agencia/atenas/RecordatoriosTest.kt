package com.agencia.atenas

import com.agencia.atenas.utils.*
import org.junit.Assert.*
import org.junit.Test

class RecordatoriosTest {
    @Test fun separaAyerHoyYFechasFuturas() {
        assertEquals(SituacionRecordatorio.ATRASADO,
            situacionRecordatorio("2026-10-16", hoy = "2026-10-17"))
        assertEquals(SituacionRecordatorio.HOY,
            situacionRecordatorio("2026-10-17", hoy = "2026-10-17"))
        assertEquals(SituacionRecordatorio.PENDIENTE,
            situacionRecordatorio("2026-10-18", hoy = "2026-10-17"))
    }

    @Test fun noAvisaDeTareasResueltasNiFechasInvalidas() {
        assertNull(situacionRecordatorio("2026-10-16", resuelto = true, hoy = "2026-10-17"))
        assertNull(situacionRecordatorio("2026-02-31", hoy = "2026-10-17"))
        assertNull(situacionRecordatorio("17/10/2026", hoy = "2026-10-17"))
        assertNull(situacionRecordatorio("", hoy = "2026-10-17"))
    }

    @Test fun comparaCorrectamenteAlCambiarDeAnio() {
        assertEquals(SituacionRecordatorio.ATRASADO,
            situacionRecordatorio("2026-12-31", hoy = "2027-01-01"))
        assertEquals(SituacionRecordatorio.PENDIENTE,
            situacionRecordatorio("2027-01-01", hoy = "2026-12-31"))
    }

    @Test fun proximaAccionYFechaDebenEstarJuntas() {
        assertTrue(validarSeguimiento("Llamada", "Sin respuesta", "Llamar", "", "")
            .containsKey("fechaProxima"))
        assertTrue(validarSeguimiento("Llamada", "Sin respuesta", "", "18/10/2026", "")
            .containsKey("proximaAccion"))
        assertTrue(validarSeguimiento("Llamada", "Sin respuesta", "Llamar", "31/02/2026", "")
            .containsKey("fechaProxima"))
    }

    @Test fun resolverExigeResultadoYPermiteProgramarOtraAccion() {
        assertTrue(validarSeguimiento("Llamar", "", "", "", "").containsKey("resultado"))
        assertTrue(validarSeguimiento("Llamar", "Confirmó recepción", "Enviar resumen", "18/10/2026", "").isEmpty())
        assertTrue(validarSeguimiento("Llamar", "Sin respuesta", "", "", "").isEmpty())
    }
}
