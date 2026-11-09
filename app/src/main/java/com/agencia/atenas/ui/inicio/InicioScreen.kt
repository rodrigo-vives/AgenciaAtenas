package com.agencia.atenas.ui.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.agencia.atenas.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.oportunidades.EstadoOportunidad
import com.agencia.atenas.data.oportunidades.RecordatorioSeguimiento
import com.agencia.atenas.ui.componentes.MensajeOperacion
import com.agencia.atenas.utils.SesionActual
import com.agencia.atenas.utils.fechas.SituacionRecordatorio
import com.agencia.atenas.utils.fechas.situacionRecordatorio
import com.agencia.atenas.utils.validacion.Fechas
import com.agencia.atenas.ui.theme.*

/**
 * Recordatorios internos: se muestran al abrir Inicio, sin notificaciones del sistema
 * ni permisos nuevos. La clasificación por fecha es calculada, no otro estado en Room.
 */
@Composable
fun InicioScreen(
    onLista: () -> Unit,
    onPerfil: () -> Unit,
    onOrganizaciones: () -> Unit,
    onContactos: () -> Unit,
    onSupervisor: () -> Unit,
    onSalir: () -> Unit,
    onSeguimiento: (Int, Int) -> Unit,
    onDetalle: (Int) -> Unit,
    viewModel: InicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var hoy by remember { mutableStateOf(Fechas.hoy()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { hoy = Fechas.hoy() }

    // Leer el estado en este mismo ámbito evita calcular listas con una instantánea
    // antigua mientras solo se recompone el contenido interno de Pagina.
    val atrasados = estado.recordatorios.filter {
        situacionRecordatorio(it.fechaProxima, hoy = hoy) == SituacionRecordatorio.ATRASADO
    }
    val pendientes = estado.recordatorios.filter {
        situacionRecordatorio(it.fechaProxima, hoy = hoy) in
            listOf(SituacionRecordatorio.HOY, SituacionRecordatorio.PENDIENTE)
    }
    val paraHoy = pendientes.count { it.fechaProxima == hoy }
    val activos = estado.oportunidades.filter { it.estado !in listOf(
        EstadoOportunidad.DESCARTADO, EstadoOportunidad.ACTIVACION_REALIZADA, EstadoOportunidad.CERRADO
    ) }
    val proximos = activos.filter { it.fechaInicio >= hoy }.sortedBy { it.fechaInicio }.take(3)
    val sinContacto = activos.filter { it.contactoId == null }
    val sinRespuesta = activos.filter { it.estado in listOf(
        EstadoOportunidad.PROPUESTA_ENVIADA, EstadoOportunidad.EN_ESPERA
    ) }

    TemaMarca {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                EncabezadoInicio(onPerfil)
                Column(
                    Modifier.align(Alignment.CenterHorizontally).widthIn(max = 600.dp)
                        .fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Inicio", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (estado.cargando) CircularProgressIndicator()
                    MensajeOperacion(estado.mensaje, estado.esError)
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IndicadorInicio(estado.oportunidades.size, "oportunidades visibles",
                                R.drawable.ic_inicio_panel, FondoAcentoSuave, TextoAcentoSuave, Modifier.weight(1f))
                            IndicadorInicio(paraHoy, "acciones para hoy",
                                R.drawable.ic_inicio_aviso, FondoAvisoInicio, TextoAvisoInicio, Modifier.weight(1f))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccesoInicio("Oportunidades", R.drawable.ic_inicio_maletin, FondoAcentoSuave,
                            TextoAcentoSuave, onLista, Modifier.weight(1f))
                        AccesoInicio("Organizaciones", R.drawable.ic_inicio_edificio, FondoInfoInicio,
                            TextoInfoInicio, onOrganizaciones, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccesoInicio("Contactos", R.drawable.ic_inicio_contactos, FondoContactosInicio,
                            TextoContactosInicio, onContactos, Modifier.weight(1f))
                        if (SesionActual.esSupervisor) {
                            AccesoInicio("Panel de supervisor", R.drawable.ic_inicio_panel, FondoAcentoSuave,
                                TextoAcentoSuave, onSupervisor, Modifier.weight(1f))
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    if (!estado.cargando) {
                        Text("Gestiones atrasadas", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        if (atrasados.isEmpty()) Text("No hay gestiones atrasadas")
                        atrasados.forEach { recordatorio ->
                            TarjetaRecordatorio(recordatorio, "Atrasada", true) {
                                onSeguimiento(recordatorio.oportunidadId, recordatorio.seguimientoId)
                            }
                        }

                        Text("Seguimientos pendientes (${pendientes.size})", style = MaterialTheme.typography.titleMedium)
                        if (pendientes.isEmpty()) Text("No hay acciones programadas para hoy o después")
                        pendientes.forEach { recordatorio ->
                            val etiqueta = if (recordatorio.fechaProxima == hoy) "Vence hoy" else "Programada"
                            TarjetaRecordatorio(recordatorio, etiqueta, false) {
                                onSeguimiento(recordatorio.oportunidadId, recordatorio.seguimientoId)
                            }
                        }

                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Próximos eventos", style = MaterialTheme.typography.titleMedium)
                        if (proximos.isEmpty()) Text("No hay eventos próximos registrados")
                        proximos.forEach { evento ->
                            OutlinedCard(onClick = { onDetalle(evento.id) }, modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconoInicio(R.drawable.ic_inicio_calendario, FondoInfoInicio, TextoInfoInicio)
                                    Column(Modifier.weight(1f)) {
                                        Text(evento.nombreOportunidad, fontWeight = FontWeight.SemiBold)
                                        Text(listOf(Fechas.visible(evento.fechaInicio), evento.comuna)
                                            .filter { it.isNotBlank() }.joinToString(" · "),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("›")
                                }
                            }
                        }

                        // Son avisos comerciales distintos de una tarea con fecha. Se basan en
                        // información registrada: no suponen llamadas ni correos enviados realmente.
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Oportunidades sin contacto (${sinContacto.size})", style = MaterialTheme.typography.titleMedium)
                        if (sinContacto.isEmpty()) Text("Todas las oportunidades activas tienen contacto asociado")
                        sinContacto.forEach { oportunidad ->
                            TextButton(colors = BotonTexto.soloTexto(), onClick = { onDetalle(oportunidad.id) }) { Text(oportunidad.nombreOportunidad) }
                        }
                        // No tener una tarea pendiente no equivale a tener seguimiento.
                        // Esta sección identifica oportunidades sin ninguna gestión comercial registrada.
                        Text("Oportunidades sin seguimiento (${estado.sinSeguimiento.size})", style = MaterialTheme.typography.titleMedium)
                        if (estado.sinSeguimiento.isEmpty()) Text("Todas las oportunidades activas tienen alguna gestión registrada")
                        estado.sinSeguimiento.forEach { oportunidad ->
                            TextButton(colors = BotonTexto.soloTexto(), onClick = { onDetalle(oportunidad.id) }) { Text(oportunidad.nombreOportunidad) }
                        }
                        Text("Propuestas sin respuesta (${sinRespuesta.size})", style = MaterialTheme.typography.titleMedium)
                        if (sinRespuesta.isEmpty()) Text("No hay propuestas registradas en espera de respuesta")
                        sinRespuesta.forEach { oportunidad ->
                            TextButton(colors = BotonTexto.soloTexto(), onClick = { onDetalle(oportunidad.id) }) {
                                Text("${oportunidad.nombreOportunidad} · ${oportunidad.estado.etiqueta}")
                            }
                        }
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = onSalir,
                        colors = BotonTexto.soloTexto(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cerrar sesión") }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRecordatorio(
    tarea: RecordatorioSeguimiento,
    etiqueta: String,
    atrasada: Boolean,
    onResolver: () -> Unit
) {
    if (atrasada) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = FondoGestionAtrasada),
            border = BorderStroke(1.dp, BordeGestionAtrasada)
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(12.dp)) {
                // En pantallas estrechas o con letra ampliada, el botón baja para no apretar el texto.
                if (maxWidth >= 290.dp && LocalDensity.current.fontScale <= 1.2f) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatosGestionAtrasada(tarea, Modifier.weight(1f))
                        BotonResolverGestion(onResolver)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatosGestionAtrasada(tarea, Modifier.fillMaxWidth())
                        Box(Modifier.align(Alignment.End)) { BotonResolverGestion(onResolver) }
                    }
                }
            }
        }
        return
    }

    // Los pares semánticos de MaterialTheme mantienen contraste en tema claro/oscuro.
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
    Card(modifier = Modifier.fillMaxWidth(), colors = colores) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelLarge)
            Text(tarea.proximaAccion, style = MaterialTheme.typography.titleMedium)
            Text("Oportunidad: ${tarea.nombreOportunidad}")
            Text("Fecha: ${Fechas.visible(tarea.fechaProxima)}")
            Text("Responsable actual: ${tarea.responsable}")
            Button(colors = BotonGeneral.relleno(), onClick = onResolver) { Text("Registrar resultado") }
        }
    }
}

@Composable
private fun DatosGestionAtrasada(tarea: RecordatorioSeguimiento, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconoInicio(R.drawable.ic_error_formulario, FondoIconoGestionAtrasada, IconoGestionAtrasada)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(tarea.proximaAccion, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            val secundario = MaterialTheme.colorScheme.onSurfaceVariant
            Text(tarea.nombreOportunidad, color = secundario, style = MaterialTheme.typography.bodyMedium)
            Text("Fecha: ${Fechas.visible(tarea.fechaProxima)}", color = secundario,
                style = MaterialTheme.typography.bodyMedium)
            Text("Responsable: ${tarea.responsable}", color = secundario,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BotonResolverGestion(onResolver: () -> Unit) {
    Button(onClick = onResolver, colors = BotonGeneral.relleno(),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
        Text("Registrar resultado")
    }
}

@Composable
private fun EncabezadoInicio(onPerfil: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().background(colores.inverseSurface).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(painterResource(R.drawable.atenea_icono), contentDescription = null,
                modifier = Modifier.size(36.dp))
            Text("Agencia Atenas", color = colores.inverseOnSurface,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val usuario = SesionActual.usuario.orEmpty()
            Box(Modifier.size(36.dp).background(colores.primary, CircleShape),
                contentAlignment = Alignment.Center) {
                Text(usuario.take(1).uppercase(), color = colores.onPrimary)
            }
            Column(Modifier.weight(1f)) {
                Text("Hola, $usuario", color = colores.inverseOnSurface, fontWeight = FontWeight.SemiBold)
                Text(if (SesionActual.esSupervisor) "Supervisor comercial" else "Ejecutivo comercial",
                    color = colores.inverseOnSurface, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onPerfil, colors = BotonVolverEncabezado.soloTexto()) { Text("Mi perfil") }
        }
    }
}

@Composable
private fun IconoInicio(icono: Int, fondo: Color, texto: Color) {
    Box(Modifier.size(38.dp).background(fondo, CircleShape), contentAlignment = Alignment.Center) {
        Icon(painterResource(icono), contentDescription = null, tint = texto, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun IndicadorInicio(
    cantidad: Int, etiqueta: String, icono: Int, fondo: Color, texto: Color, modifier: Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IconoInicio(icono, fondo, texto)
        Text(cantidad.toString(), color = texto, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AccesoInicio(
    etiqueta: String, icono: Int, fondo: Color, texto: Color, onClick: () -> Unit, modifier: Modifier
) {
    OutlinedCard(onClick = onClick, modifier = modifier.heightIn(min = 104.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IconoInicio(icono, fondo, texto)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(etiqueta, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
