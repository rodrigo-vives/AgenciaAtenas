package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



/** Consulta por contacto histórico o por organización, siempre con alcance por rol. */
class HistorialCatalogoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.obtenerBaseDeDatos(application)
    private val repository = OportunidadRepository(db)
    private val catalogos = CatalogoRepository(db)
    private val _uiState = MutableStateFlow(HistorialCatalogoUiState())
    val uiState = _uiState.asStateFlow()
    private var consulta: Job? = null

    fun cargar(id: Int, esContacto: Boolean) {
        // Volver a cargar cancela la observación anterior para no acumular consultas.
        consulta?.cancel()
        consulta = viewModelScope.launch {
            try {
                val nombre = catalogos.obtenerNombre(id, esContacto)
                val flujo = if (esContacto) repository.historialContacto(id) else repository.historialOrganizacion(id)
                // combine recalcula cuando cambia la lista autorizada O el historial.
                // Así una reasignación retira los datos sin tener que volver a entrar.
                combine(repository.observar(), flujo) { eventos, historial ->
                    // Un Set reúne IDs únicos y facilita comprobar si una oportunidad
                    // contiene gestiones antiguas con el contacto seleccionado.
                    val idsHistoricos = historial.map { item -> item.seguimiento.oportunidadId }.toSet()
                    // El contacto conserva acceso a sus gestiones pasadas, pero el usuario
                    // solo ve oportunidades que le están autorizadas actualmente.
                    val relacionados = eventos.filter { evento ->
                        if (esContacto) {
                            val contactoActual = evento.contactoId == id
                            val tieneGestionesAnteriores = evento.id in idsHistoricos
                            contactoActual || tieneGestionesAnteriores
                        } else {
                            evento.organizacionId == id
                        }
                    }
                    val idsVisibles = eventos.map { it.id }.toSet()
                    val historialVisible = historial.filter { item ->
                        item.seguimiento.oportunidadId in idsVisibles
                    }
                    HistorialCatalogoUiState(
                        nombre = nombre,
                        eventos = relacionados,
                        historial = historialVisible,
                        cargando = false
                    )
                }.collect { _uiState.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = HistorialCatalogoUiState(cargando = false, mensaje = e.message ?: "No se pudo abrir el historial")
            }
        }
    }
}
