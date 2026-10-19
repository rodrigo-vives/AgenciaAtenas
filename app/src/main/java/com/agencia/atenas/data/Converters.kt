package com.agencia.atenas.data

import androidx.room.TypeConverter

/**
 * SQLite no guarda directamente un enum de Kotlin. Room usa estos dos métodos
 * para escribir su nombre estable (por ejemplo EN_ESPERA) y reconstruirlo al leer.
 * La etiqueta visible del menú es distinta y puede cambiar sin cambiar lo guardado.
 * AppDatabase activa esta clase mediante @TypeConverters(Converters::class).
 */
class Converters {
    @TypeConverter
    fun deEstadoAString(estado: EstadoOportunidad): String = estado.name

    @TypeConverter
    fun deStringAEstado(nombre: String): EstadoOportunidad = EstadoOportunidad.valueOf(nombre)
}
