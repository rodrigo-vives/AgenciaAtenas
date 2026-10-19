package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.*
import com.agencia.atenas.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** El borrador solo se limpia al confirmar seguimiento + indicadores + cambio de estado. */
class ResultadoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OportunidadRepository(AppDatabase.obtenerBaseDeDatos(application))
    private val _uiState = MutableStateFlow(ResultadoUiState(fechaRealizacion = Fechas.visible(Fechas.hoy())))
    val uiState = _uiState.asStateFlow()

    // Cada campo avisa su clave y texto. copy conserva los demás datos del borrador.
    // La UI no modifica _uiState: solo puede llamar a estos eventos del ViewModel.
    fun cambiar(clave: String, valor: String) {
        if (_uiState.value.guardando) return
        _uiState.update { estado ->
            val nuevo = when (clave) {
                "fechaRealizacion" -> estado.copy(fechaRealizacion = valor)
                "asistentes" -> estado.copy(asistentes = valor)
                "contactosGenerados" -> estado.copy(contactosGenerados = valor)
                "interesados" -> estado.copy(interesados = valor)
                "inscripciones" -> estado.copy(inscripciones = valor)
                "evaluacion" -> estado.copy(evaluacion = valor)
                "observaciones" -> estado.copy(observaciones = valor)
                "aprendizajes" -> estado.copy(aprendizajes = valor)
                else -> estado
            }
            // Se retira el error del campo editado; al guardar se validará todo de nuevo.
            nuevo.copy(errores = estado.errores - clave, mensaje = "", esError = false)
        }
    }

    fun guardar(id: Int) {
        // Capturar una sola instantánea asegura que se guarden los valores validados.
        // Mientras guardando=true, los campos y el botón quedan deshabilitados.
        val borrador = _uiState.value
        if (borrador.guardando) return
        // La función externa calcula reglas; el ViewModel comunica sus errores a la UI.
        val errores = validarResultado(borrador)
        _uiState.update { it.copy(errores = errores) }
        // Si hay errores, terminar aquí mantiene todos los textos ingresados.
        if (errores.isNotEmpty()) return
        _uiState.update { it.copy(guardando = true, mensaje = "") }
        // launch permite esperar a Room sin bloquear la pantalla. La transacción
        // completa está en Repository; aquí solo se publica éxito o se conserva el borrador.
        viewModelScope.launch {
            try {
                repository.guardarResultado(id, borrador)
                // Solo tras el éxito se crea un formulario nuevo con la fecha de hoy.
                _uiState.value = ResultadoUiState(fechaRealizacion = Fechas.visible(Fechas.hoy()),
                    mensaje = "Resultado guardado. Puedes consultarlo en el detalle y el historial.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, esError = true, mensaje = e.message ?: "No se pudo guardar el resultado") }
            }
        }
    }
}
