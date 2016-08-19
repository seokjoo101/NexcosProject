package com.example.seokjoo.contactex;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.seokjoo.contactex.global.Global;
import com.example.seokjoo.contactex.global.VideoCodec;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

/**
 * Created by Seokjoo on 2016-08-08.
 */
public class AcceptActivity extends Activity implements ScreenDecoder.setDecoderListener,VideoCodec{


    private ScreenDecoder mDecorder;
    private NotificationManager nm = null;
    private Notification notification = null;


    SurfaceView screen;
    Surface surface;

    private MediaProjectionManager mMediaProjectionManager;
    private static final int REQUEST_CODE = 1;
    private ScreenRecorder mRecorder;

    public static Activity contextMain;

    boolean bFaceVisible=false;

    public static boolean bRecordClick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_layout);



        mDecorder= new ScreenDecoder(this);

        WebRtcClient.getmInstance().dataChannel.registerObserver(ScreenDecoder.getInstance());
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        screen=(SurfaceView)findViewById(R.id.screen);
        surface = screen.getHolder().getSurface();
        WebRtcClient.getmInstance().videoSource.stop();


        VideoViewService.getInstance().displaySide();


        //클릭 애니메이션
        final Animation anim = AnimationUtils.loadAnimation
                (this, // 현재화면 제어권자
                        R.anim.button_click);      // 에니메이션 설정한 파일


        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.exit).startAnimation(anim);

                //전화끊기
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


        findViewById(R.id.faceView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.faceView).startAnimation(anim);

                if(bFaceVisible){

                    VideoViewService.getInstance().vsv.setVisibility(View.GONE);
                    WebRtcClient.getmInstance().videoSource.stop();
                    ((ImageButton) findViewById(R.id.faceView)).setImageResource(R.drawable.faceon);

                    surface = screen.getHolder().getSurface();

                    bFaceVisible=false;
                }else{
                    VideoViewService.getInstance().vsv.setVisibility(View.VISIBLE);
                    WebRtcClient.getmInstance().videoSource.restart();
                    ((ImageButton) findViewById(R.id.faceView)).setImageResource(R.drawable.faceoff);

                    bFaceVisible=true;
                }
            }
        });

        findViewById(R.id.recordornot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.recordornot).startAnimation(anim);
                if(mRecorder!=null) {
                    mRecorder.quit();
                    mRecorder = null;
                    ((ImageButton) findViewById(R.id.recordornot)).setImageResource(R.drawable.accept);
                    recordExitMessage();

                }
                else{
                    if(bRecordClick){
                        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
                        startActivityForResult(captureIntent, REQUEST_CODE);
                    }
                    else{
                        toastRecordWarning();
                    }

                }
            }

        });



        contextMain=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }


    void toastRecordWarning(){
        Toast.makeText(this, "상대방이 화면 공유 중 입니다", Toast.LENGTH_SHORT).show();
    }

    private void recordExitMessage() {
        JSONObject payload = new JSONObject();
        try{
            payload.put("type","recordexit");

        }catch(JSONException ex){
            Log.i(Global.TAG,"json fail " +ex);
        }

        MqttService.getInstance().publish(Global.ToTopic,payload.toString());

        Toast.makeText(this, "화면 공유를 종료합니다.", Toast.LENGTH_SHORT).show();

        nm.cancelAll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }
        ((ImageButton) findViewById(R.id.recordornot)).setImageResource(R.drawable.deny);

        moveTaskToBack(true);

        mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection);
        mRecorder.start();


        Toast.makeText(this, "화면 공유를 시작합니다.", Toast.LENGTH_SHORT).show();


        recordNotify();
    }



    public void recordNotify(){

        Intent intent = new Intent(getApplicationContext(),AcceptActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Nexcos")
                .setContentText("화면 공유가 진행중입니다")
                .setSmallIcon(R.drawable.ic_videocam_icon)
                .setContentIntent(pendingIntent)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR;

        nm.notify(1234, notification);


    }

    private void moveMain() {
        Toast.makeText(this, "통화가 종료되었습니다", Toast.LENGTH_SHORT).show();
        stopService(new Intent(this,VideoViewService.class));
        this.finish();
    }


    @Override
    public void startDecoder(ByteBuffer buffer) {

        if(mDecorder==null)
            mDecorder= new ScreenDecoder(this);

        mDecorder.init(surface,buffer);
         Log.e(Global.TAG_,"startDecoder");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(nm!=null)
            nm.cancelAll();
        if(mRecorder!=null) {
            mRecorder.quit();
            mRecorder = null;
        }

        if(mDecorder!=null) {
            stopDecoder();
        }



    }

    @Override
    public void stopDecoder() {

        if(mDecorder!=null) {
            mDecorder.quit();
            mDecorder = null;
            Log.e(Global.TAG_,"stopDecoder");
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            //하드웨어 뒤로가기 버튼에 따른 이벤트 설정
            case KeyEvent.KEYCODE_BACK:
                moveTaskToBack(true);
                mDecorder.configured=false;

            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
