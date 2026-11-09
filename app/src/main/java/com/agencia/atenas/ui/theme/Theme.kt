package com.agencia.atenas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40


)

private val PaletaIngreso = lightColorScheme(
    primary = AcentoIngreso,
    onPrimary = SuperficieIngreso,
    background = FondoIngreso,
    onBackground = AzulMarca,
    surface = SuperficieIngreso,
    onSurface = AzulMarca,
    onSurfaceVariant = TextoSecundarioIngreso,
    outline = BordeIngreso,
    outlineVariant = BordeTarjetaIngreso,
    inverseSurface = AzulMarca,
    inverseOnSurface = FondoIngreso
)

// El ingreso conserva los colores de marca sin cambiar el tema del resto de la app.
@Composable
fun TemaIngreso(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PaletaIngreso, typography = Typography, content = content)
}

// Material 3 conserva sus colores originales cuando el valor es Color.Unspecified.
@Composable
fun ColoresBoton.relleno(): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = fondo, contentColor = texto)

@Composable
fun ColoresBoton.soloTexto(): ButtonColors =
    ButtonDefaults.textButtonColors(containerColor = fondo, contentColor = texto)

@Composable
fun ColoresBoton.contorno(): ButtonColors =
    ButtonDefaults.outlinedButtonColors(containerColor = fondo, contentColor = texto)

@Composable
fun AgenciaAtenasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // En Android 12 o posterior se puede usar la paleta del dispositivo.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
