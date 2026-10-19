package com.agencia.atenas

import com.agencia.atenas.utils.*
import com.agencia.atenas.ui.CrearUiState
import com.agencia.atenas.data.*
import org.junit.Assert.*
import org.junit.Test

class ValidacionesTest {
    private fun minimo() = CrearUiState(nombreOportunidad = "Feria de prueba",
        tipoOportunidad = "Feria empresarial", fechaInicio = "20/10/2026",
        fechaDeteccion = "17/10/2026", comuna = "Comuna de prueba", region = "Región de prueba",
        publicoEsperado = "Trabajadores")

    @Test fun fechasRealesYFormatoVisible() {
        assertEquals("2026-10-17", Fechas.aIso("17/10/2026"))
        assertEquals("17/10/2026", Fechas.visible("2026-10-17"))
        assertEquals("2026-10-17texto", Fechas.visible("2026-10-17texto"))
        assertEquals("fecha incorrecta", Fechas.visible("fecha incorrecta"))
        assertNull(Fechas.aIso("31/02/2026"))
        assertNull(Fechas.aIso("1/2/2026"))
        assertEquals("2024-02-29", Fechas.aIso("29/02/2024"))
        assertNull(Fechas.aIso("29/02/2025"))
    }
    @Test fun permiteDatosDesconocidosSinInventarlos() {
        assertTrue(validarOportunidad(minimo()).isEmpty())
        assertTrue(validarCorreo(""))
        assertTrue(validarTelefono(""))
    }
    @Test fun rechazaRangosYNumerosInvalidos() {
        val errores = validarOportunidad(minimo().copy(fechaTermino = "19/10/2026",
            cantidadAsistentes = "-1", costosReferenciales = "NaN"))
        assertTrue(errores.containsKey("fechaTermino"))
        assertTrue(errores.containsKey("cantidadAsistentes"))
        assertTrue(errores.containsKey("costosReferenciales"))
        assertFalse(validarCorreo("sin-arroba"))
        assertFalse(validarTelefono("abc12345678"))
    }
    @Test fun ejecutivoNoConfirmaNiDescarta() {
        try {
            SesionActual.iniciar(cuentasIniciales().first { it.usuario == "ejecutivo1" }.copy(id = 2))
            EstadoOportunidad.entries.forEach { estado ->
                assertFalse(estadosPermitidos(estado).contains(EstadoOportunidad.CONFIRMADO))
                assertFalse(estadosPermitidos(estado).contains(EstadoOportunidad.DESCARTADO))
            }
            SesionActual.iniciar(cuentasIniciales().first { it.usuario == "admin" }.copy(id = 1))
            assertTrue(estadosPermitidos(EstadoOportunidad.EN_ESPERA).contains(EstadoOportunidad.CONFIRMADO))
            assertTrue(estadosPermitidos(EstadoOportunidad.CERRADO).isEmpty())
        } finally { SesionActual.cerrar() }
    }

    @Test fun horarioSinFechaFinalSeValidaComoMismoDia() {
        val errores = validarOportunidad(minimo().copy(horario = "18:00 a 09:00"))
        assertTrue(errores.containsKey("horario"))
        assertFalse(validarOportunidad(minimo().copy(horario = "09:00 a 18:00")).containsKey("horario"))
    }

    @Test fun cruzarMedianocheRequiereUnaFechaPosterior() {
        val evento = minimo().copy(fechaTermino = "21/10/2026", horario = "23:00 a 01:00")
        assertTrue(validarOportunidad(evento).isEmpty())
    }
}
