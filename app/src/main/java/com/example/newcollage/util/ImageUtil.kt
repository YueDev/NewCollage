package com.example.newcollage.util

import android.content.Context
import android.content.Intent
import android.content.Intent.createChooser
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat.startActivity
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowConversionToBitmap
import coil3.request.allowHardware
import coil3.size.Scale
import coil3.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

fun loadBitmapSync(context: Context, uri: Uri, width: Int, height: Int) = try {
    val future: FutureTarget<Bitmap> =
        Glide.with(context).asBitmap()
            .load(uri)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerInside()
            .submit(width, height)
    val bitmap = future.get()
    future.cancel(false)
    bitmap
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun loadBitmapSync(context: Context, resId: Int, width: Int, height: Int) = try {
    val future: FutureTarget<Bitmap> =
        Glide.with(context).asBitmap()
            .load(resId)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerInside()
            .submit(width, height)
    val bitmap = future.get()
    future.cancel(false)
    bitmap
} catch (e: Exception) {
    e.printStackTrace()
    null
}


fun pointInImage(x: Float, y: Float, imageBitmap: Bitmap, imageMatrix: Matrix): Boolean {
    val matrix = Matrix()
    val b = imageMatrix.invert(matrix)
    if (!b) return false
    val array = floatArrayOf(x, y)
    matrix.mapPoints(array)
    return array[0] > 0f && array[1] > 0f && array[0] < imageBitmap.width && array[1] < imageBitmap.height
}

// uri获取bitmap 走的coil
suspend fun getImageBitmap(context: Context, uri: Uri): Bitmap? {
    val imageLoader = ImageLoader(context)
    val imageRequest = ImageRequest.Builder(context)
        .data(uri)
        .size(2048)
        .scale(Scale.FIT)
        .allowConversionToBitmap(true)
        .allowHardware(false)
        .build()
    val imageResult = imageLoader.execute(imageRequest)
    return imageResult.image?.toBitmap()
}

suspend fun getImageBitmap(context: Context, drawableResId: Int): Bitmap? {
    val imageLoader = ImageLoader(context)
    val imageRequest = ImageRequest.Builder(context)
        .data(drawableResId)
        .size(2048)
        .scale(Scale.FIT)
        .allowConversionToBitmap(true)
        .allowHardware(false)
        .build()
    val imageResult = imageLoader.execute(imageRequest)
    return imageResult.image?.toBitmap()
}



// 保存bitmap
// https://github.com/android/snippets/blob/main/compose/snippets/src/main/java/com/example/compose/snippets/graphics/AdvancedGraphicsSnippets.kt#L229
suspend fun Bitmap.saveToDisk(context: Context, fileName:String, isPng: Boolean = false): Uri {
    val ext = if (isPng) "png" else "jpg"
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "${fileName}.${ext}"
    )
    val format = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
    //bitmap 写入到文件
    file.writeBitmap(this, format, 100)
    //插入媒体库 的到uri
    val mimeType = if (isPng) "image/png" else "image/jpeg"
    return scanFilePath(context, file.path, mimeType) ?: throw Exception("File could not be saved")
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

private suspend fun scanFilePath(context: Context, filePath: String, mimeType: String): Uri? {
    return suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf(mimeType)
        ) { _, scannedUri ->
            if (scannedUri == null) {
                continuation.cancel(Exception("File $filePath could not be scanned"))
            } else {
                continuation.resume(scannedUri)
            }
        }
    }
}

//分享image uri
fun shareImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
//        type = if (isPng) "image/png" else "image/jpeg"
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(context, createChooser(intent, "Share your image"), null)
}

