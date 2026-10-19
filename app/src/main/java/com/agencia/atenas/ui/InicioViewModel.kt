package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Inicio y Supervisor tienen una instancia por destino, pero consultan la misma base.
 * combine reúne oportunidades y avisos; StateFlow publica el resumen que observa la UI.
 * La asignación se pide al Repository: el ViewModel no actualiza tablas directamente.
 */
class InicioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.obtenerBaseDeDatos(application)
    private val repository = OportunidadRepository(db)
    val ejecutivos = UsuarioRepository(db).ejecutivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // Los dos flujos ya tienen alcance por rol en Repository. combine vuelve
                // a publicar cuando cambia una oportunidad, una tarea o su resolución.
                combine(repository.observar(), repository.recordatorios(), repository.sinSeguimiento()) { oportunidades, tareas, sinGestion ->
                    // Triple transporta tres resultados de lectura, sin persistir otra tabla.
                    Triple(oportunidades, tareas, sinGestion)
                }.collect { (oportunidades, tareas, sinGestion) ->
                    _uiState.update {
                        it.copy(oportunidades = oportunidades,
                            sinSeguimiento = sinGestion.filter { pendiente -> oportunidades.any { it.id == pendiente.id } },
                            recordatorios = tareas.filter { tarea ->
                                oportunidades.any { it.id == tarea.oportunidadId }
                            }, cargando = false)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(cargando = false, esError = true, mensaje = "No se pudo cargar el resumen comercial") }
            }
        }
    }

    // ocupado evita enviar dos asignaciones mientras una escritura sigue en curso.
    fun asignar(id: Int, usuarioId: Int) {
        if (_uiState.value.ocupado) return
        _uiState.update { it.copy(ocupado = true) }
        viewModelScope.launch {
            try {
                repository.asignar(id, usuarioId)
                _uiState.update { it.copy(ocupado = false, esError = false, mensaje = "Responsable actualizado") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(ocupado = false, esError = true, mensaje = e.message ?: "No se pudo asignar") }
            }
        }
    }
}
