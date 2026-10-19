package com.agencia.atenas

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agencia.atenas.data.*
import com.agencia.atenas.utils.SesionActual
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Bases en memoria aisladas: no se modifican los datos del usuario en agencia_atenas.db. */
@RunWith(AndroidJUnit4::class)
class RecordatoriosRoomTest {
    private fun crearBase(): AppDatabase = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java
    ).build()

    private fun oportunidad(usuario: UsuarioEntity, orgId: Int?) = GestionOportunidadEntity(
        nombreOportunidad = "Feria de prueba",
        tipoOportunidad = "",
        descripcion = "",
        fuenteInformacion = "",
        fechaDeteccion = "2026-10-17",
        prioridad = "",
        potencialComercial = "",
        fechaInicio = "2026-10-17",
        fechaTermino = "",
        horario = "",
        comuna = "",
        region = "",
        publicoEsperado = "",
        cantidadAsistentes = null,
        condicionesParticipacion = "",
        costosReferenciales = "",
        estado = EstadoOportunidad.IDENTIFICADO,
        historialObservaciones = "",
        ubicacionGeneral = "",
        publicoObjetivo = "",
        requisitos = "",
        espaciosDisponibles = "",
        fechaUltimaActualizacion = "",
        responsableId = usuario.id,
        organizacionId = orgId
    )


    private fun gestion(oportunidadId: Int, autor: UsuarioEntity) = SeguimientoEntity(
        oportunidadId = oportunidadId, responsableId = autor.id, fechaHora = "2026-10-17 10:00:00",
        accion = "Primer contacto", resultado = "No respondió", proximaAccion = "Volver a llamar",
        fechaProxima = "2026-10-16", observaciones = ""
    )

    @Test fun resolverConservaHistorialYRechazaResolucionDuplicada() = runBlocking {
        val db = crearBase()
        try {
            val usuarios = UsuarioRepository(db)
            val ejecutivo = usuarios.autenticar("ejecutivo1", "1234")!!
            val supervisor = usuarios.autenticar("admin", "1234")!!
            SesionActual.iniciar(ejecutivo)
            val repo = OportunidadRepository(db)
            val id = repo.guardar(oportunidad(ejecutivo, null), null)
            repo.registrar(gestion(id, ejecutivo))
            val tarea = repo.recordatorios().first().single()

            SesionActual.iniciar(supervisor)
            val resultado = gestion(id, supervisor).copy(
                accion = "Texto recibido del formulario", resultado = "Se realizó la llamada",
                proximaAccion = "Enviar resumen", fechaProxima = "2026-10-18"
            )
            repo.registrar(resultado, tarea.seguimientoId)
            val historial = repo.seguimientos(id).first()
            assertEquals(2, historial.size)
            val original = historial.first { it.seguimiento.id == tarea.seguimientoId }
            assertNotNull(original.seguimiento.resueltaPorId)
            assertEquals("Se realizó la llamada", original.resultadoResolucion)
            assertEquals("admin", original.autorResolucion)
            // Solo la próxima acción NUEVA sigue abierta; la antigua no se vuelve a ofrecer.
            assertEquals("Enviar resumen", repo.recordatorios().first().single().proximaAccion)
            assertEquals("Volver a llamar", historial.first().accion)
            var rechazado = false
            try { repo.registrar(resultado, tarea.seguimientoId) }
            catch (_: IllegalArgumentException) { rechazado = true }
            assertTrue(rechazado)
            assertEquals(2, repo.seguimientos(id).first().size)
        } finally {
            SesionActual.cerrar()
            db.close()
        }
    }

    @Test fun lasTareasSiguenAlResponsableActualYSeOcultanAlFinalizar() = runBlocking {
        val db = crearBase()
        try {
            val usuarios = UsuarioRepository(db)
            val ejecutivo1 = usuarios.autenticar("ejecutivo1", "1234")!!
            val ejecutivo2 = usuarios.autenticar("ejecutivo2", "1234")!!
            val supervisor = usuarios.autenticar("admin", "1234")!!
            SesionActual.iniciar(ejecutivo1)
            val repo = OportunidadRepository(db)
            val id = repo.guardar(oportunidad(ejecutivo1, null), null)
            repo.registrar(gestion(id, ejecutivo1))
            assertEquals(1, repo.recordatorios().first().size)
            SesionActual.iniciar(ejecutivo2)
            assertTrue(repo.recordatorios().first().isEmpty())
            SesionActual.iniciar(supervisor)
            repo.asignar(id, ejecutivo2.id)
            SesionActual.iniciar(ejecutivo1)
            assertTrue(repo.recordatorios().first().isEmpty())
            SesionActual.iniciar(ejecutivo2)
            assertEquals(ejecutivo2.id, repo.recordatorios().first().single().responsableId)
            SesionActual.iniciar(supervisor)
            repo.cambiarEstado(id, EstadoOportunidad.DESCARTADO)
            assertTrue(repo.recordatorios().first().isEmpty())
        } finally {
            SesionActual.cerrar()
            db.close()
        }
    }

    @Test fun noSePuedeResolverUnaTareaDeOtraOportunidad() = runBlocking {
        val db = crearBase()
        try {
            val ejecutivo = UsuarioRepository(db).autenticar("ejecutivo1", "1234")!!
            SesionActual.iniciar(ejecutivo)
            val repo = OportunidadRepository(db)
            val primera = repo.guardar(oportunidad(ejecutivo, null), null)
            val segunda = repo.guardar(oportunidad(ejecutivo, null), null)
            repo.registrar(gestion(primera, ejecutivo))
            val tarea = repo.recordatorios().first().single()
            var rechazado = false
            try { repo.registrar(gestion(segunda, ejecutivo), tarea.seguimientoId) }
            catch (_: IllegalArgumentException) { rechazado = true }
            assertTrue(rechazado)
            assertTrue(repo.seguimientos(segunda).first().isEmpty())
            assertEquals(1, repo.recordatorios().first().size)
        } finally {
            SesionActual.cerrar()
            db.close()
        }
    }
}
