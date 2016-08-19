package com.example.seokjoo.contactex;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.seokjoo.contactex.global.Global;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class MqttService extends Service implements MqttCallback {
    private static MqttService mInstance;

    MqttClient sampleClient;

    String broker;
    String clientId;

    MemoryPersistence persistence = new MemoryPersistence();

    private DbOpenHelper mDbOpenHelper;


    //싱글톤
    public static MqttService getInstance() {
        if (mInstance != null)
            return mInstance;
        else {
            return new MqttService();
        }
    }

    //디바이스 고유의 Serial Number 얻어오는 메서드
    @SuppressLint("NewApi")
    private static String getDeviceSerialNumber() {
        try {
            return (String) Build.class.getField("SERIAL").get(null);
        } catch (Exception ignored) {
            return null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public void onDestroy() {

        Log.i(Global.TAG, "Service Destroy");

        //Action Name Broadcast
        Intent intent = new Intent("com.example.service.DESTROY");
        sendBroadcast(intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Global.TAG, "Service Start" );

        if (mInstance == null) {
            Log.i(Global.TAG, "동적 리시버 등록 " );

            mInstance = this;

            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

            intentFilter.addAction(intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(intent.ACTION_TIME_TICK);
            this.registerReceiver(new com.example.seokjoo.contactex.BroadcastReceiver(),intentFilter);

        }


        Global global =new Global(this);
        Global.Mytopic=global.getString("myTopic");

        if(Global.Mytopic!=null)
             connectMQTT();


        return START_STICKY;
    }

    private void connectMQTT() {
        broker = "tcp://61.38.158.169:1883";

        if(Global.Mytopic!=null)
            clientId = Global.Mytopic;
        else
            clientId = getDeviceSerialNumber();

        int qos = 1;

        try {


            sampleClient = new MqttClient(broker, clientId, persistence);


            if (!sampleClient.isConnected()) {
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setKeepAliveInterval(30);



                sampleClient.connect(connOpts);
                sampleClient.setCallback(this);

                Log.e(Global.TAG, "connect : " + sampleClient.isConnected() + " / " + Global.Mytopic);
                sampleClient.subscribe(Global.Mytopic, qos);
            }
        } catch (MqttException me) {
            Log.e(Global.TAG, "connect fail ");

            Log.i(Global.TAG, "reason " + me.getReasonCode());
            Log.i(Global.TAG, "msg " + me.getMessage());
            Log.i(Global.TAG, "loc " + me.getLocalizedMessage());
            Log.i(Global.TAG, "cause " + me.getCause());
            Log.i(Global.TAG, "excep" + me);

            me.printStackTrace();

        }
    }


    public void publish(String topicto, String message) {


        try {
            Log.i(Global.TAG, "Publishing message: " + message);

            if (sampleClient.isConnected()) {
                MqttMessage message1 = new MqttMessage(message.getBytes());
                sampleClient.getTopic(topicto).publish(message1);
            } else {
                connectMQTT();
            }

        } catch (MqttException me) {

            if (!sampleClient.isConnected()) {
                Log.d(Global.TAG, "publish fail -> reconnect ");
                connectMQTT();

            }


            Log.e(Global.TAG, "publish fail ");
            Log.i(Global.TAG, "reason " + me.getReasonCode());
            Log.i(Global.TAG, "msg " + me.getMessage());
            Log.i(Global.TAG, "loc " + me.getLocalizedMessage());
            Log.i(Global.TAG, "cause " + me.getCause());
            Log.i(Global.TAG, "excep" + me);

            me.printStackTrace();

        }

    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.e(Global.TAG, "connectionLost : " + cause.getLocalizedMessage());

        if (!sampleClient.isConnected()) {
            Log.d(Global.TAG, "connection Lost -> reconnect ");
            connectMQTT();

        }

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(Global.TAG, "Arrived Topic  " + topic);
        Log.i(Global.TAG, "Arrived Message  " + message.toString());


        JSONObject payload = new JSONObject(message.toString());

        if (WebRtcClient.getmInstance() != null)
                WebRtcClient.getmInstance().getMessage(message.toString());

        if (payload.getString("type").equalsIgnoreCase("contactoffer")) {

            if (payload.has("answer")) {
                Log.i(Global.TAG, "contact answer 받음");

                mDbOpenHelper = new DbOpenHelper(this);
                mDbOpenHelper.open();

                Cursor aCursor = mDbOpenHelper.getMatchPhone(payload.getString("yourphone"));

                if (aCursor.getCount() == 0) {
                    mDbOpenHelper.insertColumn(payload.getString("yourname"), payload.getString("yourphone"));
                    ((MainActivity) MainActivity.contextMain).addFriendList();
                }

            } else {
                Log.i(Global.TAG, "contact offer 받음");
                payload.put("answer", true);
                MqttService.getInstance().publish(payload.getString("myphone"), payload.toString());
            }
        }else if (payload.getString("type").equalsIgnoreCase("calling")) {
            Global.ToTopic = payload.getString("myphone");

            Intent intent = new Intent("com.example.service.CALL");
            sendBroadcast(intent);
        } else if (payload.getString("type").equalsIgnoreCase("callcancel")) {
            //전화끊기
            Intent intent = new Intent("com.example.service.CALLCANCEL");
            sendBroadcast(intent);

        } else if (payload.getString("type").equalsIgnoreCase("receivecancel")) {
            //전화끊기
            Intent intent = new Intent("com.example.service.RECEIVECANCEL");
            sendBroadcast(intent);


        } else if (payload.getString("type").equalsIgnoreCase("receiveaccept")) {
            Intent intent = new Intent("com.example.service.RECEIVEACCEPT");
            sendBroadcast(intent);
        }else if (payload.getString("type").equalsIgnoreCase("exit")) {
            Intent intent = new Intent("com.example.service.EXIT");
            sendBroadcast(intent);
        }else if (payload.getString("type").equalsIgnoreCase("recordexit")) {

             Intent intent = new Intent("com.example.service.RECORDEXIT");
            sendBroadcast(intent);
        }
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(Global.TAG, "on Rebind");
    }
}

