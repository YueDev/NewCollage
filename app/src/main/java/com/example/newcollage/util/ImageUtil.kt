package com.example.newcollage.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget

fun loadBitmapSync(context: Context, uri: Uri, width: Int, height: Int) = try {
    val future: FutureTarget<Bitmap> =
        Glide.with(context).asBitmap().load(uri).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE).centerInside().submit(width, height)
    val bitmap = future.get()
    future.cancel(false)
    bitmap
} catch (e: Exception) {
    e.printStackTrace()
    null
}
