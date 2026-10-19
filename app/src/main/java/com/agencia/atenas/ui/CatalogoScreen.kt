package com.agencia.atenas.ui

import androidx.compose.material3.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Formulario y listado de organizaciones o contactos. esContacto decide cuál de los dos
 * se muestra. Se reutiliza el diseño; las rutas de navegación siguen siendo diferentes.
 */

// CAMBIO: dos destinos funcionales reutilizan presentación para mantener el proyecto sencillo.
@Composable
fun CatalogoScreen(esContacto: Boolean,
    onVolver: () -> Unit,
    onHistorial: (Int) -> Unit = {},
    viewModel: CatalogoViewModel = viewModel()) {
    // La consulta entrega un StateFlow. collectAsStateWithLifecycle lo convierte en estado observable
    // para Compose y recoge sus valores mientras el ciclo de vida está activo. Así un cambio en Room
    // puede actualizar la pantalla sin consultar manualmente después de cada guardado.
    val estadoFormulario by viewModel.uiState.collectAsStateWithLifecycle()
    val organizaciones by viewModel.organizaciones.collectAsStateWithLifecycle()
    val contactos by viewModel.contactos.collectAsStateWithLifecycle()
    Pagina(if (esContacto) "Contactos" else "Organizaciones",
        onVolver) {
        Text("Usa únicamente información ficticia. * Obligatorio.")
        Campo("Nombre *",
            estadoFormulario.nombre,
            estadoFormulario.errores["nombre"],
            !estadoFormulario.guardando) {
            viewModel.cambiar("nombre",
                it)
        }
        if (esContacto) {
            Selector("Organización *",
                organizaciones.firstOrNull {
                    it.id == estadoFormulario.organizacionId
                }
                ?.nombre ?: "Seleccionar",
                organizaciones.map {
                    "${it.id}: ${it.nombre}"
                },
                !estadoFormulario.guardando) {
                viewModel.cambiar("organizacion",
                    it.substringBefore(':'))
            }
            estadoFormulario.errores["organizacion"]?.let {
                Text(it,
                    color = MaterialTheme.colorScheme.error)
            }
            Campo("Cargo",
                estadoFormulario.cargo,
                estadoFormulario.errores["cargo"],
                !estadoFormulario.guardando) {
                viewModel.cambiar("cargo",
                    it)
            }
            Campo("Área",
                estadoFormulario.area,
                estadoFormulario.errores["area"],
                !estadoFormulario.guardando) {
                viewModel.cambiar("area",
                    it)
            }
            Campo("Correo ficticio",
                estadoFormulario.correo,
                estadoFormulario.errores["correo"],
                !estadoFormulario.guardando) {
                viewModel.cambiar("correo",
                    it)
            }
            Campo("Teléfono ficticio",
                estadoFormulario.telefono,
                estadoFormulario.errores["telefono"],
                !estadoFormulario.guardando) {
                viewModel.cambiar("telefono",
                    it)
            }
            Campo("Medio preferente",
                estadoFormulario.medio,
                estadoFormulario.errores["medio"],
                !estadoFormulario.guardando) {
                viewModel.cambiar("medio",
                    it)
            }
        } else {
            Campo("Tipo",
                estadoFormulario.tipo,
                estadoFormulario.errores["tipo"],
                !estadoFormulario.guardando) {
                viewModel.cambiar("tipo",
                    it)
            }
            Campo("Rubro",
                estadoFormulario.rubro,
                estadoFormulario.errores["rubro"],
                !estadoFormulario.guardando) {
                viewModel.cambiar("rubro",
                    it)
            }
            // Estos campos pertenecen a la organización, no se duplican en cada evento.
            Campo(
                etiqueta = "Comuna",
                valor = estadoFormulario.comuna,
                error = estadoFormulario.errores["comuna"],
                enabled = !estadoFormulario.guardando
            ) { viewModel.cambiar("comuna", it) }
            Campo(
                etiqueta = "Región",
                valor = estadoFormulario.region,
                error = estadoFormulario.errores["region"],
                enabled = !estadoFormulario.guardando
            ) { viewModel.cambiar("region", it) }
            Campo(
                etiqueta = "Sitio web ficticio https://",
                valor = estadoFormulario.sitioWeb,
                error = estadoFormulario.errores["sitioWeb"],
                enabled = !estadoFormulario.guardando
            ) { viewModel.cambiar("sitioWeb", it) }
            Campo(
                etiqueta = "Público asociado",
                valor = estadoFormulario.publicoAsociado,
                error = estadoFormulario.errores["publicoAsociado"],
                enabled = !estadoFormulario.guardando
            ) { viewModel.cambiar("publicoAsociado", it) }
            Campo(
                etiqueta = "Observaciones",
                valor = estadoFormulario.observaciones,
                error = estadoFormulario.errores["observaciones"],
                enabled = !estadoFormulario.guardando
            ) { viewModel.cambiar("observaciones", it) }
        }
        Button(onClick = {
                viewModel.guardar(esContacto)
            },
            enabled = !estadoFormulario.guardando) {
            Text(if (estadoFormulario.guardando) "Guardando…" else "Guardar")
        }
        MensajeOperacion(estadoFormulario.mensaje, estadoFormulario.esError)
        Text("Registros guardados",
            style = MaterialTheme.typography.titleMedium)
        if (esContacto) {
            if (contactos.isEmpty()) Text("No hay contactos registrados")
            contactos.forEach { c ->
                Card {
                    // Resolver por ID evita confundir organizaciones con nombres repetidos.
                    val organizacion = organizaciones.firstOrNull { it.id == c.organizacionId }
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = "${c.nombre} · ${organizacion?.nombre.orEmpty()}\n" +
                            "Cargo: ${c.cargo} · Área: ${c.area}\n" +
                            "Correo: ${c.correo}\nTeléfono: ${c.telefono}\nMedio: ${c.medio}"
                    )
                    TextButton(onClick = { onHistorial(c.id) }) { Text("Ver historial de contacto") }
                }
            }
        } else {
            if (organizaciones.isEmpty()) Text("No hay organizaciones registradas")
            organizaciones.forEach { o -> Card {
                    Text(modifier = Modifier.padding(12.dp), text = "${o.nombre}\nTipo: ${o.tipo} · Rubro: ${o.rubro}\n" +
                        "Comuna: ${o.comuna} · Región: ${o.region}\nSitio: ${o.sitioWeb}\n" +
                        "Público: ${o.publicoAsociado}\nObservaciones: ${o.observaciones}")
                    TextButton(onClick = { onHistorial(o.id) }) { Text("Ver eventos e historial") }
                }
            }
        }
    }
}
