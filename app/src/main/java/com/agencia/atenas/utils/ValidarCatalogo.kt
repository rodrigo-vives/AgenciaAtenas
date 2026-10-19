package com.agencia.atenas.utils

import com.agencia.atenas.ui.CatalogoUiState

/**
 * Reglas del formulario de organizaciones/contactos, fuera de Compose y del ViewModel.
 * Devuelve campo -> mensaje: un mapa vacío significa que se puede intentar guardar.
 * Los datos opcionales se comprueban únicamente cuando el usuario los escribe.
 */
fun validarCatalogo(estado: CatalogoUiState, esContacto: Boolean): Map<String, String> {
    val errores = mutableMapOf<String, String>()
    if (estado.nombre.isBlank()) {
        errores["nombre"] = "El nombre es obligatorio"
    }

    // Cada clave coincide con la que consulta CatalogoScreen al dibujar su Campo.
    val campos = mapOf(
        "nombre" to estado.nombre,
        "tipo" to estado.tipo,
        "rubro" to estado.rubro,
        "cargo" to estado.cargo,
        "area" to estado.area,
        "correo" to estado.correo,
        "telefono" to estado.telefono,
        "medio" to estado.medio,
        "comuna" to estado.comuna,
        "region" to estado.region,
        "sitioWeb" to estado.sitioWeb
    )
    for ((clave, valor) in campos) {
        if (valor.length > 120) {
            errores[clave] = "Máximo 120 caracteres"
        }
    }

    for ((clave, texto) in mapOf("publicoAsociado" to estado.publicoAsociado, "observaciones" to estado.observaciones)) {
        if (texto.length > 2000) errores[clave] = "Máximo 2000 caracteres"
    }
    if (!validarSitioWeb(estado.sitioWeb)) errores["sitioWeb"] = "Usa una dirección http:// o https:// válida"

    if (esContacto) {
        // Un contacto pertenece a una organización ya registrada; 0 significa sin elegir.
        if (estado.organizacionId <= 0) {
            errores["organizacion"] = "Selecciona una organización"
        }
        if (!validarCorreo(estado.correo)) {
            errores["correo"] = "Correo inválido"
        }
        if (!validarTelefono(estado.telefono)) {
            errores["telefono"] = "Teléfono inválido (8 a 15 dígitos)"
        }
    }
    return errores
}

/** Dirección opcional, sin abrir internet ni comprobar que un sitio ficticio exista. */
fun validarSitioWeb(valor: String): Boolean {
    if (valor.isBlank()) return true
    return try {
        val uri = java.net.URI(valor)
        uri.scheme in listOf("http", "https") && !uri.host.isNullOrBlank() && uri.userInfo == null
    } catch (_: Exception) { false }
}
