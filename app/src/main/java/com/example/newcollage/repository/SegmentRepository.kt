package com.example.newcollage.repository

import android.content.Context
import android.graphics.Bitmap
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
        bitmap: Bitmap,
        maskWidth: Int,
        maskHeight: Int,
        byteBuffer: ByteBuffer
    ): IntArray {
        @ColorInt val colors = IntArray(maskWidth * maskHeight)
        bitmap.getPixels(colors, 0, maskWidth, 0, 0, maskWidth, maskHeight)
        for (i in 0 until maskWidth * maskHeight) {
            var currentBufferColor = byteBuffer.float
//            if (currentBufferColor < 0.2f) currentBufferColor = 0.0f
            //映射一下
            val min = 0.2f
            val max = 0.8f
            currentBufferColor = if (currentBufferColor <= min) {
                0f
            } else if (currentBufferColor >= max) {
                1.0f
            } else {
                //min max之间映射成0.0 - 1.0
                (currentBufferColor - min) / (max - min)
            }

            val alpha = (255 * currentBufferColor).toInt()
            colors[i] = (colors[i] and 0x00FFFFFF) or (alpha shl 24)

        }
        return colors
    }
}