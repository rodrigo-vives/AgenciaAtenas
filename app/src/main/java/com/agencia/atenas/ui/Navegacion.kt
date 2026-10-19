package com.agencia.atenas.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agencia.atenas.utils.SesionActual

/**
 * Mapa de destinos, como en el PPT 03. Cada Screen recibe callbacks (funciones),
 * no el controlador completo. Así muestra botones sin decidir rutas por su cuenta.
 * Los destinos con {id} consultan Room por esa clave: no transportan registros completos.
 */
@Composable
fun Navegacion() {
    val navController = rememberNavController()

    val salir: () -> Unit = {
        SesionActual.cerrar()
        navController.navigate("login") {
            // Retira las pantallas protegidas: Atrás no debe recuperar una sesión cerrada.
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
    val volver: () -> Unit = {
        navController.popBackStack()
        // popBackStack devuelve Boolean; este callback solo comunica una acción (Unit).
        Unit
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginExitoso = {
                navController.navigate("inicio") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("inicio") {
            InicioScreen(
                onLista = { navController.navigate("lista") },
                onPerfil = { navController.navigate("perfil") },
                onOrganizaciones = { navController.navigate("organizaciones") },
                onContactos = { navController.navigate("contactos") },
                onSupervisor = { navController.navigate("supervisor") },
                onSalir = salir,
                onSeguimiento = { oportunidadId, pendienteId ->
                    navController.navigate("seguimiento/$oportunidadId?pendienteId=$pendienteId")
                },
                onDetalle = { id -> navController.navigate("detalle/$id") }
            )
        }
        composable("perfil") { PerfilScreen(onVolver = volver) }
        composable("lista") {
            ListaScreen(
                onIrACrear = { navController.navigate("crear") },
                onIrADetalle = { id -> navController.navigate("detalle/$id") },
                onCerrarSesion = salir
            )
        }
        composable("crear") {
            CrearScreen(
                onVolver = volver,
                onGuardado = { id ->
                    // Este evento llega después del guardado. Se reemplaza el formulario
                    // por el detalle para que Atrás no vuelva a crear la misma oportunidad.
                    navController.navigate("detalle/$id") {
                        popUpTo("crear") { inclusive = true }
                    }
                }
            )
        }
        composable("editar/{id}") { entrada ->
            // Los argumentos de estas rutas son texto; no se fuerza una conversión con !!.
            val id = entrada.arguments?.getString("id")?.toIntOrNull()
            if (id != null) {
                CrearScreen(id = id, onVolver = volver, onGuardado = { volver() })
            }
        }
        composable("detalle/{id}") { entrada ->
            val id = entrada.arguments?.getString("id")?.toIntOrNull()
            if (id != null) {
                DetalleScreen(
                    id = id,
                    onVolver = volver,
                    onEditar = { navController.navigate("editar/$id") },
                    onResultado = { navController.navigate("resultado/$id") },
                    onSeguimiento = { navController.navigate("seguimiento/$id") }
                )
            }
        }
        // Dos rutas reutilizan el formulario de catálogo, con responsabilidades iguales.
        composable("organizaciones") {
            CatalogoScreen(esContacto = false, onVolver = volver,
                onHistorial = { id -> navController.navigate("historialOrganizacion/$id") })
        }
        composable("contactos") {
            CatalogoScreen(esContacto = true, onVolver = volver,
                onHistorial = { id -> navController.navigate("historialContacto/$id") })
        }
        composable(
            route = "seguimiento/{id}?pendienteId={pendienteId}",
            arguments = listOf(navArgument("pendienteId") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { entrada ->
            val id = entrada.arguments?.getString("id")?.toIntOrNull()
            val pendienteId = entrada.arguments?.getInt("pendienteId") ?: 0
            if (id != null) {
                // El parámetro opcional distingue gestión libre de una tarea a completar.
                SeguimientoScreen(id = id, onVolver = volver, pendienteInicialId = pendienteId)
            }
        }
        composable("resultado/{id}") { entrada ->
            entrada.arguments?.getString("id")?.toIntOrNull()?.let { id ->
                ResultadoScreen(id = id, onVolver = volver)
            }
        }
        composable("historialContacto/{id}") { entrada ->
            entrada.arguments?.getString("id")?.toIntOrNull()?.let { id ->
                HistorialCatalogoScreen(id, true, volver, onDetalle = { navController.navigate("detalle/$it") })
            }
        }
        composable("historialOrganizacion/{id}") { entrada ->
            entrada.arguments?.getString("id")?.toIntOrNull()?.let { id ->
                HistorialCatalogoScreen(id, false, volver, onDetalle = { navController.navigate("detalle/$it") })
            }
        }
        composable("supervisor") {
            SupervisorScreen(
                onVolver = volver,
                onDetalle = { id -> navController.navigate("detalle/$id") }
            )
        }
    }
}
