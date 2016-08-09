package com.example.seokjoo.contactex;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.seokjoo.contactex.global.Global;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.ArrayList;

public class MqttService extends Service implements MqttCallback {
    private static MqttService mInstance;

    MqttClient sampleClient;

    String broker;
    String clientId;

    MemoryPersistence persistence = new MemoryPersistence();

    private DbOpenHelper mDbOpenHelper;

    private Cursor mCursor;
    private DbInfo mInfoClass;
    private ArrayList<DbInfo> mInfoArray;

    //싱글톤
    public static MqttService getInstance(){
        if(mInstance!=null)
            return mInstance;
        else {
            return null;
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
    public void onDestroy(){

        Log.i(Global.TAG, "Service Destroy" );

        //Action Name Broadcast
        Intent intent = new Intent("com.example.service.DESTROY");
        sendBroadcast(intent);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        if(mInstance==null) mInstance = this;



        connectMQTT();


        return super.onStartCommand(intent, flags, startId);
    }

    private void connectMQTT() {
        broker       = "tcp://61.38.158.169:1883";
        clientId     = getDeviceSerialNumber();
        int qos  = 1;

        try {
            sampleClient = new MqttClient(broker, clientId, persistence);


                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);

                sampleClient.connect(connOpts);
                sampleClient.setCallback(this);
                sampleClient.subscribe(Global.Mytopic, qos);

        } catch(MqttException me) {
            Log.i(Global.TAG,"reason "+me.getReasonCode());
            Log.i(Global.TAG,"msg "+me.getMessage());
            Log.i(Global.TAG,"loc "+me.getLocalizedMessage());
            Log.i(Global.TAG,"cause "+me.getCause());
            Log.i(Global.TAG,"excep"+me);

            me.printStackTrace();

        }
    }



    public void publish(String topicto,String message){


        try{
            Log.i(Global.TAG,"Publishing message: "+ message);

            MqttMessage message1= new MqttMessage(message.getBytes());
            sampleClient.getTopic(topicto).publish(message1);

        }catch(MqttException me) {
            Log.i(Global.TAG,"reason "+me.getReasonCode());
            Log.i(Global.TAG,"msg "+me.getMessage());
            Log.i(Global.TAG,"loc "+me.getLocalizedMessage());
            Log.i(Global.TAG,"cause "+me.getCause());
            Log.i(Global.TAG,"excep"+me);

            me.printStackTrace();

        }

    }




    @Override
    public void connectionLost(Throwable cause) {
        Log.e(Global.TAG,cause.getLocalizedMessage());

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(Global.TAG,"Arrived Topic  " + topic);
        Log.i(Global.TAG,"Arrived Message  " + message.toString());

        String recevingMessage = message.toString();


        JSONObject payload = new JSONObject(message.toString());


        if(payload.getString("type").equalsIgnoreCase("contactoffer")){

            if(payload.has("answer")){
                Log.i(Global.TAG,"contact answer 받음");


                mDbOpenHelper = new DbOpenHelper(this);
                mDbOpenHelper.open();
                mInfoArray = new ArrayList<DbInfo>();

                Cursor aCursor = mDbOpenHelper.getMatchPhone(payload.getString("yourphone"));

                if(aCursor.getCount()==0){
                    mDbOpenHelper.insertColumn(payload.getString("yourname"),payload.getString("yourphone"));
                }


                doWhileCursorToArray();
                for (DbInfo i : mInfoArray) {
                    Log.i(Global.TAG, "ID = " + i._id);
                    Log.i(Global.TAG, "name = " + i.name);
                    Log.i(Global.TAG, "phone = " + i.phone);

                }

            }else{
                Log.i(Global.TAG,"contact offer 받음");
                payload.put("answer",true);
                MqttService.getInstance().publish(payload.getString("myphone"),payload.toString());
            }
        }


        if(payload.getString("type").equalsIgnoreCase("calling")){
            Global.ToTopic=payload.getString("myphone");
            Intent intent = new Intent("com.example.service.CALL");
            sendBroadcast(intent);
        }


        if(recevingMessage.equalsIgnoreCase("callcancel")){
            ReceiveActivity.contextMain.finish();

        }else if(recevingMessage.equalsIgnoreCase("receivecancel")){
            CallActivity.contextMain.finish();

        }else if(recevingMessage.equalsIgnoreCase("receiveaccept")){
            Intent intent = new Intent("com.example.service.RECEIVEACCEPT");
            sendBroadcast(intent);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(Global.TAG, "on Rebind" );

    }


    private void doWhileCursorToArray() {

        mCursor = null;
        mCursor = mDbOpenHelper.getAllColumns();
        Log.i(Global.TAG, "COUNT = " + mCursor.getCount());

        while (mCursor.moveToNext()) {

            mInfoClass = new DbInfo(
                    mCursor.getInt(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex("name")),
                    mCursor.getString(mCursor.getColumnIndex("phone"))
            );
            mInfoArray.add(mInfoClass);
        }

        mCursor.close();

    }
}
