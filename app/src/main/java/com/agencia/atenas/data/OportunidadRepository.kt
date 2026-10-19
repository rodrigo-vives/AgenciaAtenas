package com.agencia.atenas.data

import androidx.room.withTransaction
import com.agencia.atenas.utils.Fechas
import com.agencia.atenas.utils.SesionActual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

/**
 * Todas las escrituras comprueban la identidad leyendo usuarios en Room.
 * La UI puede esconder botones por rol, pero esa presentación no autoriza una operación.
 * Las lecturas devuelven OportunidadDetalle: los nombres provienen de JOIN, no de copias.
 */
class OportunidadRepository(private val db: AppDatabase) {
    private val dao = db.oportunidadDao()

    fun observar(): Flow<List<OportunidadDetalle>> {
        val usuarioId = SesionActual.usuarioId ?: return flowOf(emptyList())
        // combine reacciona tanto a cambios de oportunidades/catálogos como a cambios
        // del usuario. Desactivar una cuenta retira sus registros de la lista observable.
        return combine(dao.obtenerTodas(), db.usuarioDao().observar(usuarioId)) { lista, usuario ->
            when {
                usuario == null || !usuario.activo -> emptyList()
                usuario.rol == Roles.SUPERVISOR -> lista
                usuario.rol == Roles.EJECUTIVO -> lista.filter { it.responsableId == usuario.id }
                else -> emptyList()
            }
        }
    }

    // La sesión guarda el ID; leer la fila vigente evita confiar en un rol antiguo en memoria.
    private suspend fun usuarioActual(): UsuarioEntity {
        val id = checkNotNull(SesionActual.usuarioId) { "Inicia sesión nuevamente" }
        val usuario = checkNotNull(db.usuarioDao().obtener(id)) { "El usuario ya no existe" }
        check(usuario.activo && usuario.rol in listOf(Roles.EJECUTIVO, Roles.SUPERVISOR)) {
            "La cuenta no está habilitada"
        }
        return usuario
    }

    private suspend fun comprobarAcceso(id: Int, usuario: UsuarioEntity): OportunidadDetalle {
        val detalle = requireNotNull(dao.obtenerPorId(id)) { "La oportunidad no existe" }
        check(usuario.rol == Roles.SUPERVISOR || detalle.responsableId == usuario.id) {
            "No tienes acceso a esta oportunidad"
        }
        return detalle
    }

    suspend fun obtener(id: Int): OportunidadDetalle = comprobarAcceso(id, usuarioActual())

    // withTransaction confirma oportunidad y contacto juntos; si algo falla, revierte ambos.
    // El formulario convierte textos a datos y este límite vuelve a comprobar identidad y relaciones.
    suspend fun guardar(item: GestionOportunidadEntity, contactoId: Int?): Int = db.withTransaction {
        val usuario = usuarioActual()
        if (item.id == 0) {
            check(item.responsableId == usuario.id) { "Responsable no autorizado" }
            check(item.estado == EstadoOportunidad.IDENTIFICADO)
        } else {
            val anterior = comprobarAcceso(item.id, usuario)
            check(item.responsableId == anterior.responsableId) { "Asigna responsables desde el panel" }
            check(item.estado == anterior.estado) { "Cambia el estado desde el detalle" }
        }

        // La clave foránea comprueba existencia; esta regla comprueba coherencia comercial.
        // Un contacto existente de OTRA organización tampoco se puede asociar aquí.
        if (item.organizacionId != null) {
            requireNotNull(db.catalogoDao().obtenerOrganizacion(item.organizacionId)) {
                "La organización seleccionada ya no existe"
            }
        }
        if (contactoId != null) {
            val contacto = requireNotNull(db.catalogoDao().obtenerContacto(contactoId)) {
                "El contacto seleccionado ya no existe"
            }
            require(contacto.organizacionId == item.organizacionId) {
                "El contacto no pertenece a la organización seleccionada"
            }
        }

        val id = if (item.id == 0) {
            dao.insertar(item).toInt()
        } else {
            dao.actualizar(item)
            item.id
        }
        // La relación y la oportunidad se guardan juntas. Quitar un contacto no lo borra
        // del catálogo: solo elimina su asociación con esta oportunidad.
        dao.quitarContacto(id)
        if (contactoId != null) {
            dao.vincularContacto(OportunidadContactoEntity(id, contactoId))
        }
        id
    }

