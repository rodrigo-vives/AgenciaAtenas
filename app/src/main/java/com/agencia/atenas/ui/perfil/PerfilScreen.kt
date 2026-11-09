package com.agencia.atenas.ui.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.R
import com.agencia.atenas.data.usuarios.Roles
import com.agencia.atenas.data.usuarios.UsuarioEntity
import com.agencia.atenas.ui.theme.*

@Composable
fun PerfilScreen(
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    TemaMarca {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EncabezadoPerfil(onVolver)
                Column(
                    modifier = Modifier.padding(20.dp).widthIn(max = 520.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (estado.cargando) CircularProgressIndicator()
                    estado.usuario?.let { TarjetaPerfil(it, estado.oportunidadesVisibles) }
                    if (estado.mensaje.isNotBlank()) {
                        Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                    }
                    // La navegación reutiliza el cierre de sesión de Inicio.
                    Button(
                        onClick = onCerrarSesion,
                        colors = BotonCerrarSesion.relleno(),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoPerfil(onVolver: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().background(colores.inverseSurface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.atenea_icono),
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(MaterialTheme.shapes.small)
            )
            Text("Agencia Atenas", color = colores.inverseOnSurface,
                style = MaterialTheme.typography.titleMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVolver, colors = BotonVolverEncabezado.soloTexto()) {
                Text("← Volver")
            }
            Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                Text("Mi perfil", color = colores.inverseOnSurface,
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Datos de la cuenta actual", color = colores.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TarjetaPerfil(usuario: UsuarioEntity, oportunidadesVisibles: Int) {
    val supervisor = usuario.rol == Roles.SUPERVISOR
    val colores = MaterialTheme.colorScheme
    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp)
                        .background(colores.primary, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(usuario.nombre.trim().take(1).uppercase().ifEmpty { "?" },
                        color = colores.onPrimary, style = MaterialTheme.typography.titleLarge)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    EtiquetaPerfil(
                        if (supervisor) "Supervisor comercial" else "Ejecutivo comercial",
                        FondoAcentoSuave, TextoAcentoSuave
                    )
                }
            }
            HorizontalDivider()
            FilaPerfil("Usuario") { ValorPerfil(usuario.usuario) }
            HorizontalDivider()
            FilaPerfil("Identificador") { ValorPerfil(usuario.id.toString()) }
            HorizontalDivider()
            FilaPerfil("Estado de cuenta") {
                EtiquetaPerfil(
                    if (usuario.activo) "Activa" else "Desactivada",
                    if (usuario.activo) FondoExito else colores.errorContainer,
                    if (usuario.activo) TextoExito else colores.onErrorContainer
                )
            }
            HorizontalDivider()
            FilaPerfil("Oportunidades visibles") {
                Text(oportunidadesVisibles.toString(), color = colores.primary,
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            EtiquetaPerfil(
                if (supervisor) "Puedes consultar las oportunidades del equipo y asignar responsables."
                else "Puedes gestionar las oportunidades que tienes asignadas.",
                FondoAcentoSuave, TextoAcentoSuave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilaPerfil(etiqueta: String, valor: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(etiqueta, modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { valor() }
    }
}

@Composable
private fun ValorPerfil(valor: String) {
    Text(valor, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun EtiquetaPerfil(texto: String, fondo: Color, color: Color, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier.background(fondo, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}
