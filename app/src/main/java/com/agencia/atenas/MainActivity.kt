package com.agencia.atenas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.agencia.atenas.ui.Navegacion
import com.agencia.atenas.ui.theme.AgenciaAtenasTheme

/**
 * Punto de entrada que Android abre según AndroidManifest.xml.
 * Aquí se instala el tema y la navegación; los formularios y consultas van en sus archivos.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permite dibujar hasta los bordes. Cada Scaffold aplica el margen del sistema.
        enableEdgeToEdge()
        // setContent inicia Compose. Navegacion decide qué pantalla dibujar según la ruta.
        setContent {
            AgenciaAtenasTheme {
                Navegacion()
            }
        }
    }
}

