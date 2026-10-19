package com.agencia.atenas.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Cómo se comprueban las fechas:
 * SimpleDateFormat se crea en cada operación para no compartir una instancia mutable entre hilos.
 * isLenient = false evita ajustar automáticamente fechas imposibles como 31 de febrero.
 * La expresión regular exige DD/MM/AAAA antes de parsear. aIso devuelve null si falla y permite
 * mostrar un error de campo; visible conserva textos antiguos si no puede convertirlos.
 * java.text permite esta implementación con minSdk 24 sin agregar desugaring en Gradle.
 */


/**
 * Conversión de fechas y reglas de formato reutilizables. La interfaz muestra DD/MM/AAAA;
 * Room conserva AAAA-MM-DD para que el orden alfabético coincida con el orden de fechas.
 */

// CAMBIO: validaciones fuera de Compose, como en las clases del profesor.
// Se usa java.text para mantener compatibilidad con minSdk 24 sin dependencias nuevas.
object Fechas {

    private fun formato(patron: String) = SimpleDateFormat(patron,
        Locale.ROOT).apply {
        isLenient = false
    }

    // Acepta únicamente una fecha real con dos dígitos de día y mes; rechaza fechas imposibles.
    fun aIso(texto: String): String? {
        if (!Regex("\\d{2}/\\d{2}/\\d{4}").matches(texto)) return null
        return try {
            val fecha = formato("dd/MM/yyyy").parse(texto) ?: return null
            formato("yyyy-MM-dd").format(fecha)
        } catch (_: Exception) {
            null
        }
    }

    // Convierte una fecha guardada al formato del formulario. Si no puede convertirla,
    // conserva el texto para no ocultar valores antiguos que requieren corrección.
    fun visible(texto: String): String {
        // No interpretar parcialmente un valor con caracteres sobrantes como si fuera ISO.
        if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(texto)) return texto
        return try {
            val fecha = formato("yyyy-MM-dd").parse(texto) ?: return texto
            formato("dd/MM/yyyy").format(fecha)
        } catch (_: Exception) {
            texto
        }
    }

    fun hoy(): String = formato("yyyy-MM-dd").format(Date())

    fun ahora(): String = formato("yyyy-MM-dd HH:mm:ss").format(Date())
}

// Correo y teléfono son opcionales en el contacto. Si hay texto, debe tener formato válido.
fun validarCorreo(valor: String): Boolean {
    if (valor.isBlank()) return true
    return Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(valor)
}

fun validarTelefono(valor: String): Boolean {
    if (valor.isBlank()) return true
    // Se permiten separadores comunes y +; se cuentan solo los dígitos, no los espacios.
    val caracteresPermitidos = Regex("^[+0-9 ()-]+$").matches(valor)
    val cantidadDigitos = valor.count { caracter -> caracter.isDigit() }
    return caracteresPermitidos && cantidadDigitos in 8..15
}
