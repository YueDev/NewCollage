package com.example.newcollage.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
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