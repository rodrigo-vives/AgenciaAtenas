package com.agencia.atenas.ui

/**
 * Representa lo que se ve en Crear: valores ingresados, errores y progreso del guardado.
 * Estos datos son temporales; solo se convierten en entidad cuando el usuario guarda.
 */

// CAMBIO: se conserva el formulario aprendido, separando valores temporales de datos Room.
// Los números siguen siendo texto aquí para admitir campos vacíos mientras se escribe.
// Organización/contacto se muestran desde catálogos; solo sus IDs se vinculan al guardar.
data class CrearUiState(
    val nombreOportunidad: String = "",
    val tipoOportunidad: String = "",
    val descripcion: String = "",
    val historialObservaciones: String = "",
    val fuenteInformacion: String = "",
    val fechaDeteccion: String = "",
    val responsable: String = "",
    val prioridad: String = "Media",
    val potencialComercial: String = "",
    // Datos del evento: los números se editan como texto hasta validar y guardar.
    val fechaInicio: String = "",
    val fechaTermino: String = "",
    val horario: String = "",
    val ubicacionGeneral: String = "",
    val comuna: String = "",
    val region: String = "",
    val publicoEsperado: String = "",
    val publicoObjetivo: String = "",
    val cantidadAsistentes: String = "",
    val condicionesParticipacion: String = "",
    val requisitos: String = "",
    val espaciosDisponibles: String = "",
    val costosReferenciales: String = "",
    // Organización
    val nombreOrganizacion: String = "",
    val tipoOrganizacion: String = "",
    val rubroOrganizacion: String = "",
    // Contacto
    val nombreContacto: String = "",
    val cargoContacto: String = "",
    val areaContacto: String = "",
    val correoContacto: String = "",
    val telefonoContacto: String = "",
    val medioContactoPreferente: String = "",

    // CAMBIO: errores por campo y resultado de guardado; el contenido no se borra si falla.
    // Estos ids son los vínculos persistidos. Los nombres de arriba solo presentan datos.
    val organizacionId: Int? = null,
    val contactoId: Int? = null,
    val id: Int = 0,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoId: Int? = null,
    val errores: Map<String, String> = emptyMap(),
    val mensaje: String = ""
)
