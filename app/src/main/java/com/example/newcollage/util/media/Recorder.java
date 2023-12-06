package com.example.newcollage.util.media;
/*
 *create by mac_hou on 2020/5/29 16:49
 *
 *
 *
 */


import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_BLEND;
import static android.opengl.GLES30.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES30.GL_COMPILE_STATUS;
import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_LINK_STATUS;
import static android.opengl.GLES30.GL_ONE;
import static android.opengl.GLES30.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES30.GL_STATIC_DRAW;
import static android.opengl.GLES30.GL_TRIANGLE_FAN;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glAttachShader;
import static android.opengl.GLES30.glBindBuffer;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glBlendFunc;
import static android.opengl.GLES30.glBufferData;
import static android.opengl.GLES30.glClear;
import static android.opengl.GLES30.glClearColor;
import static android.opengl.GLES30.glCompileShader;
import static android.opengl.GLES30.glCreateProgram;
import static android.opengl.GLES30.glCreateShader;
import static android.opengl.GLES30.glDeleteBuffers;
import static android.opengl.GLES30.glDeleteProgram;
import static android.opengl.GLES30.glDeleteShader;
import static android.opengl.GLES30.glDeleteVertexArrays;
import static android.opengl.GLES30.glDrawArrays;
import static android.opengl.GLES30.glEnable;
import static android.opengl.GLES30.glEnableVertexAttribArray;
import static android.opengl.GLES30.glGenBuffers;
import static android.opengl.GLES30.glGenVertexArrays;
import static android.opengl.GLES30.glGetProgramInfoLog;
import static android.opengl.GLES30.glGetProgramiv;
import static android.opengl.GLES30.glGetShaderInfoLog;
import static android.opengl.GLES30.glGetShaderiv;
import static android.opengl.GLES30.glGetUniformLocation;
import static android.opengl.GLES30.glLinkProgram;
import static android.opengl.GLES30.glShaderSource;
import static android.opengl.GLES30.glUniformMatrix4fv;
import static android.opengl.GLES30.glUseProgram;
import static android.opengl.GLES30.glVertexAttribPointer;
import static android.opengl.GLES30.glViewport;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.permissionx.guolindev.BuildConfig;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Objects;


public class Recorder {


    public interface Listener {
        void onSuccess();

        void onProgress(float progress);

        void onError(Exception e);
    }

    private Listener mListener;

    //视频进度占总进度的百分比
    private static final float VIDEO_PROGRESS_PERCENT = 98f;

    private static final String TAG = "YUEDEVTAG";

    private static final Boolean VERBOSE = BuildConfig.DEBUG;


    // 视频编码
    public static final String MIME_TYPE_VIDEO = MediaFormat.MIMETYPE_VIDEO_AVC;
    //音频编码
    public static final String MINE_TYPE_AUDIO = MediaFormat.MIMETYPE_AUDIO_AAC;

    // 视频文件的宽高
    private int mVideoWidth;
    private int mVideoHeight;
    // 视频码率
    private int mVideoBitRate;


    //视频时长 秒
    public static final int VIDEO_TIME = 12;

    // 帧率
    private int mFrameRate;
    // 总帧数
    private int mFrameNum;


    // 编码器
    private MediaCodec mVideoEncoder;
    //容器
    private MediaMuxer mMuxer;

    private CodecInputSurface mInputSurface;

    private int mVideoTrackIndex;//video 轨道
    private boolean mMuxerStarted;


    private static final int TIMEOUT_USEC = 1000;

    //时间戳计数器
    private long mPTSCount = 0;

    // allocate one of these up front so we don't need to do it every time
    private MediaCodec.BufferInfo mBufferInfo;

    public Recorder(int videoWidth, int videoHeight, int frameRate) {

        mFrameRate = frameRate;
        mFrameNum = frameRate * VIDEO_TIME;

        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
        mVideoBitRate = videoWidth * videoHeight * 8 * mFrameRate / 30;

    }

    private List<Objects> mGLSprites;


