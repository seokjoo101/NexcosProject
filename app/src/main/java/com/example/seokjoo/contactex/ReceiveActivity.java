package com.example.seokjoo.contactex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.seokjoo.contactex.global.Global;

import net.frakbot.jumpingbeans.JumpingBeans;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seokjoo on 2016-08-01.
 */
public class ReceiveActivity extends Activity {

    public static Activity contextMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_layout);

        contextMain=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Append jumping dots
        final TextView textView1 = (TextView) findViewById(R.id.receiving);
        final JumpingBeans jp =JumpingBeans.with(textView1)
                .makeTextJump(0, textView1.getText().toString().indexOf(' '))
                .setIsWave(false)
                .setLoopDuration(1000)
                .appendJumpingDots()
                .build();


        findViewById(R.id.acccept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //전화 받았을때 peer connect 연결

                JSONObject payload = new JSONObject();
                try{
                    payload.put("type","receiveaccept");
                }catch(JSONException ex){
                    Log.i(Global.TAG,"json fail " +ex);
                }

                MqttService.getInstance().publish(Global.ToTopic,payload.toString());
                clickAccept();
                jp.stopJumping();

            }
        });
        findViewById(R.id.deny).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONObject payload = new JSONObject();
                try{
                    payload.put("type","receivecancel");
                }catch(JSONException ex){
                    Log.i(Global.TAG,"json fail " +ex);
                }

                MqttService.getInstance().publish(Global.ToTopic,payload.toString());

                ReceiveActivity.contextMain.finish();
                jp.stopJumping();

            }
        });


     }


    void clickAccept(){
        Intent i= new Intent(this, AcceptActivity.class );
        startActivity(i);
        ReceiveActivity.contextMain.finish();
    }
}
