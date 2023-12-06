package com.example.newcollage.util.media;

import static android.media.MediaFormat.KEY_CHANNEL_COUNT;
import static android.media.MediaFormat.KEY_SAMPLE_RATE;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Yue on 2021/8/2.
 */
public class AudioConvertor {

    private static final int TIMEOUT_USEC = 0;

    private MediaExtractor mExtractor;
    private int mAudioTrack = -1;

    private long mStartTimeUS;
    private long mEndTimeUS;
    private int mSampleRate;
    private int mChannelCount;
    private int mTrackIndex;
    private long mPTS = 0;
    private boolean mReadEnd;

    private ConvertListener mConvertListener;
    private String mOutPath;

    @WorkerThread
    public void convertInBackground(String inputPath, String outPath, String tempPath, int startTimeMs, int durationMs, ConvertListener convertListener) {
        new Thread(() -> convertToAAC(inputPath, outPath, tempPath, startTimeMs, durationMs, convertListener)).start();
    }

    private void convertToAAC(String inputPath, String outPath, String tempPath, int startTimeMs, int durationMs, ConvertListener convertListener) {
        try {


            mConvertListener = convertListener;
            mOutPath = outPath;
            boolean hasAudio = false;//判断音频文件是否有音频音轨

            mExtractor = new MediaExtractor();
            mExtractor.setDataSource(inputPath);
            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat format = mExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    mAudioTrack = i;
                    hasAudio = true;
                    break;
                }
            }
            if (!hasAudio && mConvertListener != null) {
                mConvertListener.onError(new Exception("File has not audio track"));
            }
            mExtractor.selectTrack(mAudioTrack);

            mStartTimeUS = startTimeMs * 1000L;
            mEndTimeUS = durationMs * 1000L + mStartTimeUS;

            convertAudioToPCM(tempPath);
            convertPCMtoAAC(tempPath, outPath);

        } catch (Exception e) {
            e.printStackTrace();
            if (mConvertListener != null) mConvertListener.onError(e);
        }

    }

    private void convertAudioToPCM(String tempPath) throws Exception {

        MediaFormat format = mExtractor.getTrackFormat(mAudioTrack);
        mSampleRate = format.getInteger(KEY_SAMPLE_RATE);
        mChannelCount = format.getInteger(KEY_CHANNEL_COUNT);

        //初始化音频解码器
        MediaCodec audioCodec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
        audioCodec.configure(format, null, null, 0);
        audioCodec.start();//启动MediaCodec，等待传入数据

        MediaCodec.BufferInfo decodeBufferInfo = new MediaCodec.BufferInfo();//用于描述解码得到的byte[]数据的相关信息

        boolean codeOver = false; //编码结束
        boolean inputDone = false;//解码结束

        FileOutputStream fos = new FileOutputStream(tempPath);

        while (!codeOver) {
            if (!inputDone) {

                long sampleTime = mExtractor.getSampleTime();

                if (sampleTime >= 0 && sampleTime < mStartTimeUS) {
                    mExtractor.advance();
                    continue;
                }

                int inBufferId = audioCodec.dequeueInputBuffer(TIMEOUT_USEC);

                if (inBufferId >= 0) {

                    ByteBuffer inputBuffer = audioCodec.getInputBuffer(inBufferId);
                    if (inputBuffer == null) return;   //这里做下错误处理
                    int sampleSize = mExtractor.readSampleData(inputBuffer, 0);

                    if (sampleSize > 0 && mEndTimeUS > sampleTime) {
                        audioCodec.queueInputBuffer(inBufferId, 0, sampleSize, sampleTime, mExtractor.getSampleFlags());
                        //可以做进度回调
//                        Log.d("YUEDEVTAG", "time=" + sampleTime + ", progress=" + (sampleTime - mStartTimeUS) * 100 / (mEndTimeUS - mStartTimeUS));
                        mExtractor.advance();
                    } else {
                        //结束了
                        audioCodec.queueInputBuffer(
                                inBufferId, 0, 0,
                                0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        );
                        inputDone = true;
                    }
                }
            }


            boolean decodeOutputDone = false;
            byte[] chunkPCM;
            while (!decodeOutputDone) {
                int outputIndex = audioCodec.dequeueOutputBuffer(decodeBufferInfo, TIMEOUT_USEC);
                if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    //没有可用的解码器
                    decodeOutputDone = true;
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                } else if (outputIndex > 0) {
                    ByteBuffer outputBuffer = audioCodec.getOutputBuffer(outputIndex);

                    chunkPCM = new byte[decodeBufferInfo.size];
                    outputBuffer.get(chunkPCM);
                    outputBuffer.clear();

                    fos.write(chunkPCM);//数据写入文件中
                    fos.flush();
                    Log.d("YUEDEVTAG", "flush outputIndex ：" + outputIndex);
                    audioCodec.releaseOutputBuffer(outputIndex, false);

                    if ((decodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {//编解码结束
                        mExtractor.release();
                        audioCodec.stop();
                        audioCodec.release();
                        codeOver = true;
                        decodeOutputDone = true;
                    }
                }
            }
        }
        fos.close();

    }


    private void convertPCMtoAAC(String pcmPath, String outPath) throws Exception {

        if (!new File(pcmPath).exists()) {//pcm文件目录不存在
            throw new Exception("input pcm file is not exist");
        }

        FileInputStream fis = new FileInputStream(pcmPath);
        byte[] readBuffer = new byte[1024];


        //初始化编码格式   mimetype  采样率  声道数
        MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, mSampleRate, mChannelCount);
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 192 * 1000);
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 500 * 1024);

        //初始化容器
        MediaMuxer muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);


        //初始化编码器
        MediaCodec mediaEncode = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        mediaEncode.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        mediaEncode.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                if (mReadEnd) {
                    //读取结束
                    //结束的时候也给个pts，否则output的时候，最后一段可能拿不到pts会导致崩溃
                    mPTS += 250000L * readBuffer.length / mSampleRate;
                    codec.queueInputBuffer(index, 0, 0, mPTS, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    return;
                }
                ByteBuffer inputBuffer = codec.getInputBuffer(index);
                if (inputBuffer == null) return;
                try {
                    mReadEnd = fis.read(readBuffer) == -1;
                    inputBuffer.clear();
                    inputBuffer.limit(readBuffer.length);
                    inputBuffer.put(readBuffer);
                    mPTS += 250000L * readBuffer.length / mSampleRate;
                    codec.queueInputBuffer(index, 0, readBuffer.length, mPTS, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mConvertListener != null) mConvertListener.onError(e);
                }

            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                if (outputBuffer != null) {
                    muxer.writeSampleData(mTrackIndex, outputBuffer, info);
                }
                codec.releaseOutputBuffer(index, false);
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    //输出结束
                    try {
                        codec.stop();
                        codec.release();
                        muxer.stop();
                        muxer.release();
                        fis.close();
                        if (mConvertListener != null) {
                            mConvertListener.onSuccess(mOutPath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mConvertListener != null) mConvertListener.onError(e);
                    }
                }
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                e.printStackTrace();
                if (mConvertListener != null) mConvertListener.onError(e);
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                mTrackIndex = muxer.addTrack(format);
                muxer.start();
            }
        });
        mediaEncode.start();
    }


    //adts header
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    public interface ConvertListener {
        void onSuccess(String outPath);

        void onError(Exception e);
    }

}
