package com.agencia.atenas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.agencia.atenas.R
import androidx.compose.ui.unit.dp

/*
 * Estos Composables reciben datos y funciones por parámetros, por eso sirven en varias pantallas.
 * Campo no valida: solo dibuja el error recibido. Pagina no sabe a qué ruta volver: llama onVolver.
 * Selector mantiene abierto/cerrado el menú localmente porque es un detalle visual; el valor
 * seleccionado lo conserva el ViewModel. Así se distingue estado visual de datos del formulario.
 */


/**
 * Componentes compartidos que antes estaban dentro de CrearScreen.
 * Aquí solo hay presentación. Las reglas de negocio permanecen en el ViewModel/Repository.
 */

// Estructura común para pantallas con un formulario o contenido desplazable.
@Composable
fun Pagina(
    titulo: String,
    onVolver: () -> Unit,
    mostrarVolver: Boolean = true,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Respeta las barras del sistema.
                .imePadding() // Evita que el teclado cubra el final del contenido.
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Inicio no tiene pantalla anterior: su acción es Cerrar sesión, no Volver.
            if (mostrarVolver) TextButton(onClick = onVolver) { Text("← Volver") }
            Text(titulo, style = MaterialTheme.typography.headlineSmall)
            contenido() // Inserta aquí lo que escribió la pantalla que utiliza Pagina.
        }
    }
}

// Un campo siempre muestra el valor recibido y comunica la edición mediante onCambio.
// error = null significa que no hay mensaje de validación que mostrar.
@Composable
fun Campo(
    etiqueta: String,
    valor: String,
    error: String? = null,
    enabled: Boolean = true,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        enabled = enabled,
        label = { Text(etiqueta) },
        isError = error != null,
        // El ícono y el mensaje acompañan al color: el error no depende de distinguir rojo.
        trailingIcon = if (error != null) {
            { Icon(painterResource(R.drawable.ic_error_formulario), contentDescription = "Campo con error") }
        } else null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// El menú solo necesita recordar si está abierto. La selección la conserva el ViewModel.
@Composable
fun Selector(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    enabled: Boolean = true,
    onElegir: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { abierto = true },
            enabled = enabled && opciones.isNotEmpty()
        ) {
            Text("$etiqueta: $valor")
        }
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        abierto = false
                        onElegir(opcion)
                    }
                )
            }
        }
    }
}
