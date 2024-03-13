package com.example.newcollage.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.ColorInt
import com.example.newcollage.util.getImageBitmap
import com.example.newcollage.viewmodel.MyResult
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.FloatBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object SubjectSegmentationHelper {

    private val options = SubjectSegmenterOptions.Builder()
        .enableForegroundConfidenceMask()
        .build()

    private val segmenter = SubjectSegmentation.getClient(options)


    //这种没有长时间的持续回调用普通flow比较好
    fun downloadModule(context: Context): Flow<MyResult<Int>> = callbackFlow {
        trySend(MyResult.Loading())
        val moduleInstallClient = ModuleInstall.getClient(context)

        val listener = InstallStatusListener {
            when (it.installState) {
                ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED -> {
                    trySend(MyResult.Success(1))
                }

                ModuleInstallStatusUpdate.InstallState.STATE_FAILED -> {
                    trySend(MyResult.Failed("module install failed"))
                }
            }
        }

        moduleInstallClient.areModulesAvailable(segmenter)
            .addOnSuccessListener {
                if (it.areModulesAvailable()) {
                    //安装成功
                    trySend(MyResult.Success(0))
                    //也可以判断这个任务是否成功 再cancel
                    cancel()
                } else {
                    //请求安装
                    val request =
                        ModuleInstallRequest.newBuilder()
                            .addApi(segmenter)
                            .setListener(listener)
                            .build()
                    moduleInstallClient.installModules(request)
                }
            }.addOnFailureListener {
                it.printStackTrace()
                trySend(MyResult.Failed(it.message ?: "error but no message"))
            }

        awaitClose {
            moduleInstallClient.unregisterListener(listener)
        }

    }


    fun segment(context: Context, uri: Uri): Flow<MyResult<Bitmap>> = flow {
        emit(MyResult.Loading())
        val imageBitmap = getImageBitmap(context, uri) ?: let {
            emit(MyResult.Failed("Can't get bitmap from uri."))
            return@flow
        }

        getResult(imageBitmap)?.also { bitmap ->
            emit(MyResult.Success(bitmap))
        } ?: run {
            emit(MyResult.Failed("Segment result is null"))
        }
    }

    private suspend fun getResult(image: Bitmap) = suspendCancellableCoroutine {

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
//                    it.cancel()
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
