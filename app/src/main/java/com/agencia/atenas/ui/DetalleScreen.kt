package com.agencia.atenas.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.*
import com.agencia.atenas.utils.*

/**
 * Consulta una oportunidad ya guardada y muestra todos sus datos. Desde aquí se abre
 * Editar o Seguimiento. El cambio de estado se solicita al ViewModel y se autoriza en Repository.
 */

@Composable
fun DetalleScreen(id: Int,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onSeguimiento: () -> Unit,
    onResultado: () -> Unit,
    viewModel: DetalleViewModel = viewModel()) {
    // La consulta entrega un StateFlow. collectAsStateWithLifecycle lo convierte en estado observable
    // para Compose y recoge sus valores mientras el ciclo de vida está activo. Así un cambio en Room
    // puede actualizar la pantalla sin consultar manualmente después de cada guardado.
    val estadoFormulario by viewModel.uiState.collectAsStateWithLifecycle()

    // CAMBIO: recargar al regresar de Editar para no mostrar el detalle antiguo.
    // ON_RESUME ocurre cuando la pantalla vuelve al primer plano. Se recarga el detalle al regresar
    // de Editar para mostrar los valores nuevos, aunque se haya conservado la instancia del ViewModel.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargar(id)
    }
    Pagina("Detalle de oportunidad",
        onVolver) {
        if (estadoFormulario.cargando) CircularProgressIndicator()
        estadoFormulario.item?.let { item ->
            Text(item.nombreOportunidad,
                style = MaterialTheme.typography.titleMedium)
            Text("Estado: ${item.estado.etiqueta}")
            Button(onClick = onEditar,
                enabled = !estadoFormulario.ocupado) {
                Text("Editar oportunidad")
            }
            Button(onClick = onSeguimiento) {
                Text("Seguimiento e historial")
            }
            if (item.estado in listOf(EstadoOportunidad.CONFIRMADO, EstadoOportunidad.ACTIVACION_REALIZADA, EstadoOportunidad.CERRADO)) {
                Button(onClick = onResultado) { Text("Registrar resultado de activación") }
            }
            if (estadoFormulario.resultados.isNotEmpty()) {
                Text("Resultados de activación", style = MaterialTheme.typography.titleLarge)
                estadoFormulario.resultados.forEach { resultado ->
                    OutlinedCard {
                        Dato("Realización", Fechas.visible(resultado.indicadores.fechaRealizacion))
                        Dato("Autor", resultado.responsable)
                        Dato("Evaluación", resultado.evaluacion)
                        Dato("Asistentes", resultado.indicadores.asistentes?.toString().orEmpty())
                        Dato("Contactos generados", resultado.indicadores.contactosGenerados?.toString().orEmpty())
                        Dato("Interesados", resultado.indicadores.interesados?.toString().orEmpty())
                        Dato("Inscripciones", resultado.indicadores.inscripciones?.toString().orEmpty())
                        Dato("Observaciones", resultado.observaciones)
                        Dato("Aprendizajes", resultado.indicadores.aprendizajes)
                    }
                }
            }
            Selector("Cambiar estado",
                item.estado.etiqueta,
                estadosPermitidos(item.estado).map {
                    it.etiqueta
                },
                !estadoFormulario.ocupado) { elegido ->
                viewModel.cambiarEstado(EstadoOportunidad.entries.first {
                        it.etiqueta == elegido
                })
            }

            // Los recursos nativos se usan después de guardar: ya existe un ID estable
            // para asociar evidencias y fechas definitivas para preparar el calendario.
            HorizontalDivider()
            RecursosOportunidad(item)
            HorizontalDivider()
            Text("Información comercial", style = MaterialTheme.typography.titleLarge)

            // CAMBIO: se muestran todos los datos, incluidos los que antes solo estaban en Crear.
            Dato("Nombre de la oportunidad",
                item.nombreOportunidad)
            Dato("Tipo de oportunidad",
                item.tipoOportunidad)
            Dato("Descripción",
                item.descripcion)
            Dato("Fuente de información",
                item.fuenteInformacion)
            Dato("Fecha de detección (DD/MM/AAAA)",
                Fechas.visible(item.fechaDeteccion))
            Dato("Responsable",
                item.responsable)
            Dato("Prioridad (Alta, Media o Baja)",
                item.prioridad)
            Dato("Potencial comercial",
                item.potencialComercial)
            HorizontalDivider()
            Text("Datos del evento", style = MaterialTheme.typography.titleLarge)
            Dato("Fecha de inicio (DD/MM/AAAA)",
                Fechas.visible(item.fechaInicio))
            Dato("Fecha de término (DD/MM/AAAA)",
                Fechas.visible(item.fechaTermino))
            Dato("Horario (HH:mm a HH:mm)",
                item.horario)
            Dato("Ubicación general",
                item.ubicacionGeneral)
            Dato("Comuna",
                item.comuna)
            Dato("Región",
                item.region)
            Dato("Público esperado",
                item.publicoEsperado)
            Dato("Público objetivo",
                item.publicoObjetivo)
            Dato("Cantidad estimada de asistentes",
                item.cantidadAsistentes?.toString().orEmpty())
            Dato("Condiciones de participación",
                item.condicionesParticipacion)
            Dato("Requisitos",
                item.requisitos)
            Dato("Espacios disponibles",
                item.espaciosDisponibles)
            Dato("Costo referencial ficticio (número CLP)",
                item.costosReferenciales)
            HorizontalDivider()
            Text("Organización y contacto", style = MaterialTheme.typography.titleLarge)
            Dato("Organización",
                item.nombreOrganizacion)
            Dato("Tipo de organización",
                item.tipoOrganizacion)
            Dato("Rubro",
                item.rubroOrganizacion)
            Dato("Nombre del contacto",
                item.nombreContacto)
            Dato("Cargo",
                item.cargoContacto)
            Dato("Área",
                item.areaContacto)
            Dato("Correo ficticio",
                item.correoContacto)
            Dato("Teléfono ficticio",
                item.telefonoContacto)
            Dato("Medio preferente",
                item.medioContactoPreferente)
            HorizontalDivider()
            Text("Registro y observaciones", style = MaterialTheme.typography.titleLarge)
            Dato("Última actualización",
                item.fechaUltimaActualizacion)
            Dato("Observaciones anteriores",
                item.historialObservaciones)
        }
        MensajeOperacion(estadoFormulario.mensaje, estadoFormulario.esError)
    }
}

@Composable
fun Dato(etiqueta: String,
    valor: String) {
    Text("$etiqueta: ${valor.ifBlank { "Sin información" }}")
}
