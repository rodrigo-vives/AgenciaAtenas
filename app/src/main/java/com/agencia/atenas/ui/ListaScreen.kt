package com.agencia.atenas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.EstadoOportunidad
import com.agencia.atenas.data.GestionOportunidadEntity
import com.agencia.atenas.utils.SesionActual

@Composable
fun ListaScreen(
    onIrACrear: () -> Unit,
    onIrADetalle: (Int) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: ListaViewModel = viewModel()
) {

    // Observa el estado del ViewModel.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Obtiene la lista filtrada según búsqueda y estado.
    val gestionesFiltradas = viewModel.obtenerGestionesFiltradas()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // Encabezado: usuario logueado + botón de cerrar sesión.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hola, ${SesionActual.usuario}",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = { onCerrarSesion() }) {
                    Text("Cerrar sesión")
                }
            }

            // Barra de búsqueda por nombre de oportunidad.
            OutlinedTextField(
                value = uiState.textoBusqueda,
                onValueChange = { viewModel.cambiarBusqueda(it) },
                label = { Text("Buscar por nombre") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            // Filtros por estado: una fila de botones.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { viewModel.limpiarFiltroEstado() }) {
                    Text("Todos")
                }
                Button(onClick = { viewModel.cambiarFiltroEstado(EstadoOportunidad.CONFIRMADO.name) }) {
                    Text("Confirmados")
                }
                Button(onClick = { viewModel.cambiarFiltroEstado(EstadoOportunidad.EN_ESPERA.name) }) {
                    Text("En espera")
                }
            }

            // Botón para crear una nueva gestión.
            Button(
                onClick = { onIrACrear() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("+ Nueva oportunidad")
            }

            // Indicador de carga mientras Room responde.
            if (uiState.cargando) {
                Text(
                    text = "Cargando...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Lista de gestiones (o mensaje si está vacía).
            if (gestionesFiltradas.isEmpty() && !uiState.cargando) {
                Text(
                    text = "No hay oportunidades registradas",
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gestionesFiltradas) { gestion ->
                        TarjetaGestion(
                            gestion = gestion,
                            onClick = { onIrADetalle(gestion.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaGestion(
    gestion: GestionOportunidadEntity,
    onClick: () -> Unit
) {

    // Color de la tarjeta según el estado de la gestión.
    // Sirve como "alerta visual" para eventos próximos o confirmados.
    val colorTarjeta = when (gestion.estado) {
        EstadoOportunidad.CONFIRMADO -> Color(0xFFC8E6C9) // verde claro
        EstadoOportunidad.EN_ESPERA -> Color(0xFFFFF9C4) // amarillo claro
        EstadoOportunidad.DESCARTADO -> Color(0xFFFFCDD2) // rojo claro
        else -> Color(0xFFE0E0E0) // gris claro
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorTarjeta)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = gestion.nombreOportunidad,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Organización: ${gestion.nombreOrganizacion}")
            Text(text = "Fecha: ${gestion.fechaInicio}")
            Text(text = "Estado: ${gestion.estado.etiqueta}")
            Text(text = "Prioridad: ${gestion.prioridad}")
        }
    }
}