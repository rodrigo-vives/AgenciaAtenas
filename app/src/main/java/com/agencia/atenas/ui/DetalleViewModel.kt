package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.*
import com.agencia.atenas.utils.Fechas
import com.agencia.atenas.utils.SesionActual
import com.agencia.atenas.utils.validarSeguimiento
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Detalle y Seguimiento tienen instancias propias de esta clase, una por destino.
 * Una gestión libre registra lo ocurrido. Al resolver una próxima acción se precarga
 * su texto y se exige un resultado; Repository enlaza ambos registros en transacción.
 */
class DetalleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OportunidadRepository(AppDatabase.obtenerBaseDeDatos(application))
    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState = _uiState.asStateFlow()
    private var consultaHistorial: Job? = null
    private var solicitudInicialProcesada = false

    fun cargar(id: Int, pendienteInicialId: Int = 0) {
        // Se cancela el collect anterior: volver a primer plano no duplica observadores.
        consultaHistorial?.cancel()
        consultaHistorial = viewModelScope.launch {
            try {
                val oportunidad = repository.obtener(id)
                _uiState.update { it.copy(item = oportunidad, cargando = false) }
                combine(repository.seguimientos(id), repository.resultados(id)) { historial, resultados ->
                    historial to resultados
                }.collect { (historial, resultados) ->
                    _uiState.update { it.copy(historial = historial, resultados = resultados) }
                    // Un acceso desde Inicio puede pedir resolver una tarea concreta.
                    // Se procesa una vez para no pisar el resultado mientras se escribe.
                    if (!solicitudInicialProcesada && pendienteInicialId > 0) {
                        solicitudInicialProcesada = true
                        prepararResolucion(pendienteInicialId)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(item = null, historial = emptyList(), cargando = false,
                        esError = true, mensaje = e.message ?: "No se pudo cargar la oportunidad")
                }
            }
        }
    }

    fun cambiar(clave: String, valor: String) {
        if (_uiState.value.ocupado) return
        // Una resolución usa la acción que estaba programada. El usuario explica el
        // resultado; cambiar el texto de la tarea original haría ambiguo el historial.
        if (clave == "accion" && _uiState.value.pendienteEnResolucionId != null) return
        _uiState.update { estado ->
            val nuevo = when (clave) {
                "accion" -> estado.copy(accion = valor)
                "resultado" -> estado.copy(resultado = valor)
                "proximaAccion" -> estado.copy(proximaAccion = valor)
                "fechaProxima" -> estado.copy(fechaProxima = valor)
                "observaciones" -> estado.copy(observaciones = valor)
                else -> estado
            }
            nuevo.copy(errores = estado.errores - clave, mensaje = "")
        }
    }

    // Busca la tarea en el historial visible. No reemplaza un borrador con datos escritos:
    // pide guardarlo o limpiarlo primero para evitar perder una gestión por tocar otro botón.
    fun prepararResolucion(pendienteId: Int) {
        val estado = _uiState.value
        if (estado.ocupado) return
        if (estado.accion.isNotBlank() || estado.resultado.isNotBlank() ||
            estado.proximaAccion.isNotBlank() || estado.fechaProxima.isNotBlank() ||
            estado.observaciones.isNotBlank()) {
            _uiState.update { it.copy(esError = true, mensaje = "Guarda o limpia el formulario antes de elegir otra acción") }
            return
        }
        val fila = estado.historial.firstOrNull { it.seguimiento.id == pendienteId }?.seguimiento
        if (fila == null || fila.resueltaPorId != null ||
            fila.proximaAccion.isBlank() || fila.fechaProxima.isBlank()) {
            _uiState.update { it.copy(esError = true, mensaje = "La acción ya se completó o no está disponible") }
            return
        }
        _uiState.update {
            it.copy(pendienteEnResolucionId = fila.id,
                fechaAccionProgramada = Fechas.visible(fila.fechaProxima),
                accion = fila.proximaAccion, errores = emptyMap(), mensaje = "")
        }
    }

    fun limpiarFormulario() {
        if (_uiState.value.ocupado) return
        _uiState.update {
            it.copy(accion = "", resultado = "", proximaAccion = "", fechaProxima = "",
                observaciones = "", errores = emptyMap(), pendienteEnResolucionId = null,
                fechaAccionProgramada = "", mensaje = "")
        }
    }

    fun cambiarEstado(estado: EstadoOportunidad) {
        val oportunidad = _uiState.value.item ?: return
        operar {
            repository.cambiarEstado(oportunidad.id, estado)
            val actualizado = repository.obtener(oportunidad.id)
            _uiState.update { it.copy(item = actualizado) }
        }
    }

    // Las operaciones suspendidas comparten carga/éxito/error. El bloque recibido describe
    // qué guardar; este método solo coordina la corrutina y la información de pantalla.
    private fun operar(operacion: suspend () -> Unit) {
        if (_uiState.value.ocupado) return
        _uiState.update { it.copy(ocupado = true, esError = false, mensaje = "") }
        viewModelScope.launch {
            try {
                operacion()
                _uiState.update { it.copy(ocupado = false, esError = false, mensaje = "Cambios guardados correctamente") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(ocupado = false, esError = true, mensaje = e.message ?: "No se pudo guardar") }
            }
        }
    }

    fun registrar() {
        val estado = _uiState.value
        val oportunidad = estado.item ?: return
        if (estado.ocupado) return
        val errores = validarSeguimiento(estado.accion, estado.resultado,
            estado.proximaAccion, estado.fechaProxima, estado.observaciones)
        _uiState.update { it.copy(errores = errores) }
        if (errores.isNotEmpty()) return

        operar {
            val registro = SeguimientoEntity(
                oportunidadId = oportunidad.id,
                responsableId = checkNotNull(SesionActual.usuarioId) { "Inicia sesión nuevamente" },
                fechaHora = Fechas.ahora(),
                accion = estado.accion.trim(),
                resultado = estado.resultado.trim(),
                proximaAccion = estado.proximaAccion.trim(),
                fechaProxima = Fechas.aIso(estado.fechaProxima).orEmpty(),
                observaciones = estado.observaciones.trim()
            )
            repository.registrar(registro, estado.pendienteEnResolucionId)
            // Solo limpiar tras confirmar ambas escrituras. Un fallo conserva el borrador.
            _uiState.update {
                it.copy(accion = "", resultado = "", proximaAccion = "", fechaProxima = "",
                    observaciones = "", pendienteEnResolucionId = null, fechaAccionProgramada = "")
            }
        }
    }
}
