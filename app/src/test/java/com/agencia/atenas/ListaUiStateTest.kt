package com.agencia.atenas

import com.agencia.atenas.data.*
import com.agencia.atenas.ui.ListaUiState
import org.junit.Assert.*
import org.junit.Test

class ListaUiStateTest {
    private fun oportunidad(id: Int, nombre: String, estado: EstadoOportunidad): OportunidadDetalle {
        return OportunidadDetalle(
            oportunidad = GestionOportunidadEntity(
                id = id,
                nombreOportunidad = nombre,
                tipoOportunidad = "",
                descripcion = "",
                fuenteInformacion = "",
                fechaDeteccion = "",
                prioridad = "",
                potencialComercial = "",
                fechaInicio = "",
                fechaTermino = "",
                horario = "",
                comuna = "",
                region = "",
                publicoEsperado = "",
                cantidadAsistentes = null,
                condicionesParticipacion = "",
                costosReferenciales = "",
                estado = estado,
                historialObservaciones = "",
                ubicacionGeneral = "",
                publicoObjetivo = "",
                requisitos = "",
                espaciosDisponibles = "",
                fechaUltimaActualizacion = "",
                responsableId = 1,
                organizacionId = null
            ),
            responsable = "admin", contactoId = null,
            nombreOrganizacion = "", tipoOrganizacion = "", rubroOrganizacion = "",
            nombreContacto = "", cargoContacto = "", areaContacto = "",
            correoContacto = "", telefonoContacto = "", medioContactoPreferente = ""
        )
    }

    @Test fun laInstantaneaNuevaIncluyeLosDatosQueLleganDespues() {
        val inicial = ListaUiState(cargando = true)
        assertTrue(inicial.gestionesFiltradas.isEmpty())
        val recibida = inicial.copy(cargando = false, gestiones = listOf(
            oportunidad(1, "op2", EstadoOportunidad.IDENTIFICADO)
        ))
        assertEquals(1, recibida.gestionesFiltradas.size)
        assertEquals("op2", recibida.gestionesFiltradas.single().nombreOportunidad)
    }

    @Test fun combinaNombreYEstadoYSuLimpiezaRecuperaRegistros() {
        val datos = ListaUiState(gestiones = listOf(
            oportunidad(1, "Feria A", EstadoOportunidad.CONFIRMADO),
            oportunidad(2, "Feria B", EstadoOportunidad.EN_ESPERA),
            oportunidad(3, "Municipal", EstadoOportunidad.IDENTIFICADO)
        ))
        val filtrado = datos.copy(textoBusqueda = "FERIA", filtroEstado = "EN_ESPERA")
        assertEquals(listOf(2), filtrado.gestionesFiltradas.map { it.id })
        assertEquals(2, filtrado.copy(filtroEstado = "").gestionesFiltradas.size)
        assertEquals(3, filtrado.copy(filtroEstado = "", textoBusqueda = "").gestionesFiltradas.size)
    }

    private fun datosFiltros(): ListaUiState {
        val primera = oportunidad(1, "Feria A", EstadoOportunidad.CONFIRMADO).let {
            it.copy(oportunidad = it.oportunidad.copy(tipoOportunidad = "Feria", prioridad = "Alta",
                fechaInicio = "2026-10-17", comuna = "Santiago", region = "Metropolitana",
                ubicacionGeneral = "Recinto Norte", organizacionId = 7, responsableId = 2),
                nombreOrganizacion = "Empresa repetida", responsable = "ejecutivo1")
        }
        val segunda = primera.copy(oportunidad = primera.oportunidad.copy(id = 2,
            fechaInicio = "2026-10-20", prioridad = "Baja", organizacionId = 8, responsableId = 3), responsable = "ejecutivo2")
        return ListaUiState(gestiones = listOf(primera, segunda))
    }

    @Test fun combinaTodosLosFiltrosSinConfundirNombresIguales() {
        val estado = datosFiltros().copy(textoBusqueda = " feria ", filtroEstado = "CONFIRMADO",
            filtroPrioridad = "Alta", filtroTipo = "Feria", filtroUbicacion = "norte",
            filtroOrganizacionId = 7, filtroResponsableId = 2,
            fechaDesde = "17/10/2026", fechaHasta = "17/10/2026")
        assertEquals(listOf(1), estado.gestionesFiltradas.map { it.id })
        assertTrue(estado.copy(filtroOrganizacionId = 8).gestionesFiltradas.isEmpty())
        assertTrue(estado.copy(filtroResponsableId = 3).gestionesFiltradas.isEmpty())
    }

    @Test fun rangoIncluyeExtremosYAdmiteUnSoloLimite() {
        val estado = datosFiltros()
        assertEquals(2, estado.copy(fechaDesde = "17/10/2026", fechaHasta = "20/10/2026").gestionesFiltradas.size)
        assertEquals(listOf(2), estado.copy(fechaDesde = "18/10/2026").gestionesFiltradas.map { it.id })
        assertEquals(listOf(1), estado.copy(fechaHasta = "18/10/2026").gestionesFiltradas.map { it.id })
    }

    @Test fun fechasInvalidasNoProducenResultadosEnganosos() {
        val estado = datosFiltros().copy(fechaDesde = "31/02/2026")
        assertTrue(estado.erroresFechas.containsKey("desde"))
        assertTrue(estado.gestionesFiltradas.isEmpty())
        val invertido = datosFiltros().copy(fechaDesde = "20/10/2026", fechaHasta = "17/10/2026")
        assertTrue(invertido.erroresFechas.containsKey("hasta"))
    }

    @Test fun ubicacionBuscaComunaRegionYRecintoSinDistinguirMayusculas() {
        listOf("santiago", "METROPOLITANA", "norte").forEach {
            assertEquals(2, datosFiltros().copy(filtroUbicacion = it).gestionesFiltradas.size)
        }
        assertTrue(datosFiltros().copy(filtroUbicacion = "Valdivia").gestionesFiltradas.isEmpty())
    }

    @Test fun limpiarConservaDatosYRecuperaTodasLasOportunidades() {
        val estado = datosFiltros().copy(textoBusqueda = "No coincide", filtroPrioridad = "Alta",
            filtroTipo = "Feria", filtroOrganizacionId = 7, fechaDesde = "17/10/2026")
        val limpio = estado.sinFiltros()
        assertEquals(0, limpio.cantidadFiltros)
        assertEquals("", limpio.textoBusqueda)
        assertEquals(estado.gestiones, limpio.gestionesFiltradas)
    }
}
