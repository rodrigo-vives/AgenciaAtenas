package com.agencia.atenas.ui

/*
 * Este archivo es una solución para no escribir muchas veces el mismo OutlinedTextField.
 * La lista almacena claves y etiquetas, no los valores ingresados. valor(clave) lee el valor
 * correspondiente de CrearUiState mediante un when. CrearViewModel.cambiar usa otra rama when
 * para actualizarlo. Si una clave está mal escrita, no se conectará el campo correctamente.
 * Se eligió esta repetición explícita en lugar de reflexión para poder seguirla en clase.
 */


/**
 * Lista las etiquetas y las claves de los campos para no repetir el formulario completo.
 * valor(clave) permite leer una propiedad por su clave. Las claves deben coincidir con cambiar()
 * en CrearViewModel. Agregar un campo exige actualizar esas dos correspondencias.
 */

// CAMBIO: describe campos repetidos en un solo lugar; las validaciones siguen en utils.
// Los identificadores coinciden con las propiedades que ustedes ya escribieron.
data class CampoFormulario(val clave: String,
    val etiqueta: String)
val camposFormulario = listOf(
    CampoFormulario("nombreOportunidad",
        "Nombre de la oportunidad"),
    CampoFormulario("tipoOportunidad",
        "Tipo de oportunidad"),
    CampoFormulario("descripcion",
        "Descripción"),
    CampoFormulario("historialObservaciones", "Observaciones de la oportunidad"),
    CampoFormulario("fuenteInformacion",
        "Fuente de información"),
    CampoFormulario("fechaDeteccion",
        "Fecha de detección (DD/MM/AAAA)"),
    CampoFormulario("responsable",
        "Responsable"),
    CampoFormulario("prioridad",
        "Prioridad (Alta, Media o Baja)"),
    CampoFormulario("potencialComercial",
        "Potencial comercial"),
    CampoFormulario("fechaInicio",
        "Fecha de inicio (DD/MM/AAAA)"),
    CampoFormulario("fechaTermino",
        "Fecha de término (DD/MM/AAAA)"),
    CampoFormulario("horario",
        "Horario (HH:mm a HH:mm)"),
    CampoFormulario("ubicacionGeneral",
        "Ubicación general"),
    CampoFormulario("comuna",
        "Comuna"),
    CampoFormulario("region",
        "Región"),
    CampoFormulario("publicoEsperado",
        "Público esperado"),
    CampoFormulario("publicoObjetivo",
        "Público objetivo"),
    CampoFormulario("cantidadAsistentes",
        "Cantidad estimada de asistentes"),
    CampoFormulario("condicionesParticipacion",
        "Condiciones de participación"),
    CampoFormulario("requisitos",
        "Requisitos"),
    CampoFormulario("espaciosDisponibles",
        "Espacios disponibles"),
    CampoFormulario("costosReferenciales",
        "Costo referencial ficticio (número CLP)"),
    CampoFormulario("nombreOrganizacion",
        "Organización"),
    CampoFormulario("tipoOrganizacion",
        "Tipo de organización"),
    CampoFormulario("rubroOrganizacion",
        "Rubro"),
    CampoFormulario("nombreContacto",
        "Nombre del contacto"),
    CampoFormulario("cargoContacto",
        "Cargo"),
    CampoFormulario("areaContacto",
        "Área"),
    CampoFormulario("correoContacto",
        "Correo ficticio"),
    CampoFormulario("telefonoContacto",
        "Teléfono ficticio"),
    CampoFormulario("medioContactoPreferente",
        "Medio preferente"),
)

fun CrearUiState.valor(clave: String): String = when(clave) {
    "nombreOportunidad" -> nombreOportunidad
    "tipoOportunidad" -> tipoOportunidad
    "descripcion" -> descripcion
    "historialObservaciones" -> historialObservaciones
    "fuenteInformacion" -> fuenteInformacion
    "fechaDeteccion" -> fechaDeteccion
    "responsable" -> responsable
    "prioridad" -> prioridad
    "potencialComercial" -> potencialComercial
    "fechaInicio" -> fechaInicio
    "fechaTermino" -> fechaTermino
    "horario" -> horario
    "ubicacionGeneral" -> ubicacionGeneral
    "comuna" -> comuna
    "region" -> region
    "publicoEsperado" -> publicoEsperado
    "publicoObjetivo" -> publicoObjetivo
    "cantidadAsistentes" -> cantidadAsistentes
    "condicionesParticipacion" -> condicionesParticipacion
    "requisitos" -> requisitos
    "espaciosDisponibles" -> espaciosDisponibles
    "costosReferenciales" -> costosReferenciales
    "nombreOrganizacion" -> nombreOrganizacion
    "tipoOrganizacion" -> tipoOrganizacion
    "rubroOrganizacion" -> rubroOrganizacion
    "nombreContacto" -> nombreContacto
    "cargoContacto" -> cargoContacto
    "areaContacto" -> areaContacto
    "correoContacto" -> correoContacto
    "telefonoContacto" -> telefonoContacto
    "medioContactoPreferente" -> medioContactoPreferente
    else -> ""
}
