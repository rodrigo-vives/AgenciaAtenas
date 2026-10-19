package com.agencia.atenas

import com.agencia.atenas.ui.ResultadoUiState
import com.agencia.atenas.utils.validarResultado
import org.junit.Assert.*
import org.junit.Test

class ValidarResultadoTest {
    private val base = ResultadoUiState(fechaRealizacion = "18/10/2026", evaluacion = "Resultado ficticio")
    @Test fun permiteIndicadoresDesconocidos() {
        assertTrue(validarResultado(base, "2026-10-18").isEmpty())
    }
    @Test fun rechazaFechaFuturaOImposible() {
        assertTrue(validarResultado(base.copy(fechaRealizacion = "19/10/2026"), "2026-10-18").containsKey("fechaRealizacion"))
        assertTrue(validarResultado(base.copy(fechaRealizacion = "31/02/2026"), "2026-10-18").containsKey("fechaRealizacion"))
    }
    @Test fun rechazaIndicadoresNegativosDecimalesODesbordados() {
        for (texto in listOf("-1", "1.5", "no sé", "99999999999999999")) {
            assertTrue(validarResultado(base.copy(inscripciones = texto), "2026-10-18").containsKey("inscripciones"))
        }
    }
    @Test fun exigeResultadoYLimitaTextoSinBorrarBorrador() {
        val borrador = base.copy(evaluacion = "", aprendizajes = "x".repeat(2001))
        val errores = validarResultado(borrador, "2026-10-18")
        assertTrue(errores.containsKey("evaluacion"))
        assertTrue(errores.containsKey("aprendizajes"))
        assertEquals(2001, borrador.aprendizajes.length)
    }
}
