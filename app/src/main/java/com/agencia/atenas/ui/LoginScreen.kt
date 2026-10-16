package com.agencia.atenas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {

    // Observa el StateFlow del ViewModel y lo convierte en State de Compose.
    // Cada cambio en el estado provoca una recomposición de la pantalla.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            // fillMaxSize: ocupa toda la pantalla.
            // padding(innerPadding): respeta las barras del sistema (status bar, nav bar).
            // padding(24.dp): margen interno para que el contenido no toque los bordes.
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            // Centra los elementos vertical y horizontalmente.
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Título de la pantalla, usando la tipografía del tema.
            Text(
                text = "Agencia Atenas",
                style = MaterialTheme.typography.headlineMedium
            )

            // Campo de usuario.
            // value: lo que se muestra (viene del estado del ViewModel).
            // onValueChange: actualiza el ViewModel cuando el usuario escribe.
            // isError: pinta el campo en rojo si hay error.
            // supportingText: mensaje debajo del campo, visible solo si hay error.
            OutlinedTextField(
                value = uiState.usuario,
                onValueChange = { viewModel.cambiarUsuario(it) },
                label = { Text("Usuario") },
                isError = uiState.errorUsuario,
                supportingText = {
                    if (uiState.errorUsuario) {
                        Text("El usuario es obligatorio")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )

            // Campo de contraseña.
            // PasswordVisualTransformation: oculta el texto (muestra puntos).
            // El resto es igual al campo de usuario.
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.cambiarPassword(it) },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.errorPassword,
                supportingText = {
                    if (uiState.errorPassword) {
                        Text("La contraseña es obligatoria")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // Mensaje de error general (credenciales incorrectas).
            // Solo se muestra si el ViewModel lo estableció.
            // Usa el color de error del tema para mantener consistencia visual.
            if (uiState.mensajeError.isNotEmpty()) {
                Text(
                    text = uiState.mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Botón de ingreso.
            // Al pulsar, llama a iniciarSesion del ViewModel y le pasa el callback.
            // Si el login es correcto, el callback navega a la siguiente pantalla.
            Button(
                onClick = { viewModel.iniciarSesion(onLoginExitoso) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Ingresar")
            }
        }
    }
}