    /**
     * 录制opengl到MP4,程序入口
     * <p>
     * 29及以上，outPath
     * 29以下，传入outDir
     * <p>
     * 资源音乐传入musicAFD
     * 用户音乐传入aacTemp
     */
    public void record(List<Objects> glSprites, String outPath, FileDescriptor outFD, String musicPath, AssetFileDescriptor musicAFD, Listener listener) {

        mListener = listener;

        mGLSprites = glSprites;

        try {
            // 初始化视频
            initVideo(outPath, outFD);

            //初始化音频
            initAudio(musicPath, musicAFD);

            // 设置 EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx
            mInputSurface.makeCurrent();

            //准备opengl
            initOpenGL();

            // 共NUM_FRAMES帧
            for (int i = 0; i < mFrameNum; i++) {
                // mEncoder从缓冲区取数据，然后交给mMuxer编码
                drainEncoder(false);
                // opengl绘制一帧
                drawOpenGL(i * 1000 / mFrameRate);
                // 设置图像，发送给EGL的显示时间
                mInputSurface.setPresentationTime(computePresentationTimeNsec(i));
                // 交换缓冲
                mInputSurface.swapBuffers();
                if (mListener != null) {
                    //视频进度回调，占整体的98%
                    float progress = VIDEO_PROGRESS_PERCENT * i / mFrameNum;
                    mListener.onProgress(progress);
                }
            }

            // 停止视频解码
            drainEncoder(true);
            // 关闭opengl
            endOpenGL();

            if (hasAudio()) {
                while (mAudioStarted) {
                    stepPipeline();
                }
            }

            releaseEncoder();
            if (mListener != null) {
                mListener.onProgress(100f);
                mListener.onSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            releaseEncoder();
            if (mListener != null) mListener.onError(e);
        }
    }

    private void endOpenGL() {
        for (Objects sprite : mGLSprites) {
//            sprite.glRelease();
        }
        glDeleteVertexArrays(1, vao, 0);
        glDeleteBuffers(1, vbo, 0);
        glDeleteProgram(programID);
    }


    private int programID;

    float[] vertex = new float[]{
            -1f, -1f, 0f, 0f, 1f,  // 左下
            1f, -1f, 0f, 1f, 1f,   // 右下
            1f, 1f, 0f, 1f, 0f,  //右上
            -1f, 1f, 0f, 0f, 0f,  // 左上//
    };


    float[] projectionMatrix = new float[16];
    float[] viewMatrix = new float[16];

    private static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

    int[] vao = new int[1];
    int[] vbo = new int[1];

    private void initOpenGL() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertex.length * FLOAT_SIZE);
        FloatBuffer vertexBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);

        glViewport(0, 0, mVideoWidth, mVideoHeight);

        //背面剔除，有个3D动画，所以没有启用
//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);


        //开启混合 才能显示png的透明
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        //顶点的各种数据 vao， vbo ebo
        glGenVertexArrays(1, vao, 0);
        glGenBuffers(1, vbo, 0);
