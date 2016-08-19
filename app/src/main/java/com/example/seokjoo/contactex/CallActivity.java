package com.example.seokjoo.contactex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.seokjoo.contactex.global.Global;

import net.frakbot.jumpingbeans.JumpingBeans;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seokjoo on 2016-08-01.
 */
public class CallActivity extends Activity{

    public static Activity contextMain;

    VideoViewService videoViewService;
    Intent videoServiceIntent=null;

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


        //클릭 애니메이션
        final Animation anim = AnimationUtils.loadAnimation
                (this, // 현재화면 제어권자
                        R.anim.button_click);      // 에니메이션 설정한 파일


        TextView callingName = (TextView)findViewById(R.id.callingName);
        callingName.setText(Global.ToName);

        findViewById(R.id.callOFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.callOFF).startAnimation(anim);
                JSONObject payload = new JSONObject();
                try{
                    payload.put("type","callcancel");
                }catch(JSONException ex){
                    Log.i(Global.TAG,"json fail " +ex);
                }

                MqttService.getInstance().publish(Global.ToTopic,payload.toString());

                retrunMain();
                //전화끊기
                stopService(videoServiceIntent);
                jp.stopJumping();

            }


        });


    }

    private void retrunMain() {
        startActivity(new Intent(this,MainActivity.class));
        this.finish();
    }

    private void startVideoService() {
        videoServiceIntent = new Intent(this,VideoViewService.class);
        this.startService(videoServiceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            //하드웨어 뒤로가기 버튼에 따른 이벤트 설정
            case KeyEvent.KEYCODE_BACK:
                moveTaskToBack(true);
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

}
