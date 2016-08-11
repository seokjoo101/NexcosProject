package com.example.seokjoo.contactex;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.seokjoo.contactex.global.Global;
import com.example.seokjoo.contactex.global.VideoCodec;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoRendererGui;

import java.nio.ByteBuffer;

/**
 * Created by Seokjoo on 2016-08-08.
 */
public class AcceptActivity extends Activity implements ScreenDecoder.setDecoderListener,VideoCodec{


    private ScreenDecoder mDecorder;

    SurfaceView screen;
    Surface surface;

    private MediaProjectionManager mMediaProjectionManager;
    private static final int REQUEST_CODE = 1;
    private ScreenRecorder mRecorder;

    public static Activity contextMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_layout);


        mDecorder= new ScreenDecoder(this);
        WebRtcClient.getmInstance().dataChannel.registerObserver(ScreenDecoder.getInstance());
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        screen=(SurfaceView)findViewById(R.id.screen);
        surface = screen.getHolder().getSurface();


        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //전화 끊기
//                VideoRendererGui.remove(VideoViewService.getInstance().localRender);
//                VideoRendererGui.remove(VideoViewService.getInstance().remoteRender);
                VideoRendererGui.dispose();
                VideoViewService.getInstance().windowManager.removeViewImmediate(VideoViewService.getInstance().windowView);


                if(mRecorder!=null) {
                    mRecorder.quit();
                    mRecorder = null;
                }

                JSONObject payload = new JSONObject();
                try{
                    payload.put("type","exit");
                }catch(JSONException ex){
                    Log.i(Global.TAG,"json fail " +ex);
                }
                MqttService.getInstance().publish(Global.ToTopic,payload.toString());
                moveMain();
            }


        });


         boolean IsCall = getIntent().getBooleanExtra("call",false);

        if(IsCall){
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE);
        }

        contextMain=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }

        mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection);
        mRecorder.start();
        Toast.makeText(this, "Screen recorder is running...", Toast.LENGTH_SHORT).show();
    }


    private void moveMain() {
        startActivity(new Intent(this,MainActivity.class));
        this.finish();
    }


    @Override
    public void startDecoder(ByteBuffer buffer) {

        if(mDecorder==null)
            mDecorder= new ScreenDecoder(this);

        mDecorder.init(surface,buffer);
//        mDecorder.start();
        Log.e(Global.TAG_,"startDecoder");
    }

    @Override
    public void stopDecoder() {

        if(mDecorder!=null) {
            mDecorder.quit();
            mDecorder = null;
            Log.e(Global.TAG_,"stopDecoder");
        }
    }
}
