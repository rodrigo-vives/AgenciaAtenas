package com.agencia.atenas.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Lee una vista previa reducida y orientada. El original permanece intacto en disco.
 * Se llama desde Dispatchers.IO: leer/decodificar una foto puede tardar y no debe
 * detener los botones ni las animaciones. null permite mostrar un error sin cerrar la app.
 */
fun cargarVistaPrevia(ruta: String): ImageBitmap? {
    return try {
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, opciones)
        var muestra = 1
        while (maxOf(opciones.outWidth, opciones.outHeight) / muestra > 1200) muestra *= 2
        val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestra })
        // Algunas cámaras guardan el giro en EXIF en vez de rotar los píxeles.
        // Aplicamos esa orientación para que una foto vertical no aparezca acostada.
        bitmap?.let { original ->
            val orientacion = runCatching { ExifInterface(ruta).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }.getOrDefault(1)
            val matriz = Matrix()
            when (orientacion) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matriz.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matriz.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matriz.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matriz.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matriz.preScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { matriz.postRotate(90f); matriz.postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_TRANSVERSE -> { matriz.postRotate(270f); matriz.postScale(-1f, 1f) }
            }
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true).asImageBitmap()
        }
    } catch (_: Exception) {
        // Una imagen ilegible no invalida la oportunidad ni las otras fotografías.
        null
    }
}
