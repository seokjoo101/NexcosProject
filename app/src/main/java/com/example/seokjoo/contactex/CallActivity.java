package com.example.seokjoo.contactex;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seokjoo.contactex.global.Global;
import com.example.seokjoo.contactex.global.VideoCodec;

import net.frakbot.jumpingbeans.JumpingBeans;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoRendererGui;

/**
 * Created by Seokjoo on 2016-08-01.
 */
public class CallActivity extends Activity{

    public static Activity contextMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_layout);

        // Append jumping dots
        final TextView textView1 = (TextView) findViewById(R.id.calling);
        final JumpingBeans jp =JumpingBeans.with(textView1)
                .makeTextJump(0, textView1.getText().toString().indexOf(' '))
                .setIsWave(false)
                .setLoopDuration(1000)
                .appendJumpingDots()
                .build();




        contextMain=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        videoViewService=new VideoViewService();
        startVideoService();



        findViewById(R.id.callOFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //전화끊기
                VideoRendererGui.dispose();
                VideoViewService.getInstance().windowManager.removeViewImmediate(VideoViewService.getInstance().windowView);


                JSONObject payload = new JSONObject();
                try{
                    payload.put("type","callcancel");
                }catch(JSONException ex){
                    Log.i(Global.TAG,"json fail " +ex);
                }

                MqttService.getInstance().publish(Global.ToTopic,payload.toString());

                callMain();

                stopService(videoServiceIntent);
                jp.stopJumping();
            }


        });


    }

    private void callMain() {
        startActivity(new Intent(this,MainActivity.class));
        this.finish();
    }

    VideoViewService videoViewService;
    Intent videoServiceIntent=null;
    private void startVideoService() {
        videoServiceIntent = new Intent(this,VideoViewService.class);
        this.startService(videoServiceIntent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoServiceIntent!=null)
            stopService(videoServiceIntent);
    }


}
