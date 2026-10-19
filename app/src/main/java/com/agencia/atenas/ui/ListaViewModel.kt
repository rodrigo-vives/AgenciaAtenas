package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.AppDatabase
import com.agencia.atenas.utils.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ListaViewModel(application: Application) : AndroidViewModel(application) {

    // CAMBIO: Repository aplica el alcance por usuario también al listado.
    private val repository = com.agencia.atenas.data.OportunidadRepository(AppDatabase.obtenerBaseDeDatos(application))

    // Estado interno (solo el ViewModel lo modifica).
    private val _uiState = MutableStateFlow(ListaUiState())

    // Estado expuesto a la UI (solo lectura).
    val uiState: StateFlow<ListaUiState> = _uiState.asStateFlow()

    init {
        // Al crear el ViewModel, carga todas las gestiones.
        cargarGestiones()
    }

    // Lee todas las gestiones de Room y las guarda en el estado.
    private fun cargarGestiones() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }

            repository.observar().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(cargando = false, mensajeError = "No se pudo cargar el listado") }
            }.collect { lista ->
                _uiState.update {
                    it.copy(
                        gestiones = lista,
                        cargando = false
                    )
                }
            }
        }
    }

    // Actualiza el texto de búsqueda y filtra en memoria.
    fun cambiarBusqueda(texto: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(textoBusqueda = texto)
        }
    }

    // Actualiza el filtro por estado.
    fun cambiarFiltroEstado(estado: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(filtroEstado = estado)
        }
    }

    // Cada evento publica una copia; la lista se deriva del nuevo estado en Compose.
    // Los filtros no se guardan como columnas: son preferencias de consulta de esta pantalla.
    fun cambiarPrioridad(valor: String) {
        _uiState.update { it.copy(filtroPrioridad = valor) }
    }

    fun cambiarTipo(valor: String) {
        _uiState.update { it.copy(filtroTipo = valor) }
    }

    fun cambiarUbicacion(valor: String) {
        _uiState.update { it.copy(filtroUbicacion = valor) }
    }

    fun cambiarOrganizacion(id: Int?) {
        _uiState.update { it.copy(filtroOrganizacionId = id) }
    }

    fun cambiarResponsable(id: Int?) {
        if (SesionActual.esSupervisor) _uiState.update { it.copy(filtroResponsableId = id) }
    }
    fun cambiarDesde(valor: String) {
        _uiState.update { it.copy(fechaDesde = valor) }
    }

    fun cambiarHasta(valor: String) {
        _uiState.update { it.copy(fechaHasta = valor) }
    }

    fun limpiarFiltros() {
        _uiState.update { it.sinFiltros() }
    }


}
