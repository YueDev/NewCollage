package com.example.newcollage.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget

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


fun pointInImage(x: Float, y: Float, imageBitmap:Bitmap, imageMatrix: Matrix): Boolean {
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
    return (imageResult.drawable as? BitmapDrawable)?.bitmap
}