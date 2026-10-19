package com.agencia.atenas.data

import com.agencia.atenas.utils.*

/**
 * Acceso a Room para organizaciones y contactos. Repite las comprobaciones básicas antes
 * de escribir, aunque el formulario ya haya mostrado sus errores.
 */

// CAMBIO: validación de catálogos también antes de escribir en Room.
class CatalogoRepository(private val db: AppDatabase) {
    private val dao = db.catalogoDao()

    private suspend fun comprobarSesion() {
        val id = checkNotNull(SesionActual.usuarioId) { "Inicia sesión nuevamente" }
        val usuario = checkNotNull(db.usuarioDao().obtener(id)) { "El usuario ya no existe" }
        check(usuario.activo && usuario.rol in listOf(Roles.EJECUTIVO, Roles.SUPERVISOR)) {
            "La cuenta no está habilitada"
        }
    }

    fun organizaciones() = dao.organizaciones()

    fun contactos() = dao.contactos()

    // Consulta puntual para el título del historial. El ViewModel no necesita saber
    // qué DAO ni qué tabla contiene el nombre; aquí se comprueba también la sesión.
    suspend fun obtenerNombre(id: Int, esContacto: Boolean): String {
        comprobarSesion()
        val nombre = if (esContacto) dao.obtenerContacto(id)?.nombre
            else dao.obtenerOrganizacion(id)?.nombre
        return requireNotNull(nombre) { "El registro no existe" }
    }

    // Guarda una organización con nombre obligatorio y datos complementarios opcionales.
    suspend fun guardarOrganizacion(nombre: String,
        tipo: String,
        rubro: String,
        comuna: String = "",
        region: String = "",
        sitioWeb: String = "",
        publicoAsociado: String = "",
        observaciones: String = "") {
        comprobarSesion()
        require(nombre.isNotBlank()) {
            "El nombre es obligatorio"
        }
        require(nombre.length <= 120 && tipo.length <= 120 && rubro.length <= 120) {
            "Máximo 120 caracteres por campo"
        }
        require(comuna.length <= 120 && region.length <= 120 && sitioWeb.length <= 120) { "Máximo 120 caracteres" }
        require(publicoAsociado.length <= 2000 && observaciones.length <= 2000) { "Máximo 2000 caracteres" }
        require(validarSitioWeb(sitioWeb)) { "Sitio web inválido" }
        dao.insertarOrganizacion(OrganizacionEntity(nombre = nombre.trim(),
                tipo = tipo.trim(),
                rubro = rubro.trim(), comuna = comuna.trim(), region = region.trim(),
                sitioWeb = sitioWeb.trim(), publicoAsociado = publicoAsociado.trim(), observaciones = observaciones.trim()))
    }

    // Valida el contacto y guarda su relación con una organización existente.
    suspend fun guardarContacto(orgId: Int,
        nombre: String,
        cargo: String,
        area: String,
        correo: String,
        telefono: String,
        medio: String) {
        comprobarSesion()
        require(orgId > 0 && nombre.isNotBlank()) {
            "Selecciona organización y nombre"
        }
        require(validarCorreo(correo)) {
            "Correo inválido"
        }
        require(validarTelefono(telefono)) {
            "Teléfono inválido"
        }
        require(listOf(nombre,
                cargo,
                area,
                correo,
                telefono,
                medio).all {
                it.length <= 120
        }) {
            "Máximo 120 caracteres por campo"
        }
        dao.insertarContacto(ContactoEntity(organizacionId = orgId,
                nombre = nombre.trim(),
                cargo = cargo.trim(),
                area = area.trim(),
                correo = correo.trim(),
                telefono = telefono.trim(),
                medio = medio.trim()))
    }
}
