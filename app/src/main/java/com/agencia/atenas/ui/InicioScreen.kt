package com.agencia.atenas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.EstadoOportunidad
import com.agencia.atenas.data.RecordatorioSeguimiento
import com.agencia.atenas.utils.*

/**
 * Recordatorios internos: se muestran al abrir Inicio, sin notificaciones del sistema
 * ni permisos nuevos. La clasificación por fecha es calculada, no otro estado en Room.
 */
@Composable
fun InicioScreen(
    onLista: () -> Unit,
    onPerfil: () -> Unit,
    onOrganizaciones: () -> Unit,
    onContactos: () -> Unit,
    onSupervisor: () -> Unit,
    onSalir: () -> Unit,
    onSeguimiento: (Int, Int) -> Unit,
    onDetalle: (Int) -> Unit,
    viewModel: InicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var hoy by remember { mutableStateOf(Fechas.hoy()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { hoy = Fechas.hoy() }

    // Leer el estado en este mismo ámbito evita calcular listas con una instantánea
    // antigua mientras solo se recompone el contenido interno de Pagina.
    val atrasados = estado.recordatorios.filter {
        situacionRecordatorio(it.fechaProxima, hoy = hoy) == SituacionRecordatorio.ATRASADO
    }
    val pendientes = estado.recordatorios.filter {
        situacionRecordatorio(it.fechaProxima, hoy = hoy) in
            listOf(SituacionRecordatorio.HOY, SituacionRecordatorio.PENDIENTE)
    }
    val paraHoy = pendientes.count { it.fechaProxima == hoy }
    val activos = estado.oportunidades.filter { it.estado !in listOf(
        EstadoOportunidad.DESCARTADO, EstadoOportunidad.ACTIVACION_REALIZADA, EstadoOportunidad.CERRADO
    ) }
    val proximos = activos.filter { it.fechaInicio >= hoy }.sortedBy { it.fechaInicio }.take(3)
    val sinContacto = activos.filter { it.contactoId == null }
    val sinRespuesta = activos.filter { it.estado in listOf(
        EstadoOportunidad.PROPUESTA_ENVIADA, EstadoOportunidad.EN_ESPERA
    ) }

    Pagina("Inicio", onSalir, mostrarVolver = false) {
        Text("${SesionActual.usuario} · ${if (SesionActual.esSupervisor) "Supervisor comercial" else "Ejecutivo comercial"}")
        if (estado.cargando) CircularProgressIndicator()
        MensajeOperacion(estado.mensaje, estado.esError)
        // Un resumen con colores del tema se adapta al modo claro y oscuro.
        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (SesionActual.esSupervisor) "Resumen del equipo" else "Mi resumen",
                    style = MaterialTheme.typography.titleMedium)
                Text("Oportunidades visibles: ${estado.oportunidades.size}")
                Text("${atrasados.size} gestiones atrasadas · $paraHoy acciones para hoy")
            }
        }
        TextButton(onClick = onPerfil) { Text("Mi perfil") }
        Text("Accesos", style = MaterialTheme.typography.titleMedium)
        Button(onClick = onLista) { Text("Oportunidades") }
        Button(onClick = onOrganizaciones) { Text("Organizaciones") }
        Button(onClick = onContactos) { Text("Contactos") }
        if (SesionActual.esSupervisor) {
            Button(onClick = onSupervisor) { Text("Panel de supervisor") }
        }

        if (!estado.cargando) {
            Text("Recordatorios internos", style = MaterialTheme.typography.titleLarge)
            Text("${atrasados.size} atrasados · $paraHoy para hoy · ${pendientes.size - paraHoy} próximos")
            Text("Las acciones no desaparecen al llegar su fecha: registra su resultado para completarlas.")

            Text("Gestiones atrasadas (${atrasados.size})", style = MaterialTheme.typography.titleMedium)
            if (atrasados.isEmpty()) Text("No hay gestiones atrasadas")
            atrasados.forEach { recordatorio ->
                TarjetaRecordatorio(recordatorio, "Atrasada", true) {
                    onSeguimiento(recordatorio.oportunidadId, recordatorio.seguimientoId)
                }
            }

            Text("Seguimientos pendientes (${pendientes.size})", style = MaterialTheme.typography.titleMedium)
            if (pendientes.isEmpty()) Text("No hay acciones programadas para hoy o después")
            pendientes.forEach { recordatorio ->
                val etiqueta = if (recordatorio.fechaProxima == hoy) "Vence hoy" else "Programada"
                TarjetaRecordatorio(recordatorio, etiqueta, false) {
                    onSeguimiento(recordatorio.oportunidadId, recordatorio.seguimientoId)
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Próximos eventos", style = MaterialTheme.typography.titleMedium)
            if (proximos.isEmpty()) Text("No hay eventos próximos registrados")
            proximos.forEach { evento ->
                TextButton(onClick = { onDetalle(evento.id) }) {
                    Text("${Fechas.visible(evento.fechaInicio)} · ${evento.nombreOportunidad}")
                }
            }

            // Son avisos comerciales distintos de una tarea con fecha. Se basan en
            // información registrada: no suponen llamadas ni correos enviados realmente.
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Oportunidades sin contacto (${sinContacto.size})", style = MaterialTheme.typography.titleMedium)
            if (sinContacto.isEmpty()) Text("Todas las oportunidades activas tienen contacto asociado")
            sinContacto.forEach { oportunidad ->
                TextButton(onClick = { onDetalle(oportunidad.id) }) { Text(oportunidad.nombreOportunidad) }
            }
            // No tener una tarea pendiente no equivale a tener seguimiento.
            // Esta sección identifica oportunidades sin ninguna gestión comercial registrada.
            Text("Oportunidades sin seguimiento (${estado.sinSeguimiento.size})", style = MaterialTheme.typography.titleMedium)
            if (estado.sinSeguimiento.isEmpty()) Text("Todas las oportunidades activas tienen alguna gestión registrada")
            estado.sinSeguimiento.forEach { oportunidad ->
                TextButton(onClick = { onDetalle(oportunidad.id) }) { Text(oportunidad.nombreOportunidad) }
            }
            Text("Propuestas sin respuesta (${sinRespuesta.size})", style = MaterialTheme.typography.titleMedium)
            if (sinRespuesta.isEmpty()) Text("No hay propuestas registradas en espera de respuesta")
            sinRespuesta.forEach { oportunidad ->
                TextButton(onClick = { onDetalle(oportunidad.id) }) {
                    Text("${oportunidad.nombreOportunidad} · ${oportunidad.estado.etiqueta}")
                }
            }
        }
        Button(onClick = onSalir) { Text("Cerrar sesión") }
    }
}

@Composable
private fun TarjetaRecordatorio(
    tarea: RecordatorioSeguimiento,
    etiqueta: String,
    atrasada: Boolean,
    onResolver: () -> Unit
) {
    // Los pares semánticos de MaterialTheme mantienen contraste en tema claro/oscuro.
    val colores = if (atrasada) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = colores) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelLarge)
            Text(tarea.proximaAccion, style = MaterialTheme.typography.titleMedium)
            Text("Oportunidad: ${tarea.nombreOportunidad}")
            Text("Fecha: ${Fechas.visible(tarea.fechaProxima)}")
            Text("Responsable actual: ${tarea.responsable}")
            TextButton(onClick = onResolver) { Text("Registrar resultado") }
        }
    }
}
