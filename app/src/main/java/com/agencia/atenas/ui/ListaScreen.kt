package com.agencia.atenas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.EstadoOportunidad
import com.agencia.atenas.data.OportunidadDetalle
import com.agencia.atenas.ui.theme.*
import com.agencia.atenas.utils.*

@Composable
fun ListaScreen(onIrACrear: () -> Unit, onIrADetalle: (Int) -> Unit,
    onCerrarSesion: () -> Unit, viewModel: ListaViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var filtrosAbiertos by rememberSaveable { mutableStateOf(false) }
    var hoy by remember { mutableStateOf(Fechas.hoy()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { hoy = Fechas.hoy() }
    // Las opciones salen de datos que el usuario puede ver. No se consulta un catálogo
    // global para revelar responsables u organizaciones de oportunidades ajenas.
    val tipos = estado.gestiones.map { it.tipoOportunidad }.filter { it.isNotBlank() }.distinct().sorted()
    val organizaciones = estado.gestiones.filter { it.organizacionId != null }
        .distinctBy { it.organizacionId }.associate { "${it.organizacionId}: ${it.nombreOrganizacion}" to it.organizacionId }
    val responsables = estado.gestiones.distinctBy { it.responsableId }
        .associate { "${it.responsableId}: ${it.responsable}" to it.responsableId }
    val visibles = estado.gestionesFiltradas

    Scaffold { padding ->
        // Un único desplazamiento incluye filtros y resultados. Así el teclado o una
        // pantalla pequeña no dejan la lista sin espacio ni ocultan el botón Limpiar.
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Oportunidades", style = MaterialTheme.typography.headlineSmall)
                Text("Hola, ${SesionActual.usuario}")
                TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                OutlinedTextField(value = estado.textoBusqueda, onValueChange = viewModel::cambiarBusqueda,
                    label = { Text("Buscar por nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { filtrosAbiertos = !filtrosAbiertos }) {
                    Text("${if (filtrosAbiertos) "Ocultar" else "Mostrar"} filtros (${estado.cantidadFiltros})")
                }
            }
            if (filtrosAbiertos) item {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Combina los filtros que necesites", style = MaterialTheme.typography.titleMedium)
                        Selector("Estado", EstadoOportunidad.entries.firstOrNull { it.name == estado.filtroEstado }?.etiqueta ?: "Todos",
                            listOf("Todos") + EstadoOportunidad.entries.map { it.etiqueta }) { elegido ->
                            viewModel.cambiarFiltroEstado(EstadoOportunidad.entries.firstOrNull { it.etiqueta == elegido }?.name.orEmpty())
                        }
                        Selector("Prioridad", estado.filtroPrioridad.ifBlank { "Todas" }, listOf("Todas", "Alta", "Media", "Baja")) {
                            viewModel.cambiarPrioridad(if (it == "Todas") "" else it)
                        }
                        Selector("Tipo", estado.filtroTipo.ifBlank { "Todos" }, listOf("Todos") + tipos) {
                            viewModel.cambiarTipo(if (it == "Todos") "" else it)
                        }
                        Campo("Comuna, región o ubicación", estado.filtroUbicacion, onCambio = viewModel::cambiarUbicacion)
                        Selector("Organización", organizaciones.entries.firstOrNull { it.value == estado.filtroOrganizacionId }?.key ?: "Todas",
                            listOf("Todas") + organizaciones.keys) { viewModel.cambiarOrganizacion(organizaciones[it]) }
                        if (SesionActual.esSupervisor) {
                            Selector("Responsable", responsables.entries.firstOrNull { it.value == estado.filtroResponsableId }?.key ?: "Todos",
                                listOf("Todos") + responsables.keys) { viewModel.cambiarResponsable(responsables[it]) }
                        }
                        Text("Rango de fecha de inicio del evento (incluye ambos días)")
                        Campo("Desde DD/MM/AAAA", estado.fechaDesde, estado.erroresFechas["desde"], onCambio = viewModel::cambiarDesde)
                        Campo("Hasta DD/MM/AAAA", estado.fechaHasta, estado.erroresFechas["hasta"], onCambio = viewModel::cambiarHasta)
                    }
                }
            }
            item {
                if (estado.cantidadFiltros > 0 || estado.textoBusqueda.isNotBlank()) {
                    TextButton(onClick = viewModel::limpiarFiltros) { Text("Limpiar filtros y búsqueda") }
                }
                Button(onClick = onIrACrear, modifier = Modifier.fillMaxWidth()) { Text("+ Nueva oportunidad") }
                if (estado.mensajeError.isNotBlank()) Text(estado.mensajeError, color = MaterialTheme.colorScheme.error)
                if (estado.cargando) CircularProgressIndicator()
                if (estado.erroresFechas.isNotEmpty()) {
                    Text("Corrige las fechas en Filtros para consultar el rango", color = MaterialTheme.colorScheme.error)
                } else if (!estado.cargando) {
                    Text("${visibles.size} de ${estado.gestiones.size} oportunidades", style = MaterialTheme.typography.titleMedium)
                    if (visibles.isEmpty()) Text(if (estado.gestiones.isEmpty())
                        "No hay oportunidades registradas para este usuario" else "No hay coincidencias con los filtros elegidos")
                }
            }
            items(visibles, key = { it.id }) { gestion ->
                TarjetaGestion(gestion, hoy, onClick = { onIrADetalle(gestion.id) })
            }
        }
    }
}

@Composable
fun TarjetaGestion(
    gestion: OportunidadDetalle,
    hoy: String = Fechas.hoy(),
    onClick: () -> Unit
) {

    // Antes el color solo representaba el estado. Ahora la alerta tiene prioridad:
    // hoy se destaca en rojo suave; dentro de siete días, en naranja. Sin alerta
    // se conserva el color por estado que tenía el trabajo original del compañero.
    // La función auxiliar calcula la fecha; este Composable solo decide cómo mostrarla.
    val diasRestantes = diasDeAlertaEvento(gestion.fechaInicio, gestion.estado, hoy)
    val colorTarjeta = when {
        diasRestantes == 0 -> FondoEventoHoy
        diasRestantes != null -> FondoEventoProximo
        gestion.estado == EstadoOportunidad.CONFIRMADO -> FondoOportunidadConfirmada
        gestion.estado == EstadoOportunidad.EN_ESPERA -> FondoOportunidadEnEspera
        gestion.estado == EstadoOportunidad.DESCARTADO -> FondoOportunidadDescartada
        else -> FondoOportunidadNormal
    }

    // El texto acompaña al color para que se entienda la alerta sin distinguir colores.
    val mensajeAlerta = when (diasRestantes) {
        null -> null
        0 -> "Evento hoy"
        1 -> "Evento mañana"
        else -> "Evento en $diasRestantes días"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorTarjeta,
            contentColor = TextoTarjetaOportunidad
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = gestion.nombreOportunidad,
                style = MaterialTheme.typography.titleMedium
            )
            if (mensajeAlerta != null) {
                Text(
                    text = mensajeAlerta,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(text = "Organización: ${gestion.nombreOrganizacion}")
            Text(text = "Fecha: ${Fechas.visible(gestion.fechaInicio)}")
            Text(text = "Estado: ${gestion.estado.etiqueta}")
            Text(text = "Prioridad: ${gestion.prioridad}")
        }
    }
}