/**
 * Copyright (c) 2019 by Roman Sisik. All rights reserved.
 */
package backgounderaser.photoeditor.pictureart.magic.dynamic.media

import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.nio.ByteOrder
import java.security.InvalidParameterException
import kotlin.math.log


/**
 * This class converts audio from various audio formats that are supported by Android's decoders into
 * m4a/aac audio.
 * It is based on the examples from Android's CTS. For more information, please see
 * https://android.googlesource.com/platform/cts/+/jb-mr2-release/tests/tests/media/src/android/media/cts/DecodeEditEncodeTest.java
 */
class AudioTrackToAacConvertor {

    //输出比特率96KHz
    private val outBitRate = 96000

    //输出采样率44.1K
    private val outSampleSize = 44100

    var extractor: MediaExtractor? = null
    var muxer: MediaMuxer? = null
    var decoder: MediaCodec? = null
    var encoder: MediaCodec? = null

    val timeoutUs = 0 * 1000L
    val bufferInfo = MediaCodec.BufferInfo()
    var trackIndex = -1
    var outPath: String? = null


    /**
     * inPath：输入音乐的路径
     * outPath：裁切后输出的路径
     * startTimeMillis: 开始时间 毫秒
     * durationMillis: 持续时间 毫秒
     * fadeInDurationMillis： 渐入时间 毫秒
     * fadeOutDurationMillis： 渐出世间 毫秒
     *
     */


    fun convertToAAC(
            inPath: String, outPath: String, startTimeMillis: Int = -1, durationMillis: Int = -1,
            fadeInDurationMillis: Int = -1, fadeOutDurationMillis: Int = -1, converListener: ConverListener
    ) {
        this.converListener = converListener
        extractor = MediaExtractor()
        extractor!!.setDataSource(inPath)
        this.outPath = outPath
        muxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        Thread {
            convert(startTimeMillis, durationMillis, fadeInDurationMillis, fadeOutDurationMillis, converListener)
        }.start()
    }


    //时间都是毫秒ms
    //渐入渐出比较耗时
    fun convertToAAC(
            inAFd: AssetFileDescriptor, outFile: String, startTimeMillis: Int = -1, durationMillis: Int = -1,
            fadeInDurationMillis: Int = -1, fadeOutDurationMillis: Int = -1
    ) {

        extractor = MediaExtractor()
        extractor!!.setDataSource(inAFd.fileDescriptor, inAFd.startOffset, inAFd.length)

        // Init muxer
        muxer = MediaMuxer(outFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        convert(startTimeMillis, durationMillis, fadeInDurationMillis, fadeOutDurationMillis, converListener)
    }


    private fun convert(
            startTimeMillis: Int = -1,
            durationMillis: Int = -1,
            fadeInDurationMillis: Int = -1,
            fadeOutDurationMillis: Int = -1,
            converListener: ConverListener?
    ) {
        try {

            val inFormat = selectAudioTrack(extractor!!)
            initCodecs(inFormat)

            var allInputExtracted = false
            var allInputDecoded = false
            var allOutputEncoded = false

            // This will determine the total duration of output file
            val startTimeUS = (startTimeMillis * 1000L).coerceAtLeast(0L)

            val endTimeUS = if (durationMillis <= 0) {
                inFormat.getLong(MediaFormat.KEY_DURATION)
            } else {
                startTimeUS + durationMillis * 1000L
            }

            while (!allOutputEncoded) {

                // Feed input to decoder
                if (!allInputExtracted) {

                    val sampleTime = extractor!!.sampleTime

                    if (sampleTime < 0) {
                        //应该是解码失败，做下错误处理
                    }

                    if (sampleTime < startTimeUS) {
                        extractor!!.advance()
                        continue
                    }

                    val inBufferId = decoder!!.dequeueInputBuffer(timeoutUs)
                    if (inBufferId >= 0) {

                        val buffer = decoder?.getInputBuffer(inBufferId) ?: return
                        val sampleSize = extractor!!.readSampleData(buffer, 0)

                        if (sampleSize >= 0 && endTimeUS > sampleTime) {
                            decoder!!.queueInputBuffer(
                                    inBufferId, 0, sampleSize,
                                    sampleTime, extractor!!.sampleFlags
                            )

                            //可以做进度回调
                            Log.d(
                                    "YUEDEVTAG",
                                    "time==$sampleTime  progress==${(sampleTime - startTimeUS) * 100 / (endTimeUS - startTimeUS)}"
                            )
                            extractor!!.advance()
                        } else {
                            decoder!!.queueInputBuffer(
                                    inBufferId, 0, 0,
                                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            allInputExtracted = true
                        }
                    }
                }



                var encoderOutputAvailable = true
                var decoderOutputAvailable = !allInputDecoded

                while (encoderOutputAvailable || decoderOutputAvailable) {
                    // Drain Encoder & mux first
                    val outBufferId = encoder!!.dequeueOutputBuffer(bufferInfo, timeoutUs)
                    if (outBufferId >= 0) {

                        val encodedBuffer = encoder?.getOutputBuffer(outBufferId) ?: return

                        muxer!!.writeSampleData(trackIndex, encodedBuffer, bufferInfo)

                        encoder!!.releaseOutputBuffer(outBufferId, false)

                        // Are we finished here?
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            allOutputEncoded = true
                            break
                        }
                    } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        encoderOutputAvailable = false
                    } else if (outBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        trackIndex = muxer!!.addTrack(encoder!!.outputFormat)
                        muxer!!.start()
                    }

                    if (outBufferId != MediaCodec.INFO_TRY_AGAIN_LATER)
                        continue

                    // Get output from decoder and feed it to encoder
                    if (!allInputDecoded) {
                        val outBufferId = decoder!!.dequeueOutputBuffer(bufferInfo, timeoutUs)
                        if (outBufferId >= 0) {
                            val outBuffer = decoder!!.getOutputBuffer(outBufferId)

                            val inBufferId = encoder!!.dequeueInputBuffer(timeoutUs)
                            val inBuffer = encoder!!.getInputBuffer(inBufferId)

//                            val format = decoder!!.getOutputFormat(outBufferId)

                            //当前时间   要减去起始时间才能对得上
                            val currentTimeUS = bufferInfo.presentationTimeUs - startTimeUS

                            //距离结束的时间ms 计算渐出用
                            val tillEndMillis = (endTimeUS - startTimeUS - currentTimeUS) / 1000


//                            // Fade in?
//                            if (bufferInfo.presentationTimeUs < fadeInDurationMillis * 1000) {
//                                fadeIn(inBufferId, outBufferId, fadeInDurationMillis.toLong())
//                            }
//                            // Fade out?
//                            else


                            //渐入渐出比较耗时
                            when {
                                currentTimeUS < fadeInDurationMillis * 1000 ->
                                    fadeIn(inBufferId, outBufferId, fadeInDurationMillis.toLong(), currentTimeUS / 1000)
                                fadeOutDurationMillis >= tillEndMillis ->
                                    fadeOut(inBufferId, outBufferId, fadeOutDurationMillis.toLong(), tillEndMillis)
                                else ->
                                    inBuffer?.put(outBuffer ?: return)
                            }


                            // Feed to encoder
                            encoder!!.queueInputBuffer(inBufferId, bufferInfo.offset, bufferInfo.size, currentTimeUS, bufferInfo.flags)
                            decoder!!.releaseOutputBuffer(outBufferId, false)

                            // Did we get all output from decoder?
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                                allInputDecoded = true

                        } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            decoderOutputAvailable = false
                        }
                    }
                }
            }
            cleanup()
            if (converListener != null && outPath != null) {
                converListener.converFinsh(outPath!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            converListener?.converError()
        }
    }

    private fun initCodecs(inFormat: MediaFormat) {
        decoder = inFormat.getString(MediaFormat.KEY_MIME)?.let { MediaCodec.createDecoderByType(it) }
        decoder?.configure(inFormat, null, null, 0)
        decoder?.start()

        val outFormat = getOutputFormat(inFormat)
        encoder = outFormat.getString(MediaFormat.KEY_MIME)?.let { MediaCodec.createEncoderByType(it) }
        encoder?.configure(outFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder?.start()

    }

    private fun selectAudioTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                extractor.selectTrack(i)
                return format
            }
        }

