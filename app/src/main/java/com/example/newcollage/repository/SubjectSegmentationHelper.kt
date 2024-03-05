package com.example.newcollage.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.ColorInt
import com.example.newcollage.util.getImageBitmap
import com.example.newcollage.viewmodel.SegmentResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.FloatBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log


object SubjectSegmentationHelper {

    private val options = SubjectSegmenterOptions.Builder()
        .enableForegroundConfidenceMask()
        .build()

    private val segmenter = SubjectSegmentation.getClient(options)

    fun segment(context: Context, uri: Uri): Flow<SegmentResult<Bitmap>> = flow {
        emit(SegmentResult.Loading())
        val imageBitmap = getImageBitmap(context, uri) ?: let {
            emit(SegmentResult.Failed("Can't get bitmap from uri."))
            return@flow
        }

        getResult(imageBitmap)?.also { bitmap ->
            emit(SegmentResult.Success(bitmap))
        } ?: run {
            emit(SegmentResult.Failed("Segment result is null"))
        }
    }

    private suspend fun getResult(image: Bitmap) = suspendCoroutine {

        val inputImage = InputImage.fromBitmap(image, 0)

        segmenter.process(inputImage)
            .addOnSuccessListener { result ->
                // Resume the coroutine with the foreground Bitmap result on success
                val maskWidth = image.width
                val maskHeight = image.height
                result.foregroundConfidenceMask?.let { buffer ->
                    val colors = maskColorsFromByteBuffer(image, maskWidth, maskHeight, buffer)
                    val maskBitmap =
                        Bitmap.createBitmap(colors, maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
                    it.resume(maskBitmap)
                } ?: run {
                    it.resume(null)
                }
            }
            .addOnFailureListener { e ->
                // Resume the coroutine with an exception in case of failure
                e.printStackTrace()
                it.resume(null)
            }
    }


    @ColorInt
    private fun maskColorsFromByteBuffer(
        bitmap: Bitmap,
        maskWidth: Int,
        maskHeight: Int,
        floatBuffer: FloatBuffer
    ): IntArray {
        @ColorInt val colors = IntArray(maskWidth * maskHeight)
        bitmap.getPixels(colors, 0, maskWidth, 0, 0, maskWidth, maskHeight)
        for (i in 0 until maskWidth * maskHeight) {
            var currentBufferColor = floatBuffer.get()
//            if (currentBufferColor < 0.2f) currentBufferColor = 0.0f
            //映射一下
            val min = 0.2f
            val max = 0.9f
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