    suspend fun cambiarEstado(id: Int, estado: EstadoOportunidad) = db.withTransaction {
        val usuario = usuarioActual()
        val anterior = comprobarAcceso(id, usuario)
        check(estado in estadosPermitidos(anterior.estado, usuario.rol == Roles.SUPERVISOR)) {
            "Transición o permiso no permitido"
        }
        dao.actualizar(anterior.oportunidad.copy(
            estado = estado,
            fechaUltimaActualizacion = Fechas.ahora()
        ))
        guardarAccionAutomatica(id, usuario.id, "Cambio de estado",
            "${anterior.estado.etiqueta} → ${estado.etiqueta}")
    }

    suspend fun asignar(id: Int, responsableId: Int) = db.withTransaction {
        val supervisor = usuarioActual()
        check(supervisor.rol == Roles.SUPERVISOR) { "Solo el supervisor puede asignar" }
        val ejecutivo = requireNotNull(db.usuarioDao().obtener(responsableId)) {
            "El ejecutivo ya no existe"
        }
        require(ejecutivo.activo && ejecutivo.rol == Roles.EJECUTIVO) {
            "Selecciona un ejecutivo activo"
        }
        val anterior = comprobarAcceso(id, supervisor)
        dao.actualizar(anterior.oportunidad.copy(
            responsableId = ejecutivo.id,
            fechaUltimaActualizacion = Fechas.ahora()
        ))
        guardarAccionAutomatica(id, supervisor.id, "Asignación",
            "${anterior.responsable} → ${ejecutivo.usuario}")
    }

    // resultado es la descripción histórica del hecho, no un campo usado para relacionar
    // tablas. Se conserva ese texto aunque después cambie el nombre del ejecutivo.
    private suspend fun guardarAccionAutomatica(id: Int, autorId: Int, accion: String, resultado: String) {
        db.catalogoDao().insertarSeguimiento(SeguimientoEntity(
            oportunidadId = id, responsableId = autorId, fechaHora = Fechas.ahora(),
            accion = accion, resultado = resultado, proximaAccion = "",
            fechaProxima = "", observaciones = ""
        ))
    }

    fun seguimientos(id: Int): Flow<List<SeguimientoDetalle>> {
        return combine(db.catalogoDao().seguimientos(id), observar()) { historial, oportunidades ->
            if (oportunidades.any { it.id == id }) historial else emptyList()
        }
    }

    fun sinSeguimiento(): Flow<List<OportunidadDetalle>> {
        return combine(observar(), db.catalogoDao().idsConGestionComercial()) { oportunidades, ids ->
            val conGestion = ids.toSet()
            oportunidades.filter { it.id !in conGestion && it.estado !in listOf(
                EstadoOportunidad.DESCARTADO, EstadoOportunidad.ACTIVACION_REALIZADA, EstadoOportunidad.CERRADO
            ) }
        }
    }

    // Intersección por ID: solo regresan tareas cuya oportunidad puede ver el usuario.
    fun recordatorios(): Flow<List<RecordatorioSeguimiento>> {
        return combine(db.catalogoDao().observarRecordatorios(), observar()) { tareas, oportunidades ->
            val idsVisibles = oportunidades.map { it.id }.toSet()
            tareas.filter { it.oportunidadId in idsVisibles }
        }
    }

