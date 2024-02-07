package com.example.newcollage.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.Coil
import coil.ImageLoader
import coil.executeBlocking
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import coil.util.CoilUtils
import com.bumptech.glide.util.Util
import com.example.newcollage.viewmodel.SegmentResult
import com.permissionx.guolindev.dialog.allSpecialPermissions
import kotlinx.coroutines.flow.flow

class SegmentRepository {

    fun segment(contex: Context, uri: Uri) = flow<SegmentResult<Bitmap>> {
        emit(SegmentResult.Loading())
        val imageBitmap = getImageBitmap(contex, uri) ?: let {
            emit(SegmentResult.Failed("Can't get bitmap from uri."))
            return@flow
        }


    }

    // uri获取bitmap 走的coil
    private suspend fun getImageBitmap(context: Context, uri: Uri): Bitmap?{
        val imageLoader = ImageLoader(context)
        val imageRequest = ImageRequest.Builder(context)
            .data(uri)
            .size(1024)
            .scale(Scale.FIT)
            .allowConversionToBitmap(true)
            .build()
        val imageResult = imageLoader.execute(imageRequest)
        return (imageResult.drawable as? BitmapDrawable)?.bitmap
    }

    //TODO 分割bitmap


}