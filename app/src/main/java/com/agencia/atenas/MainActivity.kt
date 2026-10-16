package com.agencia.atenas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.agencia.atenas.ui.Navegacion
import com.agencia.atenas.ui.theme.AgenciaAtenasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgenciaAtenasTheme {
                Navegacion()
            }
        }
    }
}

