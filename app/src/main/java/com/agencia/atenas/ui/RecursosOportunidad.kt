package com.agencia.atenas.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.OportunidadDetalle
import com.agencia.atenas.utils.intervaloCalendario
import com.agencia.atenas.utils.cargarVistaPrevia
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Bloque del detalle: solo recibe una oportunidad ya consultada y autorizada. */
@Composable
fun RecursosOportunidad(item: OportunidadDetalle, viewModel: EvidenciaViewModel = viewModel()) {
    val context = LocalContext.current
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var mensajeCalendario by remember { mutableStateOf("") }

    // TakePicture escribe la foto completa en el content:// recibido y devuelve true/false.
    // El contrato concede permisos temporales a la cámara; no se comparte una ruta file://.
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        viewModel.terminar(exito)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar(item.id) }

    Text("Fotografías de la oportunidad", style = MaterialTheme.typography.titleMedium)
    Button(enabled = !estado.ocupado, onClick = {
        viewModel.preparar(item.id) { ruta ->
            try {
                val uri = FileProvider.getUriForFile(context,
                    "${context.packageName}.evidencias", File(ruta))
                camara.launch(uri)
            } catch (e: Exception) {
                // También se limpia el archivo vacío si no hay cámara o no se pudo abrir.
                viewModel.terminar(false, "No se pudo abrir la cámara del dispositivo")
            }
        }
    }) { Text("Tomar fotografía") }
    if (estado.ocupado) CircularProgressIndicator()
    // ANIMACIÓN 3: Crossfade sustituye el mensaje anterior por el nuevo mediante
    // un desvanecimiento. Observa la cantidad real publicada después de guardar:
    // cancelar la cámara no cambia el contador ni anuncia una foto inexistente.
    // Duración de un segundo para que se pueda observar la transición en la defensa.
    // Solo cambia la presentación: el contador sigue usando la cantidad realmente guardada.
    Crossfade(targetState = estado.fotos.size, animationSpec = tween(1000),
        label = "Cantidad de fotografías guardadas") { cantidad ->
        Text(when (cantidad) {
            0 -> "No hay fotografías guardadas"
            1 -> "1 fotografía guardada"
            else -> "$cantidad fotografías guardadas"
        })
    }
    if (estado.fotos.isNotEmpty()) Text("Toca una fotografía para verla en grande")
    estado.fotos.forEachIndexed { indice, ruta ->
        // La ruta es identidad estable: al insertar la foto nueva al principio,
        // Compose conserva la imagen y el estado de las fotos que ya estaban.
        key(ruta) { VistaPreviaFoto(ruta, indice + 1) }
    }
    MensajeOperacion(estado.mensaje, estado.esError)

    Text("Calendario del dispositivo", style = MaterialTheme.typography.titleMedium)
    OutlinedButton(onClick = {
        try {
            val intervalo = intervaloCalendario(item.fechaInicio, item.fechaTermino, item.horario)
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, item.nombreOportunidad)
                putExtra(CalendarContract.Events.DESCRIPTION, item.descripcion)
                putExtra(CalendarContract.Events.EVENT_LOCATION,
                    listOf(item.ubicacionGeneral, item.comuna, item.region).filter { it.isNotBlank() }.joinToString(", "))
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, intervalo.inicio)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, intervalo.termino)
                putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, intervalo.todoElDia)
            }
            // Se abre el editor nativo. El usuario decide guardar o cancelar allí.
            // No escribimos directamente en Calendar: no hace falta READ/WRITE_CALENDAR.
            context.startActivity(intent)
            mensajeCalendario = "Revisa y guarda el evento en el calendario. Abrirlo no confirma que se haya guardado."
        } catch (e: ActivityNotFoundException) {
            mensajeCalendario = "No hay una aplicación de calendario disponible en este dispositivo"
        } catch (e: Exception) {
            mensajeCalendario = e.message ?: "No se pudo abrir el calendario"
        }
    }) { Text("Añadir evento al calendario") }
    if (mensajeCalendario.isNotBlank()) Text(mensajeCalendario)
}

