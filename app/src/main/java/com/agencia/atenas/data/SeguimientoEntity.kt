package com.agencia.atenas.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * CAMBIO: la gestión referencia al usuario por responsableId, sin copiar su login.
 * Al leer el historial se une usuarios para mostrar quién realizó la acción.
 * El id del autor no cambia si la oportunidad se reasigna a otra persona.
 */
@Entity(
    tableName = "seguimientos",
    foreignKeys = [
        ForeignKey(entity = GestionOportunidadEntity::class, parentColumns = ["id"],
            childColumns = ["oportunidadId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id"],
            childColumns = ["responsableId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ContactoEntity::class, parentColumns = ["id"],
            childColumns = ["contactoId"], onDelete = ForeignKey.RESTRICT),
        // La resolución es otra gestión con acción, resultado, fecha y autor propios.
        // En lugar de duplicar esos datos, apuntamos al registro que completó la tarea.
        ForeignKey(entity = SeguimientoEntity::class, parentColumns = ["id"],
            childColumns = ["resueltaPorId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("oportunidadId"), Index("responsableId"), Index("contactoId"),
        Index(value = ["resueltaPorId"], unique = true)]
)
data class SeguimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val oportunidadId: Int,
    val responsableId: Int,
    val fechaHora: String,
    val accion: String,
    val resultado: String,
    val proximaAccion: String,
    val fechaProxima: String,
    val observaciones: String,
    // null: la próxima acción sigue abierta. Un ID: otra gestión registró su resultado.
    // Esta columna no afecta a la acción ya realizada de esta fila: solo a su próxima acción.
    val resueltaPorId: Int? = null,
    // Guarda con quién se realizó esta gestión. Cambiar luego el contacto principal
    // no mueve las gestiones antiguas a otra persona. null = no se conocía un contacto.
    val contactoId: Int? = null
)

// Proyección de lectura: combina la gestión con el nombre actual de su autor.
data class SeguimientoDetalle(
    @Embedded val seguimiento: SeguimientoEntity,
    val responsable: String,
    val fechaResolucion: String? = null,
    val resultadoResolucion: String? = null,
    val autorResolucion: String? = null
) {
    @get:Ignore
    val fechaHora get() = seguimiento.fechaHora
    @get:Ignore
    val accion get() = seguimiento.accion
    @get:Ignore
    val resultado get() = seguimiento.resultado
    @get:Ignore
    val proximaAccion get() = seguimiento.proximaAccion
    @get:Ignore
    val fechaProxima get() = seguimiento.fechaProxima
    @get:Ignore
    val observaciones get() = seguimiento.observaciones
}
