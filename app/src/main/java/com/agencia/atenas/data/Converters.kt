package com.agencia.atenas.data

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun deEstadoAString(estado: EstadoOportunidad): String = estado.name
    @TypeConverter
    fun deStringAEstado(nombre: String): EstadoOportunidad = EstadoOportunidad.valueOf(nombre)

}

/*
Y en AppDatabase.kt:

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { ... }
*/