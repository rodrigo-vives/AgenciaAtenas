package com.agencia.atenas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/*
 * Room genera la implementación de esta interfaz durante la compilación.
 * @Insert genera la escritura; @Query ejecuta el SQL declarado. Un Flow emite la lista inicial
 * y las actualizaciones de esa consulta cuando Room detecta cambios en sus tablas.
 * No hay que implementar manualmente CatalogoDao_Impl ni editar los archivos generados.
 */


/**
 * Consultas e inserciones de los catálogos y del historial. Flow mantiene observadas las
 * consultas; suspend permite llamar las escrituras desde las corrutinas de los ViewModels.
 */

@Dao
interface CatalogoDao {
    @Query("SELECT * FROM organizaciones ORDER BY nombre")
    fun organizaciones(): Flow<List<OrganizacionEntity>>
    @Query("SELECT * FROM contactos ORDER BY nombre")
    fun contactos(): Flow<List<ContactoEntity>>
    @Query("SELECT * FROM contactos WHERE id = :id LIMIT 1")
    suspend fun obtenerContacto(id: Int): ContactoEntity?

    @Query("SELECT * FROM organizaciones WHERE id = :id LIMIT 1")
    suspend fun obtenerOrganizacion(id: Int): OrganizacionEntity?

    @Insert suspend fun insertarOrganizacion(item: OrganizacionEntity)
    @Insert suspend fun insertarContacto(item: ContactoEntity)
    @Insert suspend fun insertarSeguimiento(item: SeguimientoEntity): Long
    // LEFT JOIN permite mostrar tareas abiertas y completadas en un mismo historial.
    @Query("""
        SELECT s.*, u.usuario AS responsable,
               resolucion.fechaHora AS fechaResolucion,
               resolucion.resultado AS resultadoResolucion,
               autor.usuario AS autorResolucion
        FROM seguimientos s
        INNER JOIN usuarios u ON u.id = s.responsableId
        LEFT JOIN seguimientos resolucion ON resolucion.id = s.resueltaPorId
        LEFT JOIN usuarios autor ON autor.id = resolucion.responsableId
        WHERE s.oportunidadId = :id ORDER BY s.id DESC
    """)
    fun seguimientos(id: Int): Flow<List<SeguimientoDetalle>>

    // Cambios de estado y asignaciones son auditoría automática, no una llamada/reunión.
    // Una oportunidad solo sale del aviso cuando se registra una gestión comercial.
    @Query("SELECT DISTINCT oportunidadId FROM seguimientos WHERE accion NOT IN ('Cambio de estado', 'Asignación')")
    fun idsConGestionComercial(): Flow<List<Int>>

    @Query("SELECT * FROM seguimientos WHERE id = :id LIMIT 1")
    suspend fun obtenerSeguimiento(id: Int): SeguimientoEntity?

    // El responsable del aviso es el de la oportunidad AHORA, no quien creó la tarea.
    // Por eso una reasignación mueve los avisos, pero conserva el autor en el historial.
    // Las tareas completadas y las oportunidades terminales no generan avisos.
    @Query("""
        SELECT s.id AS seguimientoId, o.id AS oportunidadId,
               o.nombreOportunidad, o.responsableId, u.usuario AS responsable,
               s.proximaAccion, s.fechaProxima
        FROM seguimientos s
        INNER JOIN gestiones_oportunidad o ON o.id = s.oportunidadId
        INNER JOIN usuarios u ON u.id = o.responsableId
        WHERE s.resueltaPorId IS NULL
          AND TRIM(s.proximaAccion) <> '' AND s.fechaProxima <> ''
          AND o.estado NOT IN ('DESCARTADO', 'ACTIVACION_REALIZADA', 'CERRADO')
        ORDER BY s.fechaProxima ASC, s.id ASC
    """)
    fun observarRecordatorios(): Flow<List<RecordatorioSeguimiento>>

    @Insert
    suspend fun insertarResultado(item: ResultadoActivacionEntity)

    @Query("""
        SELECT r.*, s.oportunidadId, s.resultado AS evaluacion, s.observaciones,
               u.usuario AS responsable, s.fechaHora AS fechaRegistro
        FROM resultados_activacion r
        INNER JOIN seguimientos s ON s.id = r.seguimientoId
        INNER JOIN usuarios u ON u.id = s.responsableId
        WHERE s.oportunidadId = :id ORDER BY s.id DESC
    """)
    fun resultados(id: Int): Flow<List<ResultadoActivacionDetalle>>

    // Se consulta el contacto guardado EN LA GESTIÓN, no el enlace principal actual.
    @Query("""
        SELECT s.*, o.nombreOportunidad, u.usuario AS responsable
        FROM seguimientos s
        INNER JOIN gestiones_oportunidad o ON o.id = s.oportunidadId
        INNER JOIN usuarios u ON u.id = s.responsableId
        WHERE s.contactoId = :contactoId ORDER BY s.id DESC
    """)
    fun historialContacto(contactoId: Int): Flow<List<HistorialContactoItem>>

    @Query("""
        SELECT s.*, o.nombreOportunidad, u.usuario AS responsable
        FROM seguimientos s
        INNER JOIN gestiones_oportunidad o ON o.id = s.oportunidadId
        INNER JOIN usuarios u ON u.id = s.responsableId
        WHERE o.organizacionId = :organizacionId ORDER BY s.id DESC
    """)
    fun historialOrganizacion(organizacionId: Int): Flow<List<HistorialContactoItem>>

    // Actualización condicional: una tarea no se puede completar dos veces. El número
    // devuelto indica si se cambió una fila; Repository lo comprueba en la transacción.
    @Query("""
        UPDATE seguimientos SET resueltaPorId = :resolucionId
        WHERE id = :pendienteId AND oportunidadId = :oportunidadId
          AND resueltaPorId IS NULL
          AND TRIM(proximaAccion) <> '' AND fechaProxima <> ''
    """)
    suspend fun resolverAccion(pendienteId: Int, oportunidadId: Int, resolucionId: Int): Int
}
