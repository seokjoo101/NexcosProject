package com.example.seokjoo.contactex;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.example.seokjoo.contactex.global.Global;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.webrtc.VideoRendererGui;

/**
 * Created by Seokjoo on 2016-08-02.
 */
public class BroadcastReceiver extends android.content.BroadcastReceiver {

    private static PowerManager.WakeLock mWakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        Log.d(Global.TAG,"On receive " + action );


        //Action명으로 받는다
        if (action.equals("com.example.service.DESTROY")) {
            startService(context);
        }else if(action.equals("com.example.service.CALL")){
            releaseWakeLock();
            Intent i= new Intent(context, ReceiveActivity.class );;
            PendingIntent pi_activity = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
            try {
                pi_activity.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if(action.equals("com.example.service.RECEIVEACCEPT")){

            Intent i= new Intent(context, AcceptActivity.class );
            i.putExtra("call",true);
            PendingIntent pi_activity = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);

            try {
                pi_activity.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            CallActivity.contextMain.finish();
        }else if(action.equals("com.example.service.EXIT")){

            Intent i= new Intent(context, MainActivity.class );
            PendingIntent pi_activity = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);

            try {
                pi_activity.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            AcceptActivity.contextMain.finish();
        }

       if(action.equals(intent.ACTION_BOOT_COMPLETED)) {
            liveService(context);
        }else if(action.equals(intent.ACTION_POWER_CONNECTED)) {
            liveService(context);
        }else if(action.equals(intent.ACTION_POWER_DISCONNECTED)) {
            liveService(context);
        } else if(action.equals(intent.ACTION_USER_PRESENT)) {
            liveService(context);
       } else if(action.equals(intent.ACTION_PACKAGE_RESTARTED)) {
            liveService(context);
       } else if(action.equals(intent.ACTION_PACKAGE_FIRST_LAUNCH)) {
            liveService(context);
       }else if(action.equals(intent.ACTION_PACKAGE_ADDED)) {
           liveService(context);
       }

    }

    boolean liveService(Context context){
        ActivityManager manager =  (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);


        if(MqttService.getInstance()!=null){
            if(!MqttService.getInstance().sampleClient.isConnected()) {
                try {
                    MqttService.getInstance().sampleClient.connect();
                } catch (MqttException e) {

                }
            }else{
                Log.i(Global.TAG,"Mqtt : " + MqttService.getInstance().sampleClient.isConnected() );

            }
        }

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            Log.d(Global.TAG,"실행중인 서비스  : "+ service.service.getClassName());

            if (MqttService.class.getName().equals(service.service.getClassName())) {
                Log.i(Global.TAG,"Service on" );
                  return true;
            }
        }
        Log.e(Global.TAG,"Service off" );
         startService(context);

        return false;


    }

    void startService(Context context){

        Intent service = new Intent(context,MqttService.class);
        PendingIntent pi_service = PendingIntent.getService(context, 0, service, PendingIntent.FLAG_ONE_SHOT);
        try {
            pi_service.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

    }



    public static void wakeLock(Context context) {
        if(mWakeLock != null) {
            return;
        }


        PowerManager powerManager =
                (PowerManager)context.getSystemService(
                        Context.POWER_SERVICE);


        mWakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK, Global.TAG);
        mWakeLock.acquire();
    }

    public static void releaseWakeLock() {
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
