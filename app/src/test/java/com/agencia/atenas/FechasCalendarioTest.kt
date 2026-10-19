package com.agencia.atenas

import com.agencia.atenas.utils.intervaloCalendario
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

/** Prueba la conversión real de fechas sin depender de tener Calendar instalado. */
class FechasCalendarioTest {
    private val utc = TimeZone.getTimeZone("UTC")

    @Test fun diaCompletoSinTerminoIncluyeUnDia() {
        val intervalo = intervaloCalendario("2026-10-17", "", "", utc)
        assertTrue(intervalo.todoElDia)
        assertEquals(24 * 60 * 60 * 1000L, intervalo.termino - intervalo.inicio)
    }

    @Test fun terminoDeDiaCompletoEsExclusivo() {
        val intervalo = intervaloCalendario("2026-12-31", "2027-01-02", "", utc)
        assertEquals(3 * 24 * 60 * 60 * 1000L, intervalo.termino - intervalo.inicio)
    }

    @Test fun respetaHorasYLaZonaHoraria() {
        val local = intervaloCalendario("2026-10-17", "", "09:00 a 18:00", TimeZone.getTimeZone("GMT-03:00"))
        val referencia = intervaloCalendario("2026-10-17", "", "12:00 a 21:00", utc)
        assertFalse(local.todoElDia)
        assertEquals(referencia.inicio, local.inicio)
        assertEquals(referencia.termino, local.termino)
    }

    @Test fun permiteUnEventoQueCruzaMedianocheConFechaFinal() {
        val intervalo = intervaloCalendario("2026-10-17", "2026-10-18", "23:00 a 01:00", utc)
        assertEquals(2 * 60 * 60 * 1000L, intervalo.termino - intervalo.inicio)
    }

    @Test fun rechazaFechasImposiblesYFormatosDistintos() {
        listOf("2026-02-30", "17/10/2026", "", "2026-13-01").forEach { fecha ->
            assertThrows(Exception::class.java) { intervaloCalendario(fecha, "", "", utc) }
        }
    }

    @Test fun rechazaTerminoAnteriorOHorasIncorrectas() {
        assertThrows(Exception::class.java) { intervaloCalendario("2026-10-17", "2026-10-16", "", utc) }
        listOf("18:00 a 09:00", "09:00 a 09:00", "25:00 a 26:00", "9 a 18").forEach { horario ->
            assertThrows(Exception::class.java) { intervaloCalendario("2026-10-17", "", horario, utc) }
        }
    }
}
