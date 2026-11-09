package com.agencia.atenas.ui.perfil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.agencia.atenas.data.base.AppDatabase
import com.agencia.atenas.data.oportunidades.OportunidadRepository
import com.agencia.atenas.data.usuarios.UsuarioRepository

/** Coordina la cuenta y el resumen del perfil mediante Repository; la pantalla no muestra credenciales. */


class PerfilViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.obtenerBaseDeDatos(application)
    private val usuarios = UsuarioRepository(db)
    private val oportunidades = OportunidadRepository(db)
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // Se observa la cuenta real: un cambio en su rol o estado se refleja aquí.
                combine(usuarios.observarCuentaActual(), oportunidades.observar()) { usuario, lista ->
                    PerfilUiState(usuario = usuario, oportunidadesVisibles = lista.size, cargando = false)
                }.collect { _uiState.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensaje = e.message ?: "No se pudo consultar el perfil") }
            }
        }
    }
}
