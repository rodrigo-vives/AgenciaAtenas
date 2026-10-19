package com.agencia.atenas.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.utils.Fechas

/**
 * Reutiliza la misma presentación para contacto u organización. El booleano decide
 * qué consulta solicita al ViewModel; esta pantalla no filtra permisos ni consulta SQL.
 */
@Composable
fun HistorialCatalogoScreen(id: Int, esContacto: Boolean, onVolver: () -> Unit,
    onDetalle: (Int) -> Unit, viewModel: HistorialCatalogoViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(id, esContacto) { viewModel.cargar(id, esContacto) }
    Pagina(if (esContacto) "Historial del contacto" else "Eventos e historial", onVolver) {
        if (estado.cargando) CircularProgressIndicator()
        Text(estado.nombre, style = MaterialTheme.typography.titleLarge)
        Text("Solo se muestra la información de oportunidades que puedes consultar.")
        if (estado.mensaje.isNotBlank()) MensajeOperacion(estado.mensaje, true)
        Text("Oportunidades relacionadas", style = MaterialTheme.typography.titleMedium)
        if (!estado.cargando && estado.eventos.isEmpty()) Text("No hay oportunidades relacionadas visibles")
        estado.eventos.forEach { evento ->
            TextButton(onClick = { onDetalle(evento.id) }) {
                Text("${evento.nombreOportunidad} · ${Fechas.visible(evento.fechaInicio)}")
            }
        }
        Text("Gestiones realizadas", style = MaterialTheme.typography.titleMedium)
        if (!estado.cargando && estado.historial.isEmpty()) Text("No hay gestiones registradas visibles")
        estado.historial.forEach { item ->
            OutlinedCard {
                Dato("Oportunidad", item.nombreOportunidad)
                Dato("Fecha y autor", "${item.seguimiento.fechaHora} · ${item.responsable}")
                Dato("Acción", item.seguimiento.accion)
                Dato("Resultado", item.seguimiento.resultado)
                Dato("Observaciones", item.seguimiento.observaciones)
            }
        }
    }
}
