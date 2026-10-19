package com.agencia.atenas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agencia.atenas.data.*
import com.agencia.atenas.utils.Fechas
import com.agencia.atenas.utils.SesionActual
import com.agencia.atenas.utils.validarOportunidad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordina el formulario sin escribir SQL ni navegar.
 * Los catálogos y usuarios se relacionan por ID. Los nombres del UiState
 * son etiquetas temporales de pantalla y nunca se copian a la oportunidad guardada.
 */
class CrearViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.obtenerBaseDeDatos(application)
    private val repository = OportunidadRepository(db)
    private val catalogos = CatalogoRepository(db)

    // stateIn convierte los Flow de Room en StateFlow con una lista inicial vacía.
    // WhileSubscribed mantiene la consulta mientras la UI la observa y espera cinco
    // segundos antes de detenerla, para evitar reinicios por cambios breves de pantalla.
    val organizaciones = catalogos.organizaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val contactos = catalogos.contactos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(CrearUiState(
        responsable = SesionActual.usuario,
        fechaDeteccion = Fechas.visible(Fechas.hoy())
    ))
    val uiState = _uiState.asStateFlow()
    private var inicializado = false
    private var anterior: OportunidadDetalle? = null

    fun cargar(id: Int) {
        // Evitar que una recomposición vuelva a cargar y borre cambios aún sin guardar.
        if (inicializado) return
        inicializado = true
        if (id == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val detalle = repository.obtener(id)
                anterior = detalle
                _uiState.value = crearEstadoDeEdicion(detalle)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensaje = e.message ?: "No se pudo cargar") }
            }
        }
    }

    // El detalle sale de JOIN y trae las etiquetas actuales de las tablas relacionadas.
    // Convertirlas al UiState sirve para mostrarlas; los vínculos se conservan por ID.
    private fun crearEstadoDeEdicion(detalle: OportunidadDetalle): CrearUiState {
        return CrearUiState(
            id = detalle.id,
            organizacionId = detalle.organizacionId,
            contactoId = detalle.contactoId,
            nombreOportunidad = detalle.nombreOportunidad,
            tipoOportunidad = detalle.tipoOportunidad,
            descripcion = detalle.descripcion,
            historialObservaciones = detalle.historialObservaciones,
            fuenteInformacion = detalle.fuenteInformacion,
            fechaDeteccion = Fechas.visible(detalle.fechaDeteccion),
            responsable = detalle.responsable,
            prioridad = detalle.prioridad,
            potencialComercial = detalle.potencialComercial,
            fechaInicio = Fechas.visible(detalle.fechaInicio),
            fechaTermino = Fechas.visible(detalle.fechaTermino),
            horario = detalle.horario,
            ubicacionGeneral = detalle.ubicacionGeneral,
            comuna = detalle.comuna,
            region = detalle.region,
            publicoEsperado = detalle.publicoEsperado,
            publicoObjetivo = detalle.publicoObjetivo,
            cantidadAsistentes = detalle.cantidadAsistentes?.toString().orEmpty(),
            condicionesParticipacion = detalle.condicionesParticipacion,
            requisitos = detalle.requisitos,
            espaciosDisponibles = detalle.espaciosDisponibles,
            costosReferenciales = detalle.costosReferenciales,
            nombreOrganizacion = detalle.nombreOrganizacion,
            tipoOrganizacion = detalle.tipoOrganizacion,
            rubroOrganizacion = detalle.rubroOrganizacion,
            nombreContacto = detalle.nombreContacto,
            cargoContacto = detalle.cargoContacto,
            areaContacto = detalle.areaContacto,
            correoContacto = detalle.correoContacto,
            telefonoContacto = detalle.telefonoContacto,
            medioContactoPreferente = detalle.medioContactoPreferente
        )
    }

    fun cambiar(clave: String, valor: String) {
        if (_uiState.value.guardando) return
        _uiState.update { actual ->
            val nuevo = when (clave) {
                "nombreOportunidad" -> actual.copy(nombreOportunidad = valor)
                "tipoOportunidad" -> actual.copy(tipoOportunidad = valor)
                "descripcion" -> actual.copy(descripcion = valor)
                "historialObservaciones" -> actual.copy(historialObservaciones = valor)
                "fuenteInformacion" -> actual.copy(fuenteInformacion = valor)
                "fechaDeteccion" -> actual.copy(fechaDeteccion = valor)
                "responsable" -> actual.copy(responsable = valor)
                "prioridad" -> actual.copy(prioridad = valor)
                "potencialComercial" -> actual.copy(potencialComercial = valor)
                "fechaInicio" -> actual.copy(fechaInicio = valor)
                "fechaTermino" -> actual.copy(fechaTermino = valor)
                "horario" -> actual.copy(horario = valor)
                "ubicacionGeneral" -> actual.copy(ubicacionGeneral = valor)
                "comuna" -> actual.copy(comuna = valor)
                "region" -> actual.copy(region = valor)
                "publicoEsperado" -> actual.copy(publicoEsperado = valor)
                "publicoObjetivo" -> actual.copy(publicoObjetivo = valor)
                "cantidadAsistentes" -> actual.copy(cantidadAsistentes = valor)
                "condicionesParticipacion" -> actual.copy(condicionesParticipacion = valor)
                "requisitos" -> actual.copy(requisitos = valor)
                "espaciosDisponibles" -> actual.copy(espaciosDisponibles = valor)
                "costosReferenciales" -> actual.copy(costosReferenciales = valor)
                "nombreOrganizacion" -> actual.copy(nombreOrganizacion = valor)
                "tipoOrganizacion" -> actual.copy(tipoOrganizacion = valor)
                "rubroOrganizacion" -> actual.copy(rubroOrganizacion = valor)
                "nombreContacto" -> actual.copy(nombreContacto = valor)
                "cargoContacto" -> actual.copy(cargoContacto = valor)
                "areaContacto" -> actual.copy(areaContacto = valor)
                "correoContacto" -> actual.copy(correoContacto = valor)
                "telefonoContacto" -> actual.copy(telefonoContacto = valor)
                "medioContactoPreferente" -> actual.copy(medioContactoPreferente = valor)
                else -> actual
            }
            // copy no modifica la instancia anterior. Publica otra conservando los demás
            // valores. Quitar solo esta clave limpia el error del campo que se está editando.
            nuevo.copy(errores = actual.errores - clave, mensaje = "")
        }
    }

    // Métodos del primer ejercicio: delegan en el mismo mecanismo de actualización.
    fun cambiarNombre(valor: String) = cambiar("nombreOportunidad", valor)
    fun cambiarDescripcion(valor: String) = cambiar("descripcion", valor)

    fun elegirOrganizacion(org: OrganizacionEntity) {
        if (_uiState.value.guardando) return
        _uiState.update {
            // Al cambiar de organización hay que retirar el contacto anterior; de otro
            // modo podría quedar seleccionada una persona de otra organización.
            it.copy(
                organizacionId = org.id,
                nombreOrganizacion = org.nombre,
                tipoOrganizacion = org.tipo,
                rubroOrganizacion = org.rubro,
                contactoId = null,
                nombreContacto = "", cargoContacto = "", areaContacto = "",
                correoContacto = "", telefonoContacto = "", medioContactoPreferente = ""
            )
        }
    }

    fun elegirContacto(contacto: ContactoEntity) {
        if (_uiState.value.guardando) return
        if (contacto.organizacionId != _uiState.value.organizacionId) return
        _uiState.update {
            it.copy(
                contactoId = contacto.id,
                nombreContacto = contacto.nombre,
                cargoContacto = contacto.cargo,
                areaContacto = contacto.area,
                correoContacto = contacto.correo,
                telefonoContacto = contacto.telefono,
                medioContactoPreferente = contacto.medio
            )
        }
    }

    fun validarFormulario(): Boolean {
        val errores = validarOportunidad(_uiState.value)
        _uiState.update { it.copy(errores = errores) }
        return errores.isEmpty()
    }

    fun guardar() {
        if (_uiState.value.guardando || _uiState.value.cargando) return
        if (!validarFormulario()) return
        val estado = _uiState.value
        _uiState.update { it.copy(guardando = true, mensaje = "") }

        // Room es suspendido. La corrutina espera el resultado sin bloquear la interfaz.
        // Solo se publica guardadoId tras el éxito; la pantalla lo observa para navegar.
        viewModelScope.launch {
            try {
                val registro = crearRegistro(estado)
                val id = repository.guardar(registro, estado.contactoId)
                _uiState.update { it.copy(guardando = false, guardadoId = id) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // No se borra el borrador: el usuario conserva sus datos para reintentar.
                _uiState.update {
                    it.copy(guardando = false, mensaje = e.message ?: "No se pudo guardar")
                }
            }
        }
    }

    private fun crearRegistro(estado: CrearUiState): GestionOportunidadEntity {
        // La validación ya verificó las fechas. En Room se guarda ISO para ordenarlas.
        // cantidadAsistentes vacío se transforma en null: desconocido es distinto de cero.
        // No hay argumentos nombreOrganizacion/correoContacto: viven en sus propias tablas.
        return GestionOportunidadEntity(
            id = estado.id,
            nombreOportunidad = estado.nombreOportunidad.trim(),
            tipoOportunidad = estado.tipoOportunidad.trim(),
            descripcion = estado.descripcion.trim(),
            fuenteInformacion = estado.fuenteInformacion.trim(),
            fechaDeteccion = Fechas.aIso(estado.fechaDeteccion).orEmpty(),
            prioridad = estado.prioridad.trim(),
            potencialComercial = estado.potencialComercial.trim(),
            fechaInicio = Fechas.aIso(estado.fechaInicio).orEmpty(),
            fechaTermino = Fechas.aIso(estado.fechaTermino).orEmpty(),
            horario = estado.horario.trim(),
            comuna = estado.comuna.trim(),
            region = estado.region.trim(),
            publicoEsperado = estado.publicoEsperado.trim(),
            cantidadAsistentes = estado.cantidadAsistentes.trim().toIntOrNull(),
            condicionesParticipacion = estado.condicionesParticipacion.trim(),
            costosReferenciales = estado.costosReferenciales.trim(),
            estado = anterior?.estado ?: EstadoOportunidad.IDENTIFICADO,
            // La columna conserva su nombre histórico; el historial de gestiones está en Seguimiento.
            historialObservaciones = estado.historialObservaciones.trim(),
            ubicacionGeneral = estado.ubicacionGeneral.trim(),
            publicoObjetivo = estado.publicoObjetivo.trim(),
            requisitos = estado.requisitos.trim(),
            espaciosDisponibles = estado.espaciosDisponibles.trim(),
            fechaUltimaActualizacion = Fechas.ahora(),
            responsableId = anterior?.responsableId ?: checkNotNull(SesionActual.usuarioId),
            organizacionId = estado.organizacionId
        )
    }
}
