package com.agencia.atenas.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.*
import com.agencia.atenas.utils.*

/**
 * Lista global y selección del ejecutivo responsable. No permite entrar con rol ejecutivo.
 * La asignación pasa al ViewModel y Repository vuelve a comprobar el permiso.
 */

@Composable
fun SupervisorScreen(onVolver: () -> Unit,
    onDetalle: (Int) -> Unit,
    viewModel: InicioViewModel = viewModel()) {
    // La consulta entrega un StateFlow. collectAsStateWithLifecycle lo convierte en estado observable
    // para Compose y recoge sus valores mientras el ciclo de vida está activo. Así un cambio en Room
    // puede actualizar la pantalla sin consultar manualmente después de cada guardado.
    val estadoFormulario by viewModel.uiState.collectAsStateWithLifecycle()
    val ejecutivos by viewModel.ejecutivos.collectAsStateWithLifecycle()
    Pagina("Panel de supervisor",
        onVolver) {
        if (!SesionActual.esSupervisor) Text("Acceso no autorizado")
        else {
            if (estadoFormulario.cargando) CircularProgressIndicator()
            if (estadoFormulario.oportunidades.isEmpty() && !estadoFormulario.cargando) Text("No hay oportunidades para asignar")
            estadoFormulario.oportunidades.forEach { item ->
                Card {
                    Text(item.nombreOportunidad)
                    Text("Responsable: ${item.responsable}")
                    Selector("Asignar / reasignar",
                        item.responsable,
                        ejecutivos.map { "${it.id}: ${it.usuario}" },
                        !estadoFormulario.ocupado) {
                        viewModel.asignar(item.id,
                            it.substringBefore(':').toInt())
                    }
                    TextButton(onClick = {
                            onDetalle(item.id)
                    }) {
                        Text("Ver detalle")
                    }
                }
            }
            MensajeOperacion(estadoFormulario.mensaje, estadoFormulario.esError)
        }
    }
}
