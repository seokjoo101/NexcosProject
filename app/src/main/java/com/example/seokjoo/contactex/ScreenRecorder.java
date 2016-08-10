package com.example.seokjoo.contactex;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;

import com.example.seokjoo.contactex.global.VideoCodec;

import org.webrtc.DataChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Seokjoo on 2016-08-09.
 */
public class ScreenRecorder extends Thread implements VideoCodec {




    private static final String TAG = "ScreenRecorder";


    public static int mWidth;
    public static int mHeight;
    public static int mBitRate;
    public static int mDpi;
    private MediaProjection mMediaProjection;

    private MediaCodec mEncoder;
    private Surface mSurface;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    public MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private VirtualDisplay mVirtualDisplay;
    MediaFormat format;



    public ScreenRecorder( int width, int height, int bitrate, int dpi, MediaProjection mp) {
        super(TAG);
        mWidth = width;
        mHeight = height;
        mBitRate = bitrate;
        mDpi = dpi;
        mMediaProjection = mp;
    }


    /**
     * stop task
     */
    public final void quit() {
        mQuit.set(true);
    }

    @Override
    public void run() {
        Log.d(TAG, "Screen recording running... " );

        try {
            try {
                prepareEncoder();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR

            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurface, null, null);
            Log.d(TAG, "created virtual display: " + mVirtualDisplay);
            recordVirtualDisplay();

        } finally {
            release();
        }
    }

    private void recordVirtualDisplay() {
        while (!mQuit.get()) {
            int index = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
            Log.i(TAG, "dequeue output buffer index=" + index);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "retrieving buffers time out!");
                try {
                    // wait 10ms
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            } else if (index >= 0) {

                encodeToVideoTrack(index);
                mEncoder.releaseOutputBuffer(index, false);

            }

        }
    }
    public ByteBuffer encodedData;
    byte[] mBuffer = new byte[0];

    public ByteBuffer encodeToVideoTrack(int index) {
        encodedData = mEncoder.getOutputBuffer(index);

        if (encodedData != null) {
            final int endOfStream = mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            // pass to whoever listens to
            if (endOfStream == 0) {
                VideoViewService.getInstance().client.dataChannel.send(new DataChannel.Buffer(encodedData,true));

            }
        }



        Log.i("TAG","encodedData : " + encodedData);

        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.
            // Ignore it.
            Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) {
            Log.d(TAG, "info.size == 0, drop it.");
            encodedData = null;
        } else {
            Log.d(TAG, "got buffer, info: size=" + mBufferInfo.size
                    + ", presentationTimeUs=" + mBufferInfo.presentationTimeUs
                    + ", offset=" + mBufferInfo.offset);
        }

        if (encodedData != null) {
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
            Log.i(TAG, "sent : " + mBufferInfo.size + " bytes to muxer...  / time : " + mBufferInfo.presentationTimeUs);
        }

//        VideoViewService.getInstance().client.dataChannel.send(new DataChannel.Buffer(encodedData,false));

        return encodedData;
    }



    private void prepareEncoder() throws IOException {

        format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        Log.d(TAG, "created video format: " + format);
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mEncoder.createInputSurface();
        Log.d(TAG, "created input surface: " + mSurface);
        mEncoder.start();
    }

    private void release() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }

    }

}
