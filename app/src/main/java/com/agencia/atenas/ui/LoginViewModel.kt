package com.agencia.atenas.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.AppDatabase
import com.agencia.atenas.data.UsuarioRepository
import com.agencia.atenas.utils.SesionActual
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * CAMBIO: el formulario conserva sus campos, pero la identidad sale de Room.
 * AndroidViewModel proporciona Application para obtener la base como en las clases.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(AppDatabase.obtenerBaseDeDatos(application))
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiarUsuario(valor: String) {
        if (!_uiState.value.cargando) {
            _uiState.update { it.copy(usuario = valor, errorUsuario = false, mensajeError = "") }
        }
    }

    fun cambiarPassword(valor: String) {
        if (!_uiState.value.cargando) {
            _uiState.update { it.copy(password = valor, errorPassword = false, mensajeError = "") }
        }
    }

    fun iniciarSesion(onLoginExitoso: () -> Unit) {
        val estado = _uiState.value
        if (estado.cargando) return
        val errorUsuario = estado.usuario.isBlank()
        val errorPassword = estado.password.isBlank()
        _uiState.update { it.copy(errorUsuario = errorUsuario, errorPassword = errorPassword) }
        if (errorUsuario || errorPassword) return

        // Room es suspendido: no se bloquea la interfaz esperando la consulta.
        _uiState.update { it.copy(cargando = true, mensajeError = "") }
        viewModelScope.launch {
            try {
                val cuenta = repository.autenticar(estado.usuario, estado.password)
                if (cuenta == null) {
                    _uiState.update { it.copy(cargando = false, mensajeError = "Usuario o contraseña incorrectos") }
                } else {
                    // Se guarda identidad/rol, nunca passwordHash ni contraseña, en la sesión.
                    SesionActual.iniciar(cuenta)
                    _uiState.value = LoginUiState()
                    onLoginExitoso()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Logcat conserva la causa técnica para depurar; no se registran los campos
                // de usuario/contraseña. En pantalla se mantiene un mensaje breve y comprensible.
                Log.e("LoginViewModel", "No se pudo consultar la base local de usuarios", e)
                _uiState.update { it.copy(cargando = false, mensajeError = "No se pudo abrir la base de usuarios") }
            }
        }
    }
}
