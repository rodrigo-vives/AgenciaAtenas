package com.agencia.atenas

import com.agencia.atenas.ui.CatalogoUiState
import com.agencia.atenas.utils.validarCatalogo
import org.junit.Assert.*
import org.junit.Test

class ValidarCatalogoTest {
    @Test fun organizacionSoloExigeNombreYLimitaLongitudes() {
        assertTrue(validarCatalogo(CatalogoUiState(nombre = "Organización ficticia"), false).isEmpty())
        assertTrue(validarCatalogo(CatalogoUiState(), false).containsKey("nombre"))
        assertTrue(validarCatalogo(CatalogoUiState(nombre = "A", rubro = "x".repeat(121)), false).containsKey("rubro"))
    }

    @Test fun contactoRequiereOrganizacionValida() {
        assertTrue(validarCatalogo(CatalogoUiState(nombre = "Contacto", organizacionId = -1), true).containsKey("organizacion"))
        assertTrue(validarCatalogo(CatalogoUiState(nombre = "Contacto", organizacionId = 1), true).isEmpty())
    }

    @Test fun contactoNoAceptaCorreoNiTelefonoConFormatoInvalido() {
        val errores = validarCatalogo(CatalogoUiState(nombre = "Contacto", organizacionId = 1,
            correo = "no-es-correo", telefono = "abc"), true)
        assertTrue(errores.containsKey("correo"))
        assertTrue(errores.containsKey("telefono"))
    }

    @Test fun validarNoBorraLosDatosDelBorrador() {
        val borrador = CatalogoUiState(nombre = "Contacto", correo = "incorrecto")
        validarCatalogo(borrador, true)
        assertEquals("Contacto", borrador.nombre)
        assertEquals("incorrecto", borrador.correo)
    }

    @Test fun validaSitioWebYTextosComplementarios() {
        val correcto = CatalogoUiState(nombre = "Organización", sitioWeb = "https://example.invalid")
        assertTrue(validarCatalogo(correcto, false).isEmpty())
        assertTrue(validarCatalogo(correcto.copy(sitioWeb = "javascript:alert(1)"), false).containsKey("sitioWeb"))
        assertTrue(validarCatalogo(correcto.copy(observaciones = "x".repeat(2001)), false).containsKey("observaciones"))
    }
}
