package com.agencia.atenas.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.utils.Fechas

/**
 * Permite registrar una gestión libre o completar una acción programada con resultado.
 * El historial muestra el vínculo de resolución: no se borra la tarea ni su fecha original.
 */
@Composable
fun SeguimientoScreen(
    id: Int,
    onVolver: () -> Unit,
    pendienteInicialId: Int = 0,
    viewModel: DetalleViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar(id, pendienteInicialId)
    }

    Pagina("Seguimiento e historial", onVolver) {
        if (estado.cargando) CircularProgressIndicator()
        estado.item?.let { oportunidad ->
            Text(oportunidad.nombreOportunidad, style = MaterialTheme.typography.titleMedium)
            val resolviendo = estado.pendienteEnResolucionId != null
            Text(if (resolviendo) "Completar acción programada" else "Registrar una gestión")
            if (resolviendo) {
                Text("Fecha programada: ${estado.fechaAccionProgramada}")
                Text("Escribe el resultado. Al guardar, la acción dejará de estar pendiente.")
            }
            Campo("Acción realizada *", estado.accion, estado.errores["accion"],
                !estado.ocupado && !resolviendo) { viewModel.cambiar("accion", it) }
            Campo("Resultado *", estado.resultado, estado.errores["resultado"],
                !estado.ocupado) { viewModel.cambiar("resultado", it) }
            Campo("Próxima acción (opcional)", estado.proximaAccion, estado.errores["proximaAccion"],
                !estado.ocupado) { viewModel.cambiar("proximaAccion", it) }
            Campo("Fecha próxima DD/MM/AAAA", estado.fechaProxima, estado.errores["fechaProxima"],
                !estado.ocupado) { viewModel.cambiar("fechaProxima", it) }
            Campo("Observaciones", estado.observaciones, estado.errores["observaciones"],
                !estado.ocupado) { viewModel.cambiar("observaciones", it) }
            Button(onClick = { viewModel.registrar() }, enabled = !estado.ocupado) {
                Text(if (estado.ocupado) "Guardando…" else if (resolviendo)
                    "Completar acción y guardar resultado" else "Registrar gestión")
            }
            TextButton(onClick = { viewModel.limpiarFormulario() }, enabled = !estado.ocupado) {
                Text(if (resolviendo) "Cancelar y limpiar formulario" else "Limpiar formulario")
            }
            // El resultado se muestra cerca del botón, no al final de un historial largo.
            MensajeOperacion(estado.mensaje, estado.esError)

            Text("Historial", style = MaterialTheme.typography.titleMedium)
            if (estado.historial.isEmpty()) Text("No hay gestiones registradas")
            estado.historial.forEach { gestion ->
                Card {
                    Text("${gestion.fechaHora} · ${gestion.responsable}")
                    Dato("Acción", gestion.accion)
                    Dato("Resultado", gestion.resultado)
                    Dato("Observaciones", gestion.observaciones)
                    if (gestion.proximaAccion.isNotBlank()) {
                        Dato("Acción programada", gestion.proximaAccion)
                        Dato("Fecha programada", Fechas.visible(gestion.fechaProxima))
                        if (gestion.seguimiento.resueltaPorId != null) {
                            Dato("Completada el", gestion.fechaResolucion.orEmpty())
                            Dato("Completada por", gestion.autorResolucion.orEmpty())
                            Dato("Resultado registrado", gestion.resultadoResolucion.orEmpty())
                        } else {
                            Text("Acción todavía sin completar")
                            if (!resolviendo) {
                                TextButton(onClick = { viewModel.prepararResolucion(gestion.seguimiento.id) },
                                    enabled = !estado.ocupado) {
                                    Text("Registrar resultado de esta acción")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (estado.item == null && estado.mensaje.isNotBlank()) Text(estado.mensaje)
    }
}
