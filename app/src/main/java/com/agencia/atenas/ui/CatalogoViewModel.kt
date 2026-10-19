package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.*
import com.agencia.atenas.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/*
 * Un contacto exige una organización ya registrada. El selector entrega su id, que se guarda
 * como organizacionId en Room. Las listas se obtienen con stateIn: transforma el Flow de Room
 * en un StateFlow con lista vacía inicial. WhileSubscribed(5000) conserva la observación cinco
 * segundos después de perder observadores; evita reinicios breves innecesarios al cambiar la UI.
 * Al guardar con éxito se limpia el borrador; al fallar se mantiene para corregir y reintentar.
 */


/**
 * Mantiene los campos del catálogo, comprueba sus reglas y pide guardar al Repository.
 * Las listas se observan desde Room para mostrar registros nuevos después de guardarlos.
 */

// CAMBIO: cada destino obtiene su propia instancia; comparten solo Repository/Room.
class CatalogoViewModel(application: Application) : AndroidViewModel(application) {
    // Repository concentra las operaciones y las comprobaciones de acceso.
    private val repository = CatalogoRepository(AppDatabase.obtenerBaseDeDatos(application))
    val organizaciones = repository.organizaciones().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList())
    val contactos = repository.contactos().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList())
    // Estado modificable solo desde esta clase. La pantalla recibe la versión de lectura.
    // MutableStateFlow contiene el último estado. El prefijo _ distingue la versión interna modificable.
    // La pantalla no recibe este objeto directamente: recibe asStateFlow(), que permite observarlo.
    // Se usa para que las modificaciones pasen por funciones del ViewModel y sean fáciles de seguir.
    private val _uiState = MutableStateFlow(CatalogoUiState())
    // La UI observa este flujo; los cambios provocan la recomposición necesaria.
    // asStateFlow no copia los datos ni los guarda en disco: expone el mismo flujo como solo lectura.
    // La persistencia la hace Room; este estado solo mantiene la información de la pantalla en memoria.
    val uiState = _uiState.asStateFlow()

    // Recibe la clave de un campo y su texto nuevo. copy conserva el resto del estado.
    // Solo se limpia el error del campo editado; no se pierde el contenido de otros campos.
    fun cambiar(clave: String,
        valor: String) {
        if (_uiState.value.guardando) return
        // update recibe el estado actual y publica el objeto que devuelve el bloque. copy genera una nueva
        // instancia con los campos indicados y conserva los demás. Las propiedades val no se reasignan
        // dentro del objeto anterior. Este nuevo estado es el que observa la interfaz.
        _uiState.update {
            val nuevo = when (clave) {
                "nombre" -> it.copy(nombre = valor)
                "tipo" -> it.copy(tipo = valor)
                "rubro" -> it.copy(rubro = valor)
                "organizacion" -> it.copy(organizacionId = valor.toIntOrNull() ?: 0)
                "cargo" -> it.copy(cargo = valor)
                "area" -> it.copy(area = valor)
                "correo" -> it.copy(correo = valor)
                "telefono" -> it.copy(telefono = valor)
                "medio" -> it.copy(medio = valor)
                "comuna" -> it.copy(comuna = valor)
                "region" -> it.copy(region = valor)
                "sitioWeb" -> it.copy(sitioWeb = valor)
                "publicoAsociado" -> it.copy(publicoAsociado = valor)
                "observaciones" -> it.copy(observaciones = valor)
                else -> it
            }
            // errores es un mapa: clave del campo → mensaje. Restar una clave crea un mapa sin ese error.
            // Se limpia únicamente el campo editado; las validaciones completas se repiten al guardar.
            nuevo.copy(errores = it.errores - clave,
                mensaje = "")
        }
    }

    // Valida primero y evita dobles pulsaciones mientras se guarda. Solo comunica éxito
    // cuando termina la escritura; si falla, conserva lo que el usuario había ingresado.
    fun guardar(esContacto: Boolean) {
        // Se lee el borrador actual una vez para validar y guardar exactamente esos valores.
        // Mientras se escribe en Room se deshabilitan las ediciones para evitar envíos simultáneos.
        val estadoFormulario = _uiState.value
        if (estadoFormulario.guardando) return
        // Como en el PPT de validaciones: el ViewModel coordina; la función externa
        // revisa formatos y devuelve los mensajes para cada campo del formulario.
        val errores = validarCatalogo(estadoFormulario, esContacto)
        _uiState.update {
            it.copy(errores = errores)
        }
        // return interrumpe esta función antes de escribir datos. Como solo se actualizaron los errores,
        // los valores que el usuario había ingresado continúan en el estado.
        if (errores.isNotEmpty()) return
        _uiState.update {
            it.copy(guardando = true)
        }
        // launch inicia una corrutina vinculada al ViewModel. Permite llamar funciones suspend de Room
        // sin bloquear la interfaz esperando su resultado. No crea por sí sola un hilo de trabajo nuevo;
        // Room administra la ejecución de sus consultas suspendidas. Al destruir el ViewModel se cancela.
        viewModelScope.launch {
            try {
                if (esContacto) {
                    repository.guardarContacto(
                        orgId = estadoFormulario.organizacionId,
                        nombre = estadoFormulario.nombre,
                        cargo = estadoFormulario.cargo,
                        area = estadoFormulario.area,
                        correo = estadoFormulario.correo,
                        telefono = estadoFormulario.telefono,
                        medio = estadoFormulario.medio
                    )
                } else {
                    repository.guardarOrganizacion(
                        nombre = estadoFormulario.nombre,
                        tipo = estadoFormulario.tipo,
                        rubro = estadoFormulario.rubro,
                        comuna = estadoFormulario.comuna,
                        region = estadoFormulario.region,
                        sitioWeb = estadoFormulario.sitioWeb,
                        publicoAsociado = estadoFormulario.publicoAsociado,
                        observaciones = estadoFormulario.observaciones
                    )
                }
                // Limpiar solo después de que Room confirme el guardado.
                _uiState.value = CatalogoUiState(mensaje = "Registro guardado correctamente")
            } catch (e: CancellationException) {
                // Una cancelación al salir no representa un fallo de datos. Se propaga
                // para respetar el ciclo de vida del ViewModel.
                throw e
            } catch (e: Exception) {
                // Se conserva el borrador para corregir o reintentar; solo cambia el mensaje.
                _uiState.update {
                    it.copy(guardando = false,
                        esError = true, mensaje = e.message ?: "No se pudo guardar")
                }
            }
        }
    }
}
