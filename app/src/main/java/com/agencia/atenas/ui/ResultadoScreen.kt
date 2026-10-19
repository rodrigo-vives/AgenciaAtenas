package com.agencia.atenas.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Indicadores ficticios opcionales del caso, sin obligar a inventar cantidades. */
@Composable
fun ResultadoScreen(id: Int, onVolver: () -> Unit, viewModel: ResultadoViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    Pagina("Resultado de la activación", onVolver) {
        Text("La participación debe estar confirmada. Al registrar su resultado pasa a realizada.")
        Text("Fecha y evaluación son obligatorias. Deja vacíos los indicadores desconocidos.")
        Campo(
            etiqueta = "Fecha de realización * DD/MM/AAAA",
            valor = estado.fechaRealizacion,
            error = estado.errores["fechaRealizacion"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("fechaRealizacion", it) }
        Campo(
            etiqueta = "Asistentes",
            valor = estado.asistentes,
            error = estado.errores["asistentes"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("asistentes", it) }
        Campo(
            etiqueta = "Contactos generados",
            valor = estado.contactosGenerados,
            error = estado.errores["contactosGenerados"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("contactosGenerados", it) }
        Campo(
            etiqueta = "Personas interesadas",
            valor = estado.interesados,
            error = estado.errores["interesados"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("interesados", it) }
        Campo(
            etiqueta = "Inscripciones",
            valor = estado.inscripciones,
            error = estado.errores["inscripciones"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("inscripciones", it) }
        Campo(
            etiqueta = "Evaluación / resultado *",
            valor = estado.evaluacion,
            error = estado.errores["evaluacion"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("evaluacion", it) }
        Campo(
            etiqueta = "Observaciones",
            valor = estado.observaciones,
            error = estado.errores["observaciones"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("observaciones", it) }
        Campo(
            etiqueta = "Aprendizajes",
            valor = estado.aprendizajes,
            error = estado.errores["aprendizajes"],
            enabled = !estado.guardando
        ) { viewModel.cambiar("aprendizajes", it) }
        Button(onClick = { viewModel.guardar(id) }, enabled = !estado.guardando) {
            Text(if (estado.guardando) "Guardando…" else "Guardar resultado")
        }
        MensajeOperacion(estado.mensaje, estado.esError)
    }
}
