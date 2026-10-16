package com.agencia.atenas.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navegacion() {

    // Controlador de navegación: recuerda en qué pantalla estamos.
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // Ruta: Login. Pantalla inicial.
        composable("login") {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate("lista") {
                        // Elimina el login del back stack para que
                        // el usuario no pueda volver con el botón atrás.
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Ruta: Lista. Pantalla principal después del login.
        composable("lista") {
            ListaScreen(
                onIrACrear = {
                    navController.navigate("crear")
                },
                onIrADetalle = { id ->
                    navController.navigate("detalle/$id")
                },
                onCerrarSesion = {
                    navController.navigate("login") {
                        // Vuelve al login y limpia todo el back stack.
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Ruta: Crear. Pendiente de implementar (P2).
        composable("crear") {
            Text("Pantalla de Crear (pendiente)")
        }

        // Ruta: Detalle. Pendiente de implementar (P2).
        // Recibe el id de la gestión como parámetro.
        composable("detalle/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
            Text("Detalle de la gestión $id (pendiente)")
        }
    }
}