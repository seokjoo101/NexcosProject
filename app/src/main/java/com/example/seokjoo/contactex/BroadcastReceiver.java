package com.example.seokjoo.contactex;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.example.seokjoo.contactex.global.Global;

/**
 * Created by Seokjoo on 2016-08-02.
 */
public class BroadcastReceiver extends android.content.BroadcastReceiver {

    private static PowerManager.WakeLock mWakeLock;
    DbOpenHelper mDbOpenHelper;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        Log.d(Global.TAG,"On receive " + action );




        //Action명으로 받는다
        if (action.equals("com.example.service.DESTROY")) {
            startService(context);
        }else if(action.equals("com.example.service.CALL")){


            releaseWakeLock();


            mDbOpenHelper = new DbOpenHelper(context);
            mDbOpenHelper.open();

            Cursor aCursor = mDbOpenHelper.getMatchPhone(Global.ToTopic);
            Cursor bCursor = mDbOpenHelper.getColumn(aCursor.getCount());

            if (aCursor.getCount() != 0) {
                Global.ToName=bCursor.getString(bCursor.getColumnIndex("name"));

            }

            Intent i= new Intent(context, ReceiveActivity.class );;
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } else if(action.equals("com.example.service.RECEIVEACCEPT")){


            Intent i= new Intent(context, AcceptActivity.class );
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            if(CallActivity.contextMain!=null)
                CallActivity.contextMain.finish();
        } else if(action.equals("com.example.service.CALLCANCEL")){
            //전화끊기
            context.stopService(new Intent(context,VideoViewService.class));
//            context.startActivity(new Intent(context,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            if(ReceiveActivity.contextMain!=null)
                ReceiveActivity.contextMain.finish();

        } else if(action.equals("com.example.service.RECEIVECANCEL")){
            //전화끊기
            context.stopService(new Intent(context,VideoViewService.class));
            context.startActivity(new Intent(context,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            if(CallActivity.contextMain!=null)
                CallActivity.contextMain.finish();
            Toast.makeText(context, "상대방이 통화를 거절했습니다", Toast.LENGTH_SHORT).show();
        } else if(action.equals("com.example.service.EXIT")){
            Toast.makeText(context, "통화가 종료되었습니다", Toast.LENGTH_SHORT).show();
            context.stopService(new Intent(context,VideoViewService.class));
            if(AcceptActivity.contextMain!=null)
                AcceptActivity.contextMain.finish();
        }else if(action.equals("com.example.service.RECORDEXIT")){

            Toast.makeText(context, "상대방이 화면 공유를 종료합니다.", Toast.LENGTH_SHORT).show();
            AcceptActivity.bRecordClick=true;

        }

       if(action.equals(intent.ACTION_BOOT_COMPLETED) || action.equals(intent.ACTION_POWER_CONNECTED) || action.equals(intent.ACTION_POWER_DISCONNECTED)
               || action.equals(intent.ACTION_USER_PRESENT) || action.equals(intent.ACTION_SCREEN_OFF) || action.equals(intent.ACTION_TIME_TICK)) {
           liveService(context);
       }else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                NetworkInfo _wifi_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(_wifi_network != null) {

                    if(_wifi_network != null && activeNetInfo != null){
                        Log.d(Global.TAG,"wifi, 3g 둘 중 하나라도 있을 경우 " );
                        liveService(context);
                    }
                    else{
                        Log.d(Global.TAG,"wifi, 3g 둘 다 없을 경우 "  );
                    }
                }
            } catch (Exception e) {
                Log.i("ULNetworkReceiver", e.getMessage());
            }
        }

    }

    boolean liveService(Context context){
        ActivityManager manager =  (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);



        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            Log.d(Global.TAG,"실행중인 서비스  : "+ service.service.getClassName());

            if (MqttService.class.getName().equals(service.service.getClassName())) {
                Log.i(Global.TAG,"Service on" );

                if(!MqttService.getInstance().sampleClient.isConnected()) startService(context);
                else Log.i(Global.TAG,"MQTT on" );
                return true;
            }
        }
        Log.e(Global.TAG,"Service off" );
         startService(context);

        return false;

    }


    void startService(Context context){
        Intent service = new Intent(context,MqttService.class);
        context.startService(service);
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
