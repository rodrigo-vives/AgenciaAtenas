package com.agencia.atenas

import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agencia.atenas.data.*
import com.agencia.atenas.ui.ResultadoUiState
import com.agencia.atenas.utils.*
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Solo bases en memoria y carpetas temporales propias: no borra los datos de la app. */
@RunWith(AndroidJUnit4::class)
class CumplimientoCasoRoomTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun crearBase() = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
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



    private fun gestion(id: Int, usuario: UsuarioEntity) = SeguimientoEntity(
        oportunidadId = id, responsableId = usuario.id, fechaHora = Fechas.ahora(),
        accion = "Llamada", resultado = "Respuesta ficticia", proximaAccion = "", fechaProxima = "", observaciones = ""
    )

    @Test fun historialContactoNoCambiaAlCambiarContactoPrincipalYRespetaAsignacion() = runBlocking {
        val db = crearBase()
        try {
            val usuarios = UsuarioRepository(db)
            val ejecutivo = usuarios.autenticar("ejecutivo1", "1234")!!
            val supervisor = usuarios.autenticar("admin", "1234")!!
            val otro = usuarios.autenticar("ejecutivo2", "1234")!!
            SesionActual.iniciar(ejecutivo)
            val catalogo = db.catalogoDao()
            catalogo.insertarOrganizacion(OrganizacionEntity(nombre = "Empresa ficticia", tipo = "", rubro = ""))
            val org = catalogo.organizaciones().first().single()
            for (nombre in listOf("Contacto A", "Contacto B")) catalogo.insertarContacto(ContactoEntity(
                organizacionId = org.id, nombre = nombre, cargo = "", area = "", correo = "", telefono = "", medio = ""))
            val contactos = catalogo.contactos().first()
            val repo = OportunidadRepository(db)
            val id = repo.guardar(oportunidad(ejecutivo, org.id), contactos[0].id)
            repo.registrar(gestion(id, ejecutivo))
            repo.guardar(repo.obtener(id).oportunidad, contactos[1].id)
            assertEquals(1, repo.historialContacto(contactos[0].id).first().size)
            assertTrue(repo.historialContacto(contactos[1].id).first().isEmpty())
            SesionActual.iniciar(supervisor)
            repo.asignar(id, otro.id)
            SesionActual.iniciar(ejecutivo)
            assertTrue(repo.historialContacto(contactos[0].id).first().isEmpty())
            SesionActual.iniciar(otro)
            assertEquals(1, repo.historialContacto(contactos[0].id).first().size)
        } finally { SesionActual.cerrar(); db.close() }
    }

    @Test fun resultadoPersisteRelacionadoYFinalizaSoloUnaParticipacionConfirmada() = runBlocking {
        val db = crearBase()
        try {
            val usuarios = UsuarioRepository(db)
            val ejecutivo = usuarios.autenticar("ejecutivo1", "1234")!!
            val supervisor = usuarios.autenticar("admin", "1234")!!
            SesionActual.iniciar(ejecutivo)
            val repo = OportunidadRepository(db)
            val id = repo.guardar(oportunidad(ejecutivo, null), null)
            val resultado = ResultadoUiState(fechaRealizacion = Fechas.visible(Fechas.hoy()), evaluacion = "Prueba ficticia", inscripciones = "3")
            var rechazado = false
            try { repo.guardarResultado(id, resultado) } catch (_: IllegalArgumentException) { rechazado = true }
            assertTrue(rechazado)
            assertTrue(repo.resultados(id).first().isEmpty())
            SesionActual.iniciar(supervisor)
            repo.cambiarEstado(id, EstadoOportunidad.CONFIRMADO)
            SesionActual.iniciar(ejecutivo)
            repo.guardarResultado(id, resultado)
            val guardado = repo.resultados(id).first().single()
            assertEquals(3, guardado.indicadores.inscripciones)
            assertNull(guardado.indicadores.asistentes)
            assertEquals(ejecutivo.usuario, guardado.responsable)
            assertEquals(EstadoOportunidad.ACTIVACION_REALIZADA, repo.obtener(id).estado)
            assertTrue(repo.seguimientos(id).first().any { it.accion == "Resultado de activación" })
            var invalido = false
            try { repo.guardarResultado(id, resultado.copy(inscripciones = "-1")) } catch (_: IllegalArgumentException) { invalido = true }
            assertTrue(invalido)
            assertEquals(1, repo.resultados(id).first().size)
            db.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
        } finally { SesionActual.cerrar(); db.close() }
    }

    @Test fun asignarNoSustituyeUnaGestionComercial() = runBlocking {
        val db = crearBase()
        try {
            val usuarios = UsuarioRepository(db)
            val ejecutivo = usuarios.autenticar("ejecutivo1", "1234")!!
            val supervisor = usuarios.autenticar("admin", "1234")!!
            SesionActual.iniciar(supervisor)
            val repo = OportunidadRepository(db)
            val id = repo.guardar(oportunidad(supervisor, null), null)
            repo.asignar(id, ejecutivo.id)
            assertEquals(listOf(id), repo.sinSeguimiento().first().map { it.id })
            SesionActual.iniciar(ejecutivo)
            repo.registrar(gestion(id, ejecutivo))
            assertTrue(repo.sinSeguimiento().first().isEmpty())
        } finally { SesionActual.cerrar(); db.close() }
    }

    @Test fun organizacionConservaDatosComplementarios() = runBlocking {
        val db = crearBase()
        try {
            SesionActual.iniciar(UsuarioRepository(db).autenticar("admin", "1234")!!)
            CatalogoRepository(db).guardarOrganizacion("Organización ficticia", "Empresa", "Prueba",
                "Comuna ficticia", "Región ficticia", "https://example.invalid", "Público ficticio", "Observación")
            val org = db.catalogoDao().organizaciones().first().single()
            assertEquals("Comuna ficticia", org.comuna)
            assertEquals("https://example.invalid", org.sitioWeb)
            assertEquals("Observación", org.observaciones)
        } finally { SesionActual.cerrar(); db.close() }
    }

    @Test fun fotosSeConservanCancelacionNoAgregaYUsuarioAjenoNoAccede() = runBlocking {
        val db = crearBase()
        val carpeta = File(context.cacheDir, "evidencias-test-${UUID.randomUUID()}")
        try {
            val usuarios = UsuarioRepository(db)
            val ejecutivo = usuarios.autenticar("ejecutivo1", "1234")!!
            val ajeno = usuarios.autenticar("ejecutivo2", "1234")!!
            SesionActual.iniciar(ejecutivo)
            val id = OportunidadRepository(db).guardar(oportunidad(ejecutivo, null), null)
            val fotos = EvidenciaRepository(context, db, carpeta)
            val ruta = fotos.preparar(id)
            val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
            File(ruta).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            bitmap.recycle()
            fotos.finalizar(id, ruta, true)
            assertEquals(1, EvidenciaRepository(context, db, carpeta).listar(id).size)
            val cancelada = fotos.preparar(id)
            fotos.finalizar(id, cancelada, false)
            assertFalse(File(cancelada).exists())
            assertEquals(1, fotos.listar(id).size)
            SesionActual.iniciar(ajeno)
            var denegado = false
            try { fotos.listar(id) } catch (_: IllegalStateException) { denegado = true }
            assertTrue(denegado)
        } finally {
            SesionActual.cerrar(); db.close()
            // Carpeta única creada por esta prueba; nunca apunta a filesDir/evidencias.
            carpeta.deleteRecursively()
        }
    }
}
