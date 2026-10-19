package com.agencia.atenas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Tabla de organizaciones reutilizables. El id identifica cada registro aunque se repita un nombre.
 */

// CAMBIO: catálogos separados para registrar y reutilizar organizaciones y contactos.
@Entity(tableName = "organizaciones")

data class OrganizacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val rubro: String,
    // Información complementaria del caso. Vacío significa desconocida, no inventada.
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = ""
)
