package com.agencia.atenas.ui

import com.agencia.atenas.data.OportunidadDetalle
import com.agencia.atenas.utils.Fechas

/**
 * Instantánea del listado: datos recibidos de Room y filtros elegidos por el usuario.
 * La lista visible se calcula a partir de ESTA instantánea, no leyendo otro estado
 * desde el ViewModel mientras Compose dibuja una parte diferente de la pantalla.
 */
data class ListaUiState(
    val gestiones: List<OportunidadDetalle> = emptyList(),
    val textoBusqueda: String = "",
    val filtroEstado: String = "",
    val filtroPrioridad: String = "",
    val filtroTipo: String = "",
    val filtroUbicacion: String = "",
    val filtroOrganizacionId: Int? = null,
    val filtroResponsableId: Int? = null,
    val fechaDesde: String = "",
    val fechaHasta: String = "",
    val cargando: Boolean = false,
    val mensajeError: String = ""
) {
    // El usuario escribe DD/MM/AAAA. Solo después de validar convertimos a ISO,
    // donde comparar texto equivale a comparar fechas. El rango incluye sus extremos.
    val erroresFechas: Map<String, String>
        get() {
            val errores = mutableMapOf<String, String>()
            val desde = Fechas.aIso(fechaDesde)
            val hasta = Fechas.aIso(fechaHasta)
            if (fechaDesde.isNotBlank() && desde == null) errores["desde"] = "Usa una fecha real DD/MM/AAAA"
            if (fechaHasta.isNotBlank() && hasta == null) errores["hasta"] = "Usa una fecha real DD/MM/AAAA"
            if (desde != null && hasta != null && hasta < desde) errores["hasta"] = "No puede ser anterior a Desde"
            return errores
        }

    val cantidadFiltros: Int
        get() = listOf(filtroEstado.isNotBlank(), filtroPrioridad.isNotBlank(),
            filtroTipo.isNotBlank(), filtroUbicacion.isNotBlank(), filtroOrganizacionId != null,
            filtroResponsableId != null, fechaDesde.isNotBlank(), fechaHasta.isNotBlank()).count { it }

    // Se aplican TODOS los criterios con AND sobre la lista ya autorizada por Repository.
    // Un filtro nunca amplía permisos ni obtiene oportunidades ajenas desde Room.
    val gestionesFiltradas: List<OportunidadDetalle>
        get() {
            if (erroresFechas.isNotEmpty()) return emptyList()
            val desde = Fechas.aIso(fechaDesde)
            val hasta = Fechas.aIso(fechaHasta)
            return gestiones.filter { gestion ->
                val fechaValida = Fechas.aIso(Fechas.visible(gestion.fechaInicio)) == gestion.fechaInicio &&
                    gestion.fechaInicio.isNotBlank()
                val coincideNombre = textoBusqueda.isBlank() ||
                    gestion.nombreOportunidad.contains(textoBusqueda.trim(), ignoreCase = true)
                val coincideEstado = filtroEstado.isBlank() || gestion.estado.name == filtroEstado
                val coincidePrioridad = filtroPrioridad.isBlank() || gestion.prioridad == filtroPrioridad
                val coincideTipo = filtroTipo.isBlank() || gestion.tipoOportunidad == filtroTipo
                val coincideUbicacion = filtroUbicacion.isBlank() ||
                    listOf(gestion.comuna, gestion.region, gestion.ubicacionGeneral).any {
                        it.contains(filtroUbicacion.trim(), ignoreCase = true)
                    }
                // IDs distinguen organizaciones o usuarios aunque sus nombres se repitan.
                val coincideOrganizacion = filtroOrganizacionId == null || gestion.organizacionId == filtroOrganizacionId
                val coincideResponsable = filtroResponsableId == null || gestion.responsableId == filtroResponsableId
                val coincideFecha = (desde == null && hasta == null) ||
                    (fechaValida && (desde == null || gestion.fechaInicio >= desde) &&
                        (hasta == null || gestion.fechaInicio <= hasta))
                coincideNombre && coincideEstado && coincidePrioridad && coincideTipo &&
                    coincideUbicacion && coincideOrganizacion && coincideResponsable && coincideFecha
            }
        }

    // Se conservan los datos recibidos, la carga y cualquier error de lectura.
    // Limpiar también quita la búsqueda: no deja un criterio escondido activo.
    fun sinFiltros() = copy(textoBusqueda = "", filtroEstado = "", filtroPrioridad = "",
        filtroTipo = "", filtroUbicacion = "", filtroOrganizacionId = null,
        filtroResponsableId = null, fechaDesde = "", fechaHasta = "")
}
