package com.example.seokjoo.contactex;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.example.seokjoo.contactex.global.Global;
import com.example.seokjoo.contactex.global.VideoCodec;

import org.webrtc.DataChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Seokjoo on 2016-08-09.
 */
public class ScreenDecoder extends Thread implements DataChannel.Observer ,VideoCodec {

    private static final String VIDEO = "video/";

    private MediaCodec mDecoder;

    public boolean eosReceived;
    public setDecoderListener setDecoderListener;

    boolean IsRun;
    ByteBuffer byteBuffer;
    boolean isInput = true;
    boolean configured = false;
    private static ScreenDecoder minstance;

    public static ScreenDecoder getInstance(){
        if(minstance!=null)
            return minstance;
        else
            return null;
    }





    ScreenDecoder(setDecoderListener  decoderListener){
        minstance=this;
        setDecoderListener=decoderListener;
        IsRun=false;
    }

    public boolean init(Surface surface, ByteBuffer buffer) {


        eosReceived = false;
        try {

            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            format.setByteBuffer("buffer",buffer);
            mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
            Log.e(Global.TAG_, "format : " + format);
            mDecoder.configure(format, surface, null, 0 /* Decoder */);
            mDecoder.start();
            configured=true;

            this.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(Global.TAG_,"Init exception : " + e);
        }

        return true;
    }



    void decode(ByteBuffer buffer){
        if (configured) {
//                dequeueInputBuffer를 통해 현재 사용 가능한 index를 받아 온다.
            int inputIndex = mDecoder.dequeueInputBuffer(TIMEOUT_US);

            if (inputIndex >= 0) {
                //해당 index에 접근하여 실제 Byte를 사용
                ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputIndex);

//                     inputBuffer.clear();

                if (inputBuffer != null)
                    inputBuffer.put(buffer);


                mDecoder.queueInputBuffer(inputIndex, 0, 100000, 10000000, 0);
                Log.i(Global.TAG_, "byteBuffer : " + buffer);
            }
        }
    }

    @Override
    public void run() {

        try {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            while (!eosReceived ) {
                if(configured) {
                    int outIndex = mDecoder.dequeueOutputBuffer(info, 10000);
//                        Log.e(Global.TAG_, "outIndex : " + outIndex);

                    if (outIndex >= 0) {
                        mDecoder.releaseOutputBuffer(outIndex, true /* Surface init */);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            break;
                        }
                    }
                }else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignore) {
                    }
                }
            }

        }
        finally {
            if(isInput){

//                mDecoder.stop();
                mDecoder.release();

            }
        }


    }

    public void quit() {
        eosReceived = true;

    }


    @Override
    public void onBufferedAmountChange(long l) {
        Log.i(Global.TAG_,"onBufferedAmountChange "  );
    }


    @Override
    public void onStateChange() {
        Log.i(Global.TAG_,"onStateChange"  );
        Log.i(Global.TAG_,"data channel state " + VideoViewService.getInstance().client.dataChannel.state());
    }

    byte[] mBuffer = new byte[0];

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
//        Log.i(Global.TAG_,"receive buffer : " + buffer.data);
        byteBuffer=buffer.data;

        if(!IsRun){
            setDecoderListener.startDecoder(byteBuffer);
            IsRun=true;
        }

        if(AcceptActivity.bRecordClick)
            AcceptActivity.bRecordClick=false;

        if (mBuffer.length < byteBuffer.limit()) {
            mBuffer = new byte[byteBuffer.limit()];
        }

        byteBuffer.position(0);
        byteBuffer.limit(0 + byteBuffer.limit());
        byteBuffer.get(mBuffer, 0, byteBuffer.limit());
        byteBuffer=ByteBuffer.wrap(mBuffer, 0, byteBuffer.limit());
        decode(byteBuffer);


    }

    interface setDecoderListener{
        void startDecoder(ByteBuffer buffer);
        void stopDecoder();

    }

}