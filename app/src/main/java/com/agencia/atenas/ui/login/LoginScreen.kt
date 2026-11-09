package com.agencia.atenas.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.R
import com.agencia.atenas.ui.theme.TemaIngreso
import com.agencia.atenas.ui.theme.BotonIngresar
import com.agencia.atenas.ui.theme.relleno

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    TemaIngreso {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
            // Todo el contenido se desplaza para alcanzar el botón con el teclado abierto.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EncabezadoIngreso()
                FormularioIngreso(
                    estado = estado,
                    onUsuario = viewModel::cambiarUsuario,
                    onPassword = viewModel::cambiarPassword,
                    onIngresar = { viewModel.iniciarSesion(onLoginExitoso) }
                )
            }
        }
    }
}

@Composable
private fun EncabezadoIngreso() {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colores.inverseSurface)
            .padding(horizontal = 24.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Reutiliza la ilustración de Atenea que ya forma parte del proyecto.
        Image(
            painter = painterResource(R.drawable.atenea_ingreso),
            contentDescription = null,
            modifier = Modifier.size(108.dp).clip(MaterialTheme.shapes.medium)
        )
        Text(
            text = "Agencia Atenas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = colores.inverseOnSurface
        )
        Text(
            text = "Gestión de oportunidades",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = colores.inverseOnSurface
        )
    }
}

@Composable
private fun FormularioIngreso(
    estado: LoginUiState,
    onUsuario: (String) -> Unit,
    onPassword: (String) -> Unit,
    onIngresar: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .padding(20.dp)
            .widthIn(max = 440.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = estado.usuario,
                onValueChange = onUsuario,
                label = { Text("Usuario") },
                enabled = !estado.cargando,
                singleLine = true,
                isError = estado.errorUsuario,
                supportingText = if (estado.errorUsuario) {
                    { Text("El usuario es obligatorio") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.password,
                onValueChange = onPassword,
                label = { Text("Contraseña") },
                enabled = !estado.cargando,
                singleLine = true,
                isError = estado.errorPassword,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = if (estado.errorPassword) {
                    { Text("La contraseña es obligatoria") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.mensajeError.isNotEmpty()) {
                Text(estado.mensajeError, color = MaterialTheme.colorScheme.error)
            }
            Button(
                colors = BotonIngresar.relleno(),
                onClick = onIngresar,
                enabled = !estado.cargando,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(if (estado.cargando) "Ingresando…" else "Ingresar")
            }
        }
    }
}
