package com.agencia.atenas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {

    @Insert
    suspend fun insertar(gestion: GestionOportunidadEntity): Long

    @Update
    suspend fun actualizar(gestion: GestionOportunidadEntity)

    @Delete
    suspend fun eliminar(gestion: GestionOportunidadEntity)

    @Query("SELECT * FROM gestiones_oportunidad ORDER BY fechaInicio ASC")
    fun obtenerTodas(): Flow<List<GestionOportunidadEntity>>

    @Query("SELECT * FROM gestiones_oportunidad WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): GestionOportunidadEntity?

    @Query("SELECT * FROM gestiones_oportunidad WHERE nombreOportunidad LIKE '%' || :texto || '%' ORDER BY fechaInicio ASC")
    fun buscarPorNombre(texto: String): Flow<List<GestionOportunidadEntity>>

    @Query("SELECT * FROM gestiones_oportunidad WHERE estado = :estado ORDER BY fechaInicio ASC")
    fun filtrarPorEstado(estado: String): Flow<List<GestionOportunidadEntity>>
}