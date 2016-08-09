package com.example.seokjoo.contactex;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.seokjoo.contactex.global.Global;

import net.frakbot.jumpingbeans.JumpingBeans;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Seokjoo on 2016-08-01.
 */
public class CallActivity extends Activity {

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




        findViewById(R.id.callOFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONObject payload = new JSONObject();
                try{
                    payload.put("type","callcancel");
                }catch(JSONException ex){
                    Log.i(Global.TAG,"json fail " +ex);
                }

                MqttService.getInstance().publish(Global.ToTopic,payload.toString());

                CallActivity.contextMain.finish();

                jp.stopJumping();
            }
        });



        contextMain=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);



    }




}
