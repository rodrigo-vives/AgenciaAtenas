package com.agencia.atenas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agencia.atenas.ui.theme.*

/**
 * El ViewModel indica si hubo error. La UI no analiza palabras para decidir el color.
 * El texto acompaña al color para que el resultado no dependa de distinguir tonos.
 */
@Composable
fun MensajeOperacion(mensaje: String, esError: Boolean) {
    if (mensaje.isBlank()) return
    val fondo = if (esError) MaterialTheme.colorScheme.errorContainer else FondoExito
    val texto = if (esError) MaterialTheme.colorScheme.onErrorContainer else TextoExito
    Surface(color = fondo, contentColor = texto, shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(if (esError) "Revisa la operación" else "Resultado de la operación",
                style = MaterialTheme.typography.labelLarge)
            Text(mensaje)
        }
    }
}
