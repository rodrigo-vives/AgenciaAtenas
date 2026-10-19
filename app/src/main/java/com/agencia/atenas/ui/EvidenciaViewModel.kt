package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.EvidenciaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



/**
 * La pantalla abre la cámara; este ViewModel coordina archivos y estado observable.
 * SavedStateHandle conserva la ruta pendiente al rotar o si Android recrea el proceso
 * mientras está abierta la cámara. El callback puede así encontrar el archivo original.
 */
class EvidenciaViewModel(application: Application, private val guardado: SavedStateHandle) : AndroidViewModel(application) {
    private val repository = EvidenciaRepository(application)
    private val _estado = MutableStateFlow(EvidenciaUiState())
    val estado = _estado.asStateFlow()

    // Releer al volver al detalle recupera archivos guardados en sesiones anteriores.
    fun cargar(id: Int) = ejecutar {
        _estado.update { it.copy(fotos = repository.listar(id)) }
    }

    fun preparar(id: Int, abrirCamara: (String) -> Unit) = ejecutar {
        // No se inicia una segunda captura si la primera todavía espera respuesta.
        check(guardado.get<String>("rutaCaptura") == null) { "Ya hay una captura pendiente" }
        val ruta = repository.preparar(id)
        guardado["rutaCaptura"] = ruta
        guardado["oportunidadCaptura"] = id
        abrirCamara(ruta)
    }

    fun terminar(capturada: Boolean, mensajeError: String? = null) = ejecutar {
        val ruta = guardado.get<String>("rutaCaptura") ?: return@ejecutar
        val id = guardado.get<Int>("oportunidadCaptura") ?: return@ejecutar
        try {
            repository.finalizar(id, ruta, capturada)
            _estado.update { it.copy(fotos = repository.listar(id),
                esError = mensajeError != null,
                mensaje = mensajeError ?: if (capturada) "Fotografía guardada" else "Captura cancelada; no se añadió una foto") }
        } finally {
            guardado.remove<String>("rutaCaptura")
            guardado.remove<Int>("oportunidadCaptura")
        }
    }

    // Los errores se publican en la pantalla. CancellationException se propaga porque
    // cancelar una corrutina al abandonar el destino no representa un error del usuario.
    private fun ejecutar(operacion: suspend () -> Unit) {
        viewModelScope.launch {
            _estado.update { it.copy(ocupado = true) }
            try {
                operacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(esError = true, mensaje = e.message ?: "No se pudo procesar la fotografía") }
            } finally {
                // Se libera el indicador tanto si hubo éxito como si se informó un error.
                _estado.update { it.copy(ocupado = false) }
            }
        }
    }
}