        throw InvalidParameterException("File contains no audio track")
    }

    private fun getOutputFormat(inputFormat: MediaFormat) = MediaFormat().apply {
        setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
        setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        setInteger(MediaFormat.KEY_SAMPLE_RATE, outSampleSize)
        setInteger(MediaFormat.KEY_BIT_RATE, outBitRate)
        setInteger(MediaFormat.KEY_CHANNEL_COUNT, inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT))
        // Important, otherwise you can get BufferOverflowException when copying decoded output to encoder input buffer
        //wav pcm等可能单词取出的字节数很大，会超出buufer的size，因此把最大size设置大一些防止 BufferOverflowException
        setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
    }


    private fun fadeIn(inBufferId: Int, outBufferId: Int, fadeInDurationMillis: Long, currentTimeMS: Long) {

        fade(inBufferId, outBufferId) { elapsedMillis ->
            // How much progress since the start of fade in effect?
            val progress = (currentTimeMS.toDouble() + elapsedMillis) / fadeInDurationMillis
            // Using exponential factor to increase volume
            progress * progress
        }
    }

    private fun fadeOut(inBufferId: Int, outBufferId: Int, fadeOutDurationMillis: Long, tillEndMillis: Long) {
        val totalElapsedMillis = fadeOutDurationMillis - tillEndMillis

        fade(inBufferId, outBufferId) { elapsedMillis ->
            // How much progress since the start of fade in effect?
            val progress = (totalElapsedMillis.toDouble() + elapsedMillis) / fadeOutDurationMillis
            // Logarithic factor for decreasing volume
            (20.0 * log(progress, 10.0)) / -40.0
        }
    }

    private fun fade(inBufferId: Int, outBufferId: Int, getFactor: (elapsedMillis: Long) -> Double) {
        val inBuffer = encoder!!.getInputBuffer(inBufferId)
        val outBuffer = decoder!!.getOutputBuffer(outBufferId)
        val shortSamples = outBuffer?.order(ByteOrder.nativeOrder())?.asShortBuffer() ?: return

        val format = decoder!!.getOutputFormat(outBufferId)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val size = shortSamples.remaining()
        val sampleDurationMillis = 1000L / format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var elapsedMillis = 0L

        for (i in 0 until size step channels) {
            for (c in 0 until channels) {
                // Process the sample
                val sample = shortSamples.get()

                // Put processed sample into encoder's buffer
                inBuffer?.putShort((sample * getFactor(elapsedMillis)).toInt().toShort())
            }

            elapsedMillis += sampleDurationMillis
        }
    }

    private fun cleanup() {
        extractor?.release()
        extractor = null

        decoder?.stop()
        decoder?.release()
        decoder = null

        encoder?.stop()
        encoder?.release()
        encoder = null

        muxer?.stop()
        muxer?.release()
        muxer = null

        trackIndex = -1
    }

    var converListener: ConverListener? = null

    interface ConverListener {
        fun converFinsh(path: String);
        fun converError();
    }

}