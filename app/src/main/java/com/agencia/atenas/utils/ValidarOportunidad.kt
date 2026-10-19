package com.agencia.atenas.utils

import com.agencia.atenas.ui.CrearUiState
import com.agencia.atenas.ui.camposFormulario
import com.agencia.atenas.ui.valor

/*
 * Por qué esta función devuelve un mapa y no solamente true/false:
 * Un booleano solo indicaría que hay algún error. El mapa identifica qué campo falló y su mensaje.
 * CrearViewModel lo publica en CrearUiState.errores y CrearScreen consulta errores[campo.clave].
 * Campo recibe ese mensaje: error != null activa el rojo y supportingText dibuja la explicación.
 * Esta función solo calcula reglas. No muestra componentes, no navega y no escribe en Room.
 */


/**
 * Revisa el formulario antes de guardar. Cada error se asocia a la clave de su campo.
 * Un mapa vacío significa que el formulario pasó estas comprobaciones.
 * Los campos opcionales pueden quedar vacíos; si tienen contenido, se valida su formato.
 */
fun validarOportunidad(estado: CrearUiState): Map<String, String> {
    // Este mapa se construye localmente durante la validación. Una clave identifica un campo y su
    // valor es el mensaje que verá el usuario. No se escribe en Room: es información de presentación.
    val errores = mutableMapOf<String, String>()

    // Datos mínimos para registrar una oportunidad sin inventar los datos desconocidos.
    val camposObligatorios = listOf(
        "nombreOportunidad",
        "tipoOportunidad",
        "fechaInicio",
        "comuna",
        "region",
        "publicoEsperado",
        "fechaDeteccion"
    )
    camposObligatorios.forEach { clave ->
        if (estado.valor(clave).isBlank()) {
            errores[clave] = "Este campo es obligatorio"
        }
    }

    // Limita el texto libre; el nombre tiene un límite más corto.
    camposFormulario.forEach { campo ->
        if (estado.valor(campo.clave).length > 2000) {
            errores[campo.clave] = "Máximo 2000 caracteres"
        }
    }
    if (estado.nombreOportunidad.length > 120) {
        errores["nombreOportunidad"] = "Máximo 120 caracteres"
    }
    if (estado.prioridad !in listOf("Alta", "Media", "Baja")) {
        errores["prioridad"] = "Selecciona Alta, Media o Baja"
    }

    // Fechas.aIso devuelve null si el texto no representa una fecha real DD/MM/AAAA.
    val camposFecha = listOf("fechaDeteccion", "fechaInicio", "fechaTermino")
    camposFecha.forEach { clave ->
        if (estado.valor(clave).isNotBlank() && Fechas.aIso(estado.valor(clave)) == null) {
            errores[clave] = "Usa una fecha real DD/MM/AAAA"
        }
    }

    // En formato AAAA-MM-DD se pueden comparar las fechas como texto.
    val inicio = Fechas.aIso(estado.fechaInicio)
    val termino = Fechas.aIso(estado.fechaTermino)
    if (inicio != null && termino != null && termino < inicio) {
        errores["fechaTermino"] = "No puede ser anterior al inicio"
    }

    // Asistentes desconocidos se permiten. Si se ingresa una cantidad, debe ser entera.
    if (estado.cantidadAsistentes.isNotBlank()) {
        val cantidad = estado.cantidadAsistentes.trim().toIntOrNull()
        if (cantidad == null || cantidad < 0) {
            errores["cantidadAsistentes"] = "Ingresa un entero igual o mayor que cero"
        }
    }

    // Acepta punto o coma decimal, pero no números negativos, infinitos ni NaN.
    if (estado.costosReferenciales.isNotBlank()) {
        val costo = estado.costosReferenciales.replace(',', '.').toDoubleOrNull()
        if (costo == null || !costo.isFinite() || costo < 0) {
            errores["costosReferenciales"] = "Ingresa solo un costo numérico no negativo"
        }
    }

    // Estas funciones permiten el texto vacío porque ambos datos son opcionales.
    if (!validarCorreo(estado.correoContacto)) {
        errores["correoContacto"] = "Correo inválido"
    }
    if (!validarTelefono(estado.telefonoContacto)) {
        errores["telefonoContacto"] = "Teléfono inválido (8 a 15 dígitos)"
    }

    // Primero se comprueba el formato para acceder a horas[1] de manera segura.
    if (estado.horario.isNotBlank()) {
        val horas = estado.horario.split(" a ")
        val formatoValido = horas.size == 2 && horas.all { hora ->
            Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(hora)
        }
        if (!formatoValido) {
            errores["horario"] = "Usa HH:mm a HH:mm, por ejemplo 09:00 a 18:00"
        } else {
            // Sin fecha final, el evento termina el mismo día, igual que en Calendar.
            // Pasar medianoche exige escribir el día siguiente como fecha de término.
            val fechaFinal = termino ?: inicio
            if (inicio == fechaFinal && horas[1] <= horas[0]) {
                errores["horario"] = "El término debe ser posterior al inicio"
            }
        }
    }

    return errores
}