    // Una gestión libre solo inserta historial. Si llega pendienteId, además enlaza la tarea
    // original al resultado nuevo. La transacción impide dejar el proceso a medias.
    suspend fun registrar(item: SeguimientoEntity, pendienteId: Int? = null) = db.withTransaction {
        val usuario = usuarioActual()
        val oportunidad = comprobarAcceso(item.oportunidadId, usuario)
        val errores = com.agencia.atenas.utils.validarSeguimiento(
            item.accion, item.resultado, item.proximaAccion,
            Fechas.visible(item.fechaProxima), item.observaciones
        )
        require(errores.isEmpty()) { errores.values.firstOrNull().orEmpty() }
        if (item.proximaAccion.isNotBlank()) {
            require(oportunidad.estado !in listOf(EstadoOportunidad.DESCARTADO,
                EstadoOportunidad.ACTIVACION_REALIZADA, EstadoOportunidad.CERRADO)) {
                "No se pueden programar nuevas acciones en una oportunidad finalizada"
            }
        }

        val pendiente = if (pendienteId != null) {
            val fila = requireNotNull(db.catalogoDao().obtenerSeguimiento(pendienteId)) {
                "La acción programada ya no existe"
            }
            require(fila.oportunidadId == item.oportunidadId) { "La acción pertenece a otra oportunidad" }
            require(fila.resueltaPorId == null) { "Esta acción ya fue completada" }
            require(fila.proximaAccion.isNotBlank() && fila.fechaProxima.isNotBlank()) {
                "Esta gestión no tiene una próxima acción programada"
            }
            fila
        } else {
            null
        }

        // Primero se registra el resultado nuevo. La tarea original conserva su acción,
        // fecha y autor; solo se añade el vínculo a la gestión que la completó.
        val registro = item.copy(
            responsableId = usuario.id,
            accion = pendiente?.proximaAccion ?: item.accion,
            resueltaPorId = null,
            contactoId = oportunidad.contactoId
        )
        val resolucionId = db.catalogoDao().insertarSeguimiento(registro).toInt()
        if (pendienteId != null) {
            val cambios = db.catalogoDao().resolverAccion(pendienteId, item.oportunidadId, resolucionId)
            check(cambios == 1) { "Otra operación ya completó esta acción" }
        }
        // Si falla el vínculo, la transacción también revierte la inserción del resultado.
        resolucionId
    }
    fun historialOrganizacion(id: Int): Flow<List<HistorialContactoItem>> {
        return combine(db.catalogoDao().historialOrganizacion(id), observar()) { historial, visibles ->
            val ids = visibles.map { it.id }.toSet()
            historial.filter { it.seguimiento.oportunidadId in ids }
        }
    }

    fun historialContacto(contactoId: Int): Flow<List<HistorialContactoItem>> {
        return combine(db.catalogoDao().historialContacto(contactoId), observar()) { historial, visibles ->
            val ids = visibles.map { it.id }.toSet()
            historial.filter { it.seguimiento.oportunidadId in ids }
        }
    }

    fun resultados(id: Int): Flow<List<ResultadoActivacionDetalle>> {
        return combine(db.catalogoDao().resultados(id), observar()) { resultados, visibles ->
            if (visibles.any { it.id == id }) resultados else emptyList()
        }
    }

    suspend fun guardarResultado(id: Int, estado: com.agencia.atenas.ui.ResultadoUiState) = db.withTransaction {
        val usuario = usuarioActual()
        val oportunidad = comprobarAcceso(id, usuario)
        require(oportunidad.estado in listOf(EstadoOportunidad.CONFIRMADO,
            EstadoOportunidad.ACTIVACION_REALIZADA, EstadoOportunidad.CERRADO)) {
            "La participación debe estar confirmada antes de registrar su resultado"
        }
        val errores = com.agencia.atenas.utils.validarResultado(estado)
        require(errores.isEmpty()) { errores.values.firstOrNull().orEmpty() }
        // Paso 1: registrar la evaluación con autor y oportunidad. registrar devuelve
        // el ID generado y aplica las reglas de identidad, contacto y acceso.
        val seguimientoId = registrar(SeguimientoEntity(
            oportunidadId = id, responsableId = usuario.id, fechaHora = Fechas.ahora(),
            accion = "Resultado de activación", resultado = estado.evaluacion.trim(),
            observaciones = estado.observaciones.trim(), proximaAccion = "", fechaProxima = ""
        ))
        // Paso 2: guardar solo los indicadores propios del resultado, enlazados por ID.
        // Autor, evaluación y observaciones siguen en la gestión, sin copias innecesarias.
        db.catalogoDao().insertarResultado(ResultadoActivacionEntity(
            seguimientoId = seguimientoId,
            fechaRealizacion = checkNotNull(Fechas.aIso(estado.fechaRealizacion)),
            asistentes = estado.asistentes.trim().toIntOrNull(),
            contactosGenerados = estado.contactosGenerados.trim().toIntOrNull(),
            interesados = estado.interesados.trim().toIntOrNull(),
            inscripciones = estado.inscripciones.trim().toIntOrNull(),
            aprendizajes = estado.aprendizajes.trim()
        ))
        // Paso 3: actualizar el estado si corresponde. withTransaction abarca los tres
        // pasos: si cualquiera falla, Room revierte también lo que se insertó antes.
        // Un resultado confirmado documenta que se realizó. Se usa la misma regla de
        // transición y se registra el cambio en historial; todo se confirma junto.
        if (oportunidad.estado == EstadoOportunidad.CONFIRMADO) {
            cambiarEstado(id, EstadoOportunidad.ACTIVACION_REALIZADA)
        }
    }

}
