package com.agencia.atenas.data

import android.content.Context
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Las fotos son archivos, no texto ni bitmaps dentro de SQLite.
 * filesDir es almacenamiento privado persistente: cerrar la app no borra las fotos.
 * Cada carpeta usa el ID de una oportunidad; no depende de nombres que se pueden repetir.
 * Consultamos Room antes de cada operación para aplicar los mismos permisos del detalle.
 */
class EvidenciaRepository(
    context: Context,
    db: AppDatabase = AppDatabase.obtenerBaseDeDatos(context),
    private val raiz: File = File(context.filesDir, "evidencias")
) {
    // Los valores por defecto son los de la app. Las pruebas pueden usar su propia
    // base y carpeta para comprobar persistencia sin tocar las fotos del usuario.
    private val oportunidades = OportunidadRepository(db)

    private fun carpeta(id: Int) = File(raiz, id.toString())

    suspend fun listar(id: Int): List<String> = withContext(Dispatchers.IO) {
        oportunidades.obtener(id)
        // Solo se muestran capturas confirmadas. Una cámara cancelada no deja evidencia.
        carpeta(id).listFiles().orEmpty()
            .filter { it.extension == "jpg" && it.length() > 0 }
            .sortedByDescending { it.lastModified() }.map { it.absolutePath }
    }

    suspend fun preparar(id: Int): String = withContext(Dispatchers.IO) {
        oportunidades.obtener(id)
        val directorio = carpeta(id)
        check(directorio.isDirectory || directorio.mkdirs()) { "No se pudo crear la carpeta de fotos" }
        // UUID evita sobrescribir una foto anterior. pending indica captura aún sin confirmar.
        File(directorio, "${UUID.randomUUID()}.pending").apply {
            check(createNewFile()) { "No se pudo preparar la fotografía" }
        }.absolutePath
    }

    suspend fun finalizar(id: Int, ruta: String, capturada: Boolean) = withContext(Dispatchers.IO) {
        val archivo = File(ruta)
        // El callback solo puede operar dentro de la carpeta que corresponde a este ID.
        require(archivo.canonicalFile.parentFile == carpeta(id).canonicalFile && archivo.extension == "pending")
        if (!capturada) {
            archivo.delete()
            return@withContext
        }
        try {
            oportunidades.obtener(id)
            check(archivo.length() > 0) { "La cámara no entregó una fotografía" }
            check(archivo.renameTo(File(archivo.parentFile, "${archivo.nameWithoutExtension}.jpg"))) {
                "No se pudo conservar la fotografía"
            }
        } catch (e: Exception) {
            archivo.delete()
            throw e
        }
    }
}
