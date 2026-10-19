package com.agencia.atenas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room traduce estas anotaciones a consultas. suspend obtiene un resultado puntual;
 * Flow mantiene una observación, por ejemplo para retirar acceso al desactivar una cuenta.
 * La autenticación y las reglas de rol están en UsuarioRepository/OportunidadRepository.
 */
@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE usuario = :login LIMIT 1")
    suspend fun buscarPorLogin(login: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtener(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observar(id: Int): Flow<UsuarioEntity?>

    // IGNORE no sobrescribe usuarios existentes ni resetea su contraseña al iniciar.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE rol = 'EJECUTIVO' AND activo = 1 ORDER BY usuario")
    fun observarEjecutivos(): Flow<List<UsuarioEntity>>
}
