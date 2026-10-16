package com.agencia.atenas.ui

import androidx.lifecycle.ViewModel
import com.agencia.atenas.utils.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    // Estado interno (solo el ViewModel lo modifica)
    private val _uiState = MutableStateFlow(LoginUiState())

    // Estado expuesto a la UI (solo lectura)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Actualiza el usuario y limpia errores previos
    fun cambiarUsuario(nuevoUsuario: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                usuario = nuevoUsuario,
                errorUsuario = false,
                mensajeError = ""
            )
        }
    }

    // Actualiza la contraseña y limpia errores previos
    fun cambiarPassword(nuevaPassword: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                password = nuevaPassword,
                errorPassword = false,
                mensajeError = ""
            )
        }
    }

    // Valida campos y verifica credenciales hardcodeadas
    fun iniciarSesion(onLoginExitoso: () -> Unit) {
        val estado = _uiState.value

        // Verifica si los campos están vacíos
        val errorUsuario = estado.usuario.isBlank()
        val errorPassword = estado.password.isBlank()

        // Actualiza el estado con los errores detectados
        _uiState.update { estadoActual ->
            estadoActual.copy(
                errorUsuario = errorUsuario,
                errorPassword = errorPassword
            )
        }

        // Si hay campos vacíos, no continúa
        if (errorUsuario || errorPassword) {
            return
        }

        // Credenciales correctas: guarda sesión y avisa a la UI
        if (estado.usuario == "admin" && estado.password == "1234") {
            SesionActual.usuario = estado.usuario
            _uiState.value = LoginUiState()
            onLoginExitoso()
        } else {
            // Credenciales incorrectas: muestra mensaje general
            _uiState.update { estadoActual ->
                estadoActual.copy(
                    mensajeError = "Usuario o contraseña incorrectos"
                )
            }
        }
    }
}