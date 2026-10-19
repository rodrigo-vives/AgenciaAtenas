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
import java.util.UUID

/**
 * Pruebas sobre Room real, en bases de nombre aleatorio: no tocan agencia_atenas.db.
 * Comprueban relaciones, permisos y persistencia de la estructura actual.
 */
@RunWith(AndroidJUnit4::class)
class NormalizacionRoomTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun abrir(nombre: String): AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, nombre
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

    @Test fun cuentasPersistenYElRolSeLeeDeLaTabla() = runBlocking {
        val nombre = "prueba_usuarios_${UUID.randomUUID()}.db"
        var db = abrir(nombre)
        try {
            val usuarios = UsuarioRepository(db)
            val supervisor = usuarios.autenticar("admin", "1234")!!
            assertEquals(Roles.SUPERVISOR, supervisor.rol)
            assertNull(usuarios.autenticar("admin", "incorrecta"))
            assertEquals(3, usuarios.ejecutivos().first().size)
            db.close()
            db = abrir(nombre)
            assertEquals(supervisor.id, UsuarioRepository(db).autenticar("admin", "1234")!!.id)

            // Cambiar el rol en Room cambia el resultado; no se deduce por llamarse admin.
            db.openHelper.writableDatabase.execSQL("UPDATE usuarios SET rol = 'EJECUTIVO' WHERE usuario = 'admin'")
            SesionActual.iniciar(UsuarioRepository(db).autenticar("admin", "1234")!!)
            assertFalse(SesionActual.esSupervisor)
            db.openHelper.writableDatabase.execSQL("UPDATE usuarios SET activo = 0 WHERE usuario = 'admin'")
            assertNull(UsuarioRepository(db).autenticar("admin", "1234"))
        } finally {
            SesionActual.cerrar()
            db.close()
            context.deleteDatabase(nombre)
        }
    }

    @Test fun usaIdsAunqueLosNombresSeRepitanYAutorizaDesdeRoom() = runBlocking {
        val nombre = "prueba_relaciones_${UUID.randomUUID()}.db"
        val db = abrir(nombre)
        try {
            val usuarioRepo = UsuarioRepository(db)
            val ejecutivo = usuarioRepo.autenticar("ejecutivo1", "1234")!!
            val supervisor = usuarioRepo.autenticar("admin", "1234")!!
            SesionActual.iniciar(ejecutivo)
            val dao = db.catalogoDao()
            dao.insertarOrganizacion(OrganizacionEntity(nombre = "Mismo nombre", tipo = "", rubro = "A"))
            dao.insertarOrganizacion(OrganizacionEntity(nombre = "Mismo nombre", tipo = "", rubro = "B"))
            val organizaciones = dao.organizaciones().first()
            val orgA = organizaciones.first { it.rubro == "A" }
            val orgB = organizaciones.first { it.rubro == "B" }
            dao.insertarContacto(ContactoEntity(organizacionId = orgA.id, nombre = "Contacto A",
                cargo = "", area = "", correo = "", telefono = "", medio = ""))
            val contacto = dao.contactos().first().single()
            val repo = OportunidadRepository(db)
            val id = repo.guardar(oportunidad(ejecutivo, orgA.id), contacto.id)
            assertEquals(orgA.id, repo.obtener(id).organizacionId)
            assertEquals(contacto.id, repo.obtener(id).contactoId)

            // La misma fila de catálogo se consulta por JOIN: no hay nombre duplicado.
            db.openHelper.writableDatabase.execSQL("UPDATE organizaciones SET nombre = 'Nombre actualizado' WHERE id = ?", arrayOf(orgA.id))
            assertEquals("Nombre actualizado", repo.obtener(id).nombreOrganizacion)
            var rechazado = false
            try { repo.guardar(oportunidad(ejecutivo, orgB.id), contacto.id) }
            catch (_: IllegalArgumentException) { rechazado = true }
            assertTrue(rechazado)

            // Aunque la UI conserve un rol anterior, Repository lee el usuario vigente.
            db.openHelper.writableDatabase.execSQL("UPDATE usuarios SET activo = 0 WHERE id = ?", arrayOf(ejecutivo.id))
            rechazado = false
            try { repo.obtener(id) } catch (_: IllegalStateException) { rechazado = true }
            assertTrue(rechazado)

            SesionActual.iniciar(supervisor)
            val destino = usuarioRepo.autenticar("ejecutivo2", "1234")!!
            repo.asignar(id, destino.id)
            val historial = repo.seguimientos(id).first().single()
            assertEquals(supervisor.id, historial.seguimiento.responsableId)
            assertEquals(destino.id, repo.obtener(id).responsableId)
        } finally {
            SesionActual.cerrar()
            db.close()
            context.deleteDatabase(nombre)
        }
    }

}
