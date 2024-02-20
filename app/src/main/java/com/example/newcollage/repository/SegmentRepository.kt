package com.example.newcollage.repository

import android.R.color
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.ColorInt
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.example.newcollage.viewmodel.SegmentResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import kotlin.coroutines.resume


class SegmentRepository {

    fun segment(context: Context, uri: Uri) = flow {
        emit(SegmentResult.Loading())
        val imageBitmap = getImageBitmap(context, uri) ?: let {
            emit(SegmentResult.Failed("Can't get bitmap from uri."))
            return@flow
        }
        val result = segmentBitmap(imageBitmap)
        emit(result)
    }

    // uri获取bitmap 走的coil
    private suspend fun getImageBitmap(context: Context, uri: Uri): Bitmap? {
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

    private suspend fun segmentBitmap(bitmap: Bitmap): SegmentResult<Bitmap> {
        val option = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
//            .enableRawSizeMask()
            .build()
        val segmenter = Segmentation.getClient(option)
        val input = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { continuation ->
            segmenter.process(input)
                .addOnSuccessListener { results ->
                    val maskWidth = results.width
                    val maskHeight = results.height
                    val buffer = results.buffer
                    val colors = maskColorsFromByteBuffer(bitmap, maskWidth, maskHeight, buffer)
                    val maskBitmap =
                        Bitmap.createBitmap(colors, maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
                    continuation.resume(SegmentResult.Success(maskBitmap))
                }
                .addOnFailureListener { e ->
                    val errorMessage = e.localizedMessage ?: "segment error"
                    continuation.resume(SegmentResult.Failed(errorMessage))
                }
        }
    }


    /** Converts byteBuffer floats to ColorInt array that can be used as a mask.
     *  From https://github.com/googlesamples/mlkit/blob/master/android/vision-quickstart/app/src/main/java/com/google/mlkit/vision/demo/kotlin/segmenter/SegmentationGraphic.kt
     * */
    @ColorInt
    private fun maskColorsFromByteBuffer(
        bitmap:Bitmap,
        maskWidth: Int,
        maskHeight: Int,
        byteBuffer: ByteBuffer
    ): IntArray {
        @ColorInt val colors = IntArray(maskWidth * maskHeight)
        bitmap.getPixels(colors, 0, maskWidth, 0, 0, maskWidth, maskHeight)
        for (i in 0 until maskWidth * maskHeight) {
            var backgroundLikelihood = byteBuffer.float
            if (backgroundLikelihood < 0.2f) backgroundLikelihood = 0.0f
            val alpha = (255 * backgroundLikelihood).toInt()
            colors[i] = (colors[i] and 0x00FFFFFF) or (alpha shl 24)
//            if (backgroundLikelihood > 0.9) {
//                val alpha = 255
//                colors[i] = (colors[i] and 0x00FFFFFF) or (alpha shl 24)
//            } else if (backgroundLikelihood > 0.2) {
//                // Linear interpolation to make sure when backgroundLikelihood is 0.2, the alpha is 0 and
//                // when backgroundLikelihood is 0.9, the alpha is 128.
//                // +0.5 to round the float value to the nearest int.
//                val alpha = (182.9 * backgroundLikelihood - 36.6 + 0.5).toInt()
//                colors[i] = (colors[i] and 0x00FFFFFF) or (alpha shl 24)
//            } else {
//                val alpha = 0
//                colors[i] = (colors[i] and 0x00FFFFFF) or (alpha shl 24)
//            }
        }
        return colors
    }
}