package com.agencia.atenas.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agencia.atenas.data.Roles

/** Consulta de perfil pedida por el caso; no incorpora administración de contraseñas. */
@Composable
fun PerfilScreen(onVolver: () -> Unit, viewModel: PerfilViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    Pagina("Mi perfil", onVolver) {
        if (estado.cargando) CircularProgressIndicator()
        estado.usuario?.let { usuario ->
            Dato("Nombre ficticio", usuario.nombre)
            Dato("Usuario", usuario.usuario)
            Dato("Identificador", usuario.id.toString())
            Dato("Rol", if (usuario.rol == Roles.SUPERVISOR) "Supervisor comercial" else "Ejecutivo comercial")
            Dato("Cuenta", if (usuario.activo) "Activa" else "Desactivada")
            Dato("Oportunidades visibles", estado.oportunidadesVisibles.toString())
            Text(if (usuario.rol == Roles.SUPERVISOR)
                "Puedes consultar el equipo y asignar responsables."
                else "Puedes gestionar las oportunidades que tienes asignadas.")
        }
        if (estado.mensaje.isNotBlank()) Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
    }
}
