package com.example.seokjoo.contactex;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.seokjoo.contactex.global.Global;
import com.example.seokjoo.contactex.global.WindowTouchView;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoViewService extends Service implements WindowTouchView,WebRtcClient.RtcListener  {


    public static Context VideoContext;
    private static final String VIDEO_CODEC_VP8 = "VP8";


    private static final String AUDIO_CODEC_OPUS = "opus";
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 70;
    private static final int LOCAL_Y_CONNECTED = 70;
    private static final int LOCAL_WIDTH_CONNECTED = 30;
    private static final int LOCAL_HEIGHT_CONNECTED = 30;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;


    public View windowView;
    public WindowManager windowManager;
    public WindowManager.LayoutParams windowViewLayoutParams;
    private WindowTouchPresenter windowTouchPresenter;


    private RendererCommon.ScalingType scalingType =  RendererCommon.ScalingType.SCALE_ASPECT_FILL;
    public VideoRenderer.Callbacks localRender;
    public VideoRenderer.Callbacks remoteRender;
    public WebRtcClient client;

    @BindView(R.id.glview_call)
    GLSurfaceView vsv;

    private Handler mHandler;

    Point displaySize;

    private static VideoViewService mInstance;

    public  static VideoViewService getInstance(){
        if(mInstance!=null)
            return mInstance;
        else
            return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VideoContext=this;

        windowView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.windowview, null);
        ButterKnife.bind(this, windowView);
        displaySize = new Point();
        mHandler = new Handler();
        mInstance=this;


        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(displaySize);


        initWindowLayout();


        windowTouchPresenter = new WindowTouchPresenter(this);


        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {
                init();
            }
        });

        // local and remote render
        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        localRender = VideoRendererGui.create(
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);



        return START_NOT_STICKY;
    }




    private void init() {

        PeerConnectionParameters params = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP8, true, 1, AUDIO_CODEC_OPUS, true);

        client = new WebRtcClient(this, params );

        startCam();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void updateViewLayout(int x, int y) {
        if (windowViewLayoutParams != null) {
            windowViewLayoutParams.x += x;
            windowViewLayoutParams.y += y;

            windowManager.updateViewLayout(windowView, windowViewLayoutParams);
        }
    }


    public void displaySide(){
        windowViewLayoutParams.x=displaySize.x;
        windowViewLayoutParams.y=displaySize.y;
        windowManager.updateViewLayout(windowView, windowViewLayoutParams);


    }
    public void displayMid(int x,int y){
        windowViewLayoutParams.x=displaySize.x/2-x/2;
        windowViewLayoutParams.y=displaySize.y/2-y/2;
        windowManager.updateViewLayout(windowView, windowViewLayoutParams);

        VideoViewService.getInstance().vsv.setVisibility(View.GONE);

    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            windowTouchPresenter.onTouch(event);
            return false;
        }
    };


    /**
     * Window View 를 초기화 한다. X, Y 좌표는 0, 0으로 지정한다.
     */

    private void initWindowLayout() {


        vsv.post(new Runnable() {
            @Override
            public void run() {
                 displayMid(vsv.getWidth(),vsv.getHeight());
            }
        });

        windowViewLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                displaySize.x/2-vsv.getWidth()/2, displaySize.y/2-vsv.getHeight()/2, // X, Y 좌표
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        windowViewLayoutParams.gravity = Gravity.TOP | Gravity.START;

        windowManager.addView(windowView, windowViewLayoutParams);

        windowView.setOnTouchListener(touchListener);

    }


    @Override
    public void onStatusChanged(String newStatus) {
        Handler handler =new Handler(Looper.getMainLooper());
        handler.post(new ToastRunnable(newStatus));

    }




    @Override
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));

        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType,true);


     }


    @Override
    public void onAddRemoteStream(MediaStream remoteStream) {
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        VideoRendererGui.update(remoteRender,
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType,true);
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
                scalingType,true);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(client!=null) {
            client.onDestroy();
            windowManager.removeViewImmediate(windowView);
        }
//        mediaStream.dispose();



     }

    public void startCam() {
        client.start();
    }


    private class ToastRunnable implements Runnable {
        String mText;

        public ToastRunnable(String text) {
            mText = text;
        }

        @Override
        public void run(){
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }


    public  void call(){
        client.call(Global.ToTopic);
    }
}

