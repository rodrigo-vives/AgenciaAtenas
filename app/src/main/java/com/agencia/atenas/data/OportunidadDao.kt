package com.agencia.atenas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Alias SQL: o = oportunidad, u = usuario, org = organización, c = contacto.
// INNER JOIN exige el responsable existente; las otras relaciones son opcionales.
// Las etiquetas AS deben coincidir con las propiedades de OportunidadDetalle.
// LEFT JOIN mantiene visibles oportunidades sin organización o sin contacto conocido.
// COALESCE convierte los valores SQL null de esos catálogos en textos de presentación.
const val CONSULTA_OPORTUNIDADES = """
    SELECT o.*, u.usuario AS responsable, enlace.contactoId AS contactoId,
           COALESCE(org.nombre, '') AS nombreOrganizacion,
           COALESCE(org.tipo, '') AS tipoOrganizacion,
           COALESCE(org.rubro, '') AS rubroOrganizacion,
           COALESCE(c.nombre, '') AS nombreContacto,
           COALESCE(c.cargo, '') AS cargoContacto,
           COALESCE(c.area, '') AS areaContacto,
           COALESCE(c.correo, '') AS correoContacto,
           COALESCE(c.telefono, '') AS telefonoContacto,
           COALESCE(c.medio, '') AS medioContactoPreferente
    FROM gestiones_oportunidad o
    INNER JOIN usuarios u ON u.id = o.responsableId
    LEFT JOIN organizaciones org ON org.id = o.organizacionId
    LEFT JOIN oportunidad_contacto enlace ON enlace.oportunidadId = o.id
    LEFT JOIN contactos c ON c.id = enlace.contactoId
"""

/** Room genera la implementación. Este archivo declara consultas, no decide permisos. */
@Dao
interface OportunidadDao {
    // Insert devuelve el ID generado; se necesita para vincular el contacto en la misma transacción.
    @Insert suspend fun insertar(item: GestionOportunidadEntity): Long
    @Update suspend fun actualizar(item: GestionOportunidadEntity)

    // Flow emite de nuevo al cambiar las tablas consultadas. No se recarga la lista a mano.
    @Query(CONSULTA_OPORTUNIDADES + " ORDER BY o.fechaInicio ASC")
    fun obtenerTodas(): Flow<List<OportunidadDetalle>>

    @Query(CONSULTA_OPORTUNIDADES + " WHERE o.id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): OportunidadDetalle?

    // Quitar el vínculo no elimina el contacto del catálogo ni la oportunidad.
    @Query("DELETE FROM oportunidad_contacto WHERE oportunidadId = :id")
    suspend fun quitarContacto(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun vincularContacto(enlace: OportunidadContactoEntity)
}
