package com.agencia.atenas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/*
 * Recorrido al escribir:
 * Campo recibe estado.valor(clave). Su onValueChange termina llamando a viewModel.cambiar().
 * El ViewModel publica otra instancia de CrearUiState y Compose observa ese cambio.
 * La pantalla no modifica directamente las propiedades del estado ni llama al DAO.
 * Los callbacks onVolver/onGuardado los define Navegacion; aquí solo se invocan.
 */


/**
 * Muestra el formulario que empezamos juntos. Recibe el id para distinguir creación y edición.
 * La pantalla envía eventos al ViewModel; no escribe directamente en la base de datos.
 */

@Composable
fun CrearScreen(id: Int = 0,
    onVolver: () -> Unit = {
    },
    onGuardado: (Int) -> Unit = {
    },
    viewModel: CrearViewModel = viewModel()) {
    // La consulta entrega un StateFlow. collectAsStateWithLifecycle lo convierte en estado observable
    // para Compose y recoge sus valores mientras el ciclo de vida está activo. Así un cambio en Room
    // puede actualizar la pantalla sin consultar manualmente después de cada guardado.
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val organizaciones by viewModel.organizaciones.collectAsStateWithLifecycle()
    val contactos by viewModel.contactos.collectAsStateWithLifecycle()
    // Este efecto se ejecuta al entrar en la composición y cuando cambia id; no en cada recomposición.
    // La consulta se inicia desde aquí para no repetirla simplemente al escribir en un campo.
    LaunchedEffect(id) {
        viewModel.cargar(id)
    }

    // CAMBIO: navegar solo cuando Room confirma el guardado; si falla se conserva el formulario.
    // guardadoId comienza en null. El ViewModel lo completa después de guardar correctamente.
    // El efecto detecta ese cambio y llama a onGuardado con el id, manteniendo la navegación fuera
    // del ViewModel. Si Room falla, el id sigue en null y no se abandona el formulario.
    LaunchedEffect(estado.guardadoId) {
        estado.guardadoId?.let(onGuardado)
    }
    Pagina(if (id == 0) "Nueva oportunidad" else "Editar oportunidad",
        onVolver) {
        if (estado.cargando) {
            CircularProgressIndicator()
        } else if (id > 0 && estado.id == 0) {
            Text(estado.mensaje.ifBlank {
                    "No se pudo abrir la oportunidad"
                },
                color = MaterialTheme.colorScheme.error)
        } else {
            Text("Los campos con * son obligatorios. Los demás pueden quedar sin información.")
            val obligatorios = setOf("nombreOportunidad",
                "tipoOportunidad",
                "fechaInicio",
                "comuna",
                "region",
                "publicoEsperado",
                "fechaDeteccion")

            // CAMBIO: organización y contacto se eligen de catálogos; no se vuelven a escribir aquí.
            camposFormulario.filter {
                it.clave !in setOf("nombreOrganizacion",
                    "tipoOrganizacion",
                    "rubroOrganizacion",
                    "nombreContacto",
                    "cargoContacto",
                    "areaContacto",
                    "correoContacto",
                    "telefonoContacto",
                    "medioContactoPreferente",
                    "responsable",
                    "prioridad")
            }.forEach { campo ->
                if (campo.clave == "nombreOportunidad") Text("Oportunidad",
                    style = MaterialTheme.typography.titleMedium)
                if (campo.clave == "fechaInicio") Text("Evento",
                    style = MaterialTheme.typography.titleMedium)
                Campo(campo.etiqueta + if (campo.clave in obligatorios) " *" else "",
                    estado.valor(campo.clave),
                    estado.errores[campo.clave],
                    !estado.guardando) {
                    viewModel.cambiar(campo.clave,
                        it)
                }
            }
            Text("Responsable: ${estado.responsable}")
            Selector("Prioridad *",
                estado.prioridad,
                listOf("Alta",
                    "Media",
                    "Baja"),
                enabled = !estado.guardando) {
                viewModel.cambiar("prioridad",
                    it)
            }
            Text("Organización: ${estado.nombreOrganizacion.ifBlank { "Sin información" }}")
            Selector("Elegir organización",
                "Seleccionar",
                organizaciones.map {
                    "${it.id}: ${it.nombre}"
                },
                enabled = !estado.guardando) { elegido ->
                organizaciones.firstOrNull {
                    "${it.id}: ${it.nombre}" == elegido
                }
                ?.let(viewModel::elegirOrganizacion)
            }
            Text("Registra organizaciones y contactos desde Inicio antes de abrir este formulario.")
            // Cada organización se identifica por su clave, aunque dos tengan igual nombre.
            // El filtro no usa nombres ni correos: conserva solo contactos con ese mismo ID.
            val disponibles = contactos.filter { it.organizacionId == estado.organizacionId }
            Text("Contacto: ${estado.nombreContacto.ifBlank { "Sin información" }}")
            Selector("Elegir contacto",
                "Seleccionar",
                disponibles.map {
                    "${it.id}: ${it.nombre}"
                },
                enabled = !estado.guardando) { elegido ->
                disponibles.firstOrNull {
                    "${it.id}: ${it.nombre}" == elegido
                }
                ?.let(viewModel::elegirContacto)
            }
            if (estado.mensaje.isNotBlank()) Text(estado.mensaje,
                color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                    viewModel.guardar()
                },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()) {
                Text(if (estado.guardando) "Guardando…" else "Guardar oportunidad")
            }
        }
    }
}
