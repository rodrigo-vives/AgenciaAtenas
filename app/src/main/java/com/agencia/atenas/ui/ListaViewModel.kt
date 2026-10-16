package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.AppDatabase
import com.agencia.atenas.data.GestionOportunidadEntity
import com.agencia.atenas.utils.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListaViewModel(application: Application) : AndroidViewModel(application) {

    // DAO para acceder a la base de datos.
    private val dao = AppDatabase.obtenerBaseDeDatos(application).oportunidadDao()

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

            dao.obtenerTodas().collect { lista ->
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

    // Limpia el filtro por estado (vuelve a mostrar todas).
    fun limpiarFiltroEstado() {
        _uiState.update { estadoActual ->
            estadoActual.copy(filtroEstado = "")
        }
    }

    // Filtra la lista según búsqueda y estado.
    // Se usa en la UI para mostrar solo lo que corresponde.
    fun obtenerGestionesFiltradas(): List<GestionOportunidadEntity> {
        val estado = _uiState.value

        return estado.gestiones.filter { gestion ->
            val coincideBusqueda = estado.textoBusqueda.isBlank() ||
                    gestion.nombreOportunidad.contains(estado.textoBusqueda, ignoreCase = true)

            val coincideEstado = estado.filtroEstado.isBlank() ||
                    gestion.estado.name == estado.filtroEstado

            coincideBusqueda && coincideEstado
        }
    }

    // Cierra la sesión actual.
    fun cerrarSesion() {
        SesionActual.usuario = ""
    }
}