@Composable
private fun VistaPreviaFoto(ruta: String, numero: Int) {
    // Decodificar fuera del hilo de la interfaz evita congelarla con fotos grandes.
    // Primero leemos dimensiones; después reducimos el bitmap antes de cargarlo en memoria.
    var terminada by remember(ruta) { mutableStateOf(false) }
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        // Esta parte solo espera la lectura y publica el resultado para Compose.
        // La reducción de tamaño y la orientación están explicadas en utils/Imagenes.kt.
        value = withContext(Dispatchers.IO) { cargarVistaPrevia(ruta) }
        terminada = true
    }
    // ANIMACIÓN 1: la vista previa entra al finalizar la lectura del archivo.
    // visible cambia de false a true: AnimatedVisibility desvanece la imagen y
    // despliega su espacio. La animación representa el resultado, no guarda archivos.
    var ampliada by rememberSaveable(ruta) { mutableStateOf(false) }
    AnimatedVisibility(visible = imagen != null,
        // Entrada deliberadamente más lenta para hacer visible el despliegue de la nueva foto.
        enter = fadeIn(tween(900)) + expandVertically(tween(900)),
        exit = fadeOut(tween(600)) + shrinkVertically(tween(600))) {
        imagen?.let { bitmap ->
            Image(bitmap = bitmap, contentDescription = "Ampliar fotografía $numero de esta oportunidad",
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    .clickable(role = Role.Button, onClickLabel = "Ver fotografía en grande") { ampliada = true },
                contentScale = ContentScale.Fit)
        }
    }
    if (imagen == null) {
        Text(if (terminada) "No se pudo mostrar la fotografía $numero" else "Cargando fotografía $numero…")
    }
    if (ampliada) {
        imagen?.let { bitmap ->
            FotoAmpliada(bitmap, numero, onCerrar = { ampliada = false })
        }
    }
}

/**
 * ANIMACIÓN 2: abrir/cerrar la ampliación es otra interacción del usuario.
 * Reutilizamos el bitmap ya cargado; ampliar no crea un archivo ni duplica una foto.
 * Es una vista mayor, sin recorte, no un visor con zoom mediante gestos.
 */
@Composable
private fun FotoAmpliada(imagen: ImageBitmap, numero: Int, onCerrar: () -> Unit) {
    val transicion = remember { MutableTransitionState(false).apply { targetState = true } }
    // No retiramos inmediatamente el Dialog al cerrar: primero targetState=false
    // permite terminar el desvanecimiento de salida. Solo entonces se quita de composición.
    // Botón Cerrar, Atrás y toque fuera del diálogo solicitan el mismo cierre.
    LaunchedEffect(transicion.isIdle, transicion.currentState, transicion.targetState) {
        if (transicion.isIdle && !transicion.currentState && !transicion.targetState) onCerrar()
    }
    Dialog(onDismissRequest = { transicion.targetState = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // El contenedor reserva el tamaño final desde el primer instante.
        // Aquí animamos solo la opacidad: expandir la altura cambiaba las medidas
        // del diálogo y la imagen con Fit parecía achicarse antes de agrandarse.
        Box(Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.9f)) {
            AnimatedVisibility(visibleState = transicion, modifier = Modifier.fillMaxSize(),
                // Se alarga el desvanecimiento para la demostración. No animamos medidas
                // ni escala: la foto permanece estable durante toda la apertura y el cierre.
                enter = fadeIn(tween(800)),
                exit = fadeOut(tween(650))) {
                Surface(modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Fotografía $numero", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        Image(bitmap = imagen, contentDescription = "Vista ampliada de la fotografía $numero",
                            modifier = Modifier.weight(1f).fillMaxWidth(), contentScale = ContentScale.Fit)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { transicion.targetState = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}