//            glGenBuffers(1, ebo, 0)

        glBindVertexArray(vao[0]);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(
                GL_ARRAY_BUFFER,
                vertex.length * FLOAT_SIZE,
                vertexBuffer,
                GL_STATIC_DRAW
        );

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * FLOAT_SIZE, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * FLOAT_SIZE, 3 * FLOAT_SIZE);
        glEnableVertexAttribArray(1);


        //shader program
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, GLSL.VERTEX_SHADER_STRING);
        glCompileShader(vertexShader);


        int[] state = new int[3];
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, state, 0);
        if (state[0] == 0) {
            String log = glGetShaderInfoLog(vertexShader);
            Log.d("YUEDEVTAG", "compile vertex shader error:\n" + log);
            return;
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, GLSL.FRAGMENT_SHADER_STRING);
        glCompileShader(fragmentShader);

        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, state, 1);
        if (state[1] == 0) {
            String log = glGetShaderInfoLog(fragmentShader);
            Log.d("YUEDEVTAG", "compile fragment shader error:\n" + log);
            return;
        }


        programID = glCreateProgram();

        glAttachShader(programID, vertexShader);
        glAttachShader(programID, fragmentShader);
        glLinkProgram(programID);


        glGetProgramiv(programID, GL_LINK_STATUS, state, 2);
        if (state[2] == 0) {
            String log = glGetProgramInfoLog(programID);
            Log.d("YUEDEVTAG", "link program error:\n" + log);
            return;
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        glUseProgram(programID);


//        Matrix.orthoM(projectionMatrix, 0, -VIDEO_WIDTH * 1f / VIDEO_HEIGHT, VIDEO_WIDTH * 1f / VIDEO_HEIGHT, -1.0f, 1.0f, 0f, 100.0f);


        //用透视投影
        float scale = mVideoWidth * 1f / mVideoHeight;
        float fovy = 45f;
        Matrix.perspectiveM(projectionMatrix, 0, fovy, scale, 0f, 100f);

        int modelLocation = glGetUniformLocation(programID, GLSL.PROJECTION_UNIFORM_NAME);
        glUniformMatrix4fv(modelLocation, 1, false, projectionMatrix, 0);


        //求得投影1：1的距离，这样才能图片按照height充满屏幕高度
        float f = (float) (1.0f / Math.tan(fovy * (Math.PI / 360)));
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, f, 0f, 0f, 0f, 0f, 1f, 0f);

        int viewLocation = glGetUniformLocation(programID, GLSL.VIEW_UNIFORM_NAME);
        glUniformMatrix4fv(viewLocation, 1, false, viewMatrix, 0);


        for (Objects sprite : mGLSprites) {
//            sprite.glInit(mVideoWidth, mVideoHeight);
//            sprite.glPreDraw();
//            sprite.glPreFrame();
        }
    }


    /**
     * 生成一帧
     */
    private void drawOpenGL(int timeMs) {

        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(programID);
        glBindVertexArray(vao[0]);


        for (Objects sprite : mGLSprites) {
//            sprite.glNextFrame(timeMs);
//            sprite.glDraw(programID);
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        }

    }

    private int mResolutionError = 0;

    /**
     * 初始化视频编码
     * 初始化code surface
     * 初始化mux容器
     */
    private void initVideo(String outPath, FileDescriptor outFD) {

        // 创建一个buffer
        mBufferInfo = new MediaCodec.BufferInfo();

        //-----------------MediaFormat-----------------------
        // mediaCodeC采用的是H.264编码
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE_VIDEO, mVideoWidth, mVideoHeight);
        // 数据来源自surface
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        // 视频码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoBitRate);
        // fps
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        //设置关键帧的时间
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧间隔时间 单位s

        format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        format.setInteger("level", MediaCodecInfo.CodecProfileLevel.AVCLevel41);


        //-----------------Encoder-----------------------
        try {
            mVideoEncoder = MediaCodec.createEncoderByType(MIME_TYPE_VIDEO);
            mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            if (mResolutionError > 100) throw new RuntimeException("Resolution error!");
            mResolutionError++;
            float ratio = mVideoWidth * 1.0f / mVideoHeight;

            if (mVideoWidth < mVideoHeight) {
                if (mVideoWidth == 1080) {
                    mVideoWidth = 720;
                } else if (mVideoWidth == 720) {
                    mVideoWidth = 540;
                } else if (mVideoWidth == 540) {
                    mVideoWidth = 360;
                } else {
                    mVideoWidth /= 2;
                    mVideoWidth = mVideoWidth / 2 * 2;
                }
                mVideoHeight = (int) (mVideoWidth / ratio) / 2 * 2;

            } else {
                if (mVideoHeight == 1080) {
                    mVideoHeight = 720;
                } else if (mVideoHeight == 720) {
                    mVideoHeight = 540;
                } else if (mVideoHeight == 540) {
                    mVideoHeight = 360;
                } else {
                    mVideoHeight /= 2;
                    mVideoHeight = mVideoHeight / 2 * 2;
                }
                mVideoWidth = (int) (mVideoHeight * ratio) / 2 * 2;
            }
        }

        // 创建一个surface
        Surface surface = mVideoEncoder.createInputSurface();
        // 创建一个CodecInputSurface,其中包含GL相关
        mInputSurface = new CodecInputSurface(surface);
        //
        mVideoEncoder.start();

        //-----------------MediaMuxer-----------------------
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mMuxer = new MediaMuxer(outFD, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } else {
                mMuxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mVideoTrackIndex = -1;
        mMuxerStarted = false;
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     * 释放资源
     */
    private void releaseEncoder() {
        try {
            if (mVideoEncoder != null) {
                mVideoEncoder.stop();
                mVideoEncoder.release();
                mVideoEncoder = null;
            }

            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * mEncoder从缓冲区取数据，然后交给mMuxer编码
     *
     * @param endOfStream 是否停止录制
     */
    private void drainEncoder(boolean endOfStream) {

        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        // 停止录制
        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "end Of Stream, sending EOS to encoder");
            mVideoEncoder.signalEndOfInputStream();
        }
        //拿到输出缓冲区,用于取到编码后的数据
        while (true) {
            //拿到输出缓冲区的索引
            int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);

            if (VERBOSE) Log.d("YUEDEVTAG", "encoderStatus:" + encoderStatus);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                //
                MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                if (VERBOSE) Log.d(TAG, "encoder output format changed: " + newFormat);
                // now that we have the Magic Goodies, start the muxer
                mVideoTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
            } else {
                //获取解码后的数据
                ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(encoderStatus);
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }
                //
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }
                //

                if (mBufferInfo.size < 0
                        || mBufferInfo.offset < 0
                        || (mBufferInfo.offset + mBufferInfo.size) > encodedData.capacity()) {
                    //数据不对的情况,先跳过writeSampleData
                    mBufferInfo.size = 0;
                    String message = "mBufferInfo.size: " + mBufferInfo.size + ", " +
                            "mBufferInfo.offset: " + mBufferInfo.offset + ", " +
                            "capacity: " + encodedData.capacity();
                    if (VERBOSE) Log.d(TAG, message);
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

//                    //计算时间戳，有些手机获取的pts一直在变，这里手动赋值
                    long pts = mPTSCount * 1_000_000L / mFrameRate;
                    mBufferInfo.presentationTimeUs = pts;
                    mPTSCount++;
                    if (VERBOSE) Log.d("YUEDEVTAG", "pts:" + pts);
                    // 往容器写入数据
                    mMuxer.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }
                //释放资源
                mVideoEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        if (VERBOSE) Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }


    MediaCodec.BufferInfo mAudioBuffInfo;
    ByteBuffer mAudioBuff;

    //偏移时间
    int mAudioOffset = 0 * 1000 * 1000;

    boolean mAudioStarted = false;//是否结束


    //音频在mux里的轨道
    int mAudioTrackIndex = 0;


    MediaExtractor mAudioExtractor;


    private static final int AAC_SAMPLE_RATE = 44100;
    private static final int AAC_BIT_RATE = 96000;

    private static final int AUDIO_BUFF_SIZE = 1024 * 1024;


    //初始化音频  把aac 交给mux合成mp4
    private void initAudio(String musicPath, AssetFileDescriptor musicAFD) {

        try {

            mAudioBuff = ByteBuffer.allocateDirect(AUDIO_BUFF_SIZE).order(ByteOrder.nativeOrder());
            mAudioBuffInfo = new MediaCodec.BufferInfo();

            mAudioExtractor = new MediaExtractor();

            if (musicPath != null) {
                mAudioExtractor.setDataSource(musicPath);
                if (VERBOSE) Log.d("YUEDEVTAG", "musicPath:" + musicPath);
            } else if (musicAFD != null) {
                mAudioExtractor.setDataSource(musicAFD.getFileDescriptor(), musicAFD.getStartOffset(), musicAFD.getLength());
                if (VERBOSE) Log.d("YUEDEVTAG", "musicAFD:" + musicAFD);
            } else {
                //aacTemp与musicAFD都为空，不进行音乐编码
                mAudioStarted = false;
                return;
            }


            int trackCount = mAudioExtractor.getTrackCount();//获得通道数量
            int audiopos = 0;
            if (trackCount > 1) {
                for (int i = 0; i < trackCount; i++) { //遍历所以轨道
                    MediaFormat itemMediaFormat = mAudioExtractor.getTrackFormat(i);
                    String itemMime = itemMediaFormat.getString(MediaFormat.KEY_MIME);
                    if (itemMime.startsWith("audio")) { //获取音频轨道位置
                        audiopos = i;
                        break;
                    }
                }
            }

            mAudioExtractor.selectTrack(audiopos);//选择到音频轨道

            MediaFormat format = mAudioExtractor.getTrackFormat(audiopos);

            mAudioTrackIndex = mMuxer.addTrack(format);
            mAudioStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
            mAudioBuffInfo = null;
            mAudioStarted = false;
        }
    }


    boolean hasAudio() {
        try {
            int trackIndex = mAudioExtractor.getSampleTrackIndex();
            //没有通道
            if (VERBOSE) Log.d("YUEDEVTAG", "hasAudio: " + (trackIndex >= 0));
            return trackIndex >= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void stepPipeline() {
        if (!mAudioStarted) return;

        mAudioBuff.clear();
        int chunkSize = mAudioExtractor.readSampleData(mAudioBuff, 0);
        long sampleTime = mAudioExtractor.getSampleTime();//返回当前的时间戳 微秒

        if (VERBOSE) Log.d("YUEDEVTAG", "audio sampleTime: " + sampleTime);

        //结束
        if (sampleTime > VIDEO_TIME * 1000 * 1000L) {
            if (VERBOSE) Log.d("YUEDEVTAG", "mux audio end");
            mAudioBuff.clear();
            mAudioBuffInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            mMuxer.writeSampleData(mAudioTrackIndex, mAudioBuff, mAudioBuffInfo);
            mAudioStarted = false;
            return;
        }

        if (chunkSize > 0) {
            mAudioBuffInfo.set(0, chunkSize, sampleTime, MediaCodec.BUFFER_FLAG_KEY_FRAME);
            mMuxer.writeSampleData(mAudioTrackIndex, mAudioBuff, mAudioBuffInfo);
            mAudioExtractor.advance();
            if (mListener != null) {
                //音频进度回调用
                float progress = VIDEO_PROGRESS_PERCENT + (100f * sampleTime / VIDEO_TIME * 1000 * 1000L);
                mListener.onProgress(progress);
            }
        } else {
            //无数据了
            if (VERBOSE) Log.d("YUEDEVTAG", "no audio data");
            mAudioBuff.clear();
            mAudioBuffInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            mMuxer.writeSampleData(mAudioTrackIndex, mAudioBuff, mAudioBuffInfo);
            mAudioStarted = false;
        }

    }


    private long computePresentationTimeNsec(int frameIndex) {
        long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / mFrameRate;
    }


    /**
     * Holds state associated with a Surface used for MediaCodec encoder input.
     * <p>
     * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses that
     * to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to be sent
     * to the video encoder.
     * <p>
     * This object owns the Surface -- releasing this will release the Surface too.
     */
    private static class CodecInputSurface {
        private static final int EGL_RECORDABLE_ANDROID = 0x3142;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
        private Surface mSurface;

        /**
         * Creates a CodecInputSurface from a Surface.
         */
        public CodecInputSurface(Surface surface) {
            if (surface == null) {
                throw new NullPointerException();
            }
            mSurface = surface;
            initEGL();
        }


        /**
         * 初始化EGL
         */
        private void initEGL() {

            //--------------------mEGLDisplay-----------------------
            // 获取EGL Display
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            // 错误检查
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            // 初始化
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for recording and OpenGL ES 3.0.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE,
                    EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                    EGL_RECORDABLE_ANDROID,
                    1,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            // eglCreateContext RGB888+recordable ES2
            EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);

            // Configure context for OpenGL ES 3.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
            };
            //--------------------mEGLContext-----------------------
            //  eglCreateContext

            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    attrib_list, 0);

            checkEglError("eglCreateContext");
            //--------------------mEGLSurface-----------------------
            // 创建一个WindowSurface并与surface进行绑定,这里的surface来自mEncoder.createInputSurface();
            // Create a window surface, and attach it to the Surface we received.
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            // eglCreateWindowSurface
            mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface,
                    surfaceAttribs, 0);


            checkEglError("eglCreateWindowSurface");
        }

        /**
         * Discards all resources held by this class, notably the EGL context.  Also releases the
         * Surface that was passed to our constructor.
         * 释放资源
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }

            mSurface.release();

            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;

            mSurface = null;
        }

        /**
         * Makes our EGL context and surface current.
         * 设置 EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx
         */
        public void makeCurrent() {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
            checkEglError("eglMakeCurrent");
        }

        /**
         * Calls eglSwapBuffers.  Use this to "publish" the current frame.
         * 用该方法，发送当前Frame
         */
        public boolean swapBuffers() {
            boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            checkEglError("eglSwapBuffers");
            return result;
        }

        /**
         * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
         * 设置图像，发送给EGL的时间间隔
         */
        public void setPresentationTime(long nsecs) {
            // 设置发动给EGL的时间间隔
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
            checkEglError("eglPresentationTimeANDROID");
        }

        /**
         * Checks for EGL errors.  Throws an exception if one is found.
         * 检查错误,代码可以忽略
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }
}
//————————————————
//        版权声明：本文为CSDN博主「xiaxl」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/aiwusheng/java/article/details/72530314
