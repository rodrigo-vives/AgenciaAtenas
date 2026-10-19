package com.agencia.atenas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Esquema 5 para instalación limpia, elegido por el equipo sin migraciones.
 * Añade datos de organización, contacto histórico y resultados de activación.
 * No borra ni convierte automáticamente una base anterior. Probar en otro emulador
 * conserva intactos los datos de la instalación antigua.
 */
@Database(
    entities = [UsuarioEntity::class, GestionOportunidadEntity::class,
        OrganizacionEntity::class, ContactoEntity::class, SeguimientoEntity::class,
        OportunidadContactoEntity::class, ResultadoActivacionEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun catalogoDao(): CatalogoDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtenerBaseDeDatos(context: Context): AppDatabase {
            // Se revisa de nuevo dentro del bloqueo: otra llamada puede haber creado la base.
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agencia_atenas.db"
                )
                    .build().also { instancia = it }
            }
        }
    }
}
