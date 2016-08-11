package com.example.seokjoo.contactex;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.seokjoo.contactex.global.Global;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seokjoo on 2016-08-09.
 */
public class SplashActivity extends Activity {

     Handler hd ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);

        hd = new Handler();


        //내 전화번호 찾기
        TelephonyManager telManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        Global.Mytopic = telManager.getLine1Number().substring(1);

        Log.e("ANDROES", "내 전화번호 " + Global.Mytopic);


        startService(new Intent(getApplication(),MqttService.class));

        new MyTask().execute();

    }


    @Override
    protected void onStart() {
        super.onStart();


    }


    private class splashhandler implements Runnable{
        public void run() {

                startActivity(new Intent(getApplication(), MainActivity.class)); // 로딩이 끝난후 이동할 Activity
                SplashActivity.this.finish(); // 로딩페이지 Activity Stack에서 제거

        }
    }



    private Cursor getURI()
    {
        // 주소록 URI
        Uri people = ContactsContract.Contacts.CONTENT_URI;

        // 검색할 컬럼 정하기
        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER };

        // 쿼리 날려서 커서 얻기
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        // managedquery 는 activity 메소드이므로 아래와 같이 처리함
        return getContentResolver().query(people, projection, null, selectionArgs, sortOrder);
        // return managedQuery(people, projection, null, selectionArgs, sortOrder);
    }

    public void phoneBook() {
        try {
            Cursor cursor = getURI();                    // 전화번호부 가져오기

            int end = cursor.getCount();                // 전화번호부의 갯수 세기
            Log.i("ANDROES", "end = "+end);

            String [] name = new String[end];    // 전화번호부의 이름을 저장할 배열 선언
            String [] phone = new String[end];    // 전화번호부의 이름을 저장할 배열 선언
            int count = 0;

            String[] phone_num = new String[end];
            int phone_num_count = 0;



            if(cursor.moveToFirst())
            {
                // 컬럼명으로 컬럼 인덱스 찾기
                int idIndex = cursor.getColumnIndex("_id");

                do
                {

                    // 요소값 얻기
                    int id = cursor.getInt(idIndex);
                    String phoneChk = cursor.getString(2);
                    if (phoneChk.equals("1")) {
                        Cursor phones = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                        + " = " + id, null, null);

                        while (phones.moveToNext()) {
                            phone[count] = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                        }
                    }
                    name[count] = cursor.getString(1);

                    // LogCat에 로그 남기기
                    if(phone[count]!=null){

                        Log.i("ANDROES", "id=" + id +", name["+count+"]=" + name[count]+", phone["+count+"]=" + phone[count]);



                        phone_num[phone_num_count++]=phone[count];



                        contactOffer(name[count],phone[count].substring(1));


                    }
                    count++;
                 } while(cursor.moveToNext() || count > end);


             }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void contactOffer(String name,String phone){
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", "contactoffer");
            payload.put("myphone", Global.Mytopic);
            payload.put("yourname", name);
            payload.put("yourphone", phone);

            MqttService.getInstance().publish(phone, payload.toString());
        }catch (JSONException ex){
            Log.i(Global.TAG,"json fail " +ex);
        }
    }


    public class MyTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            //백그라운드 작업 전 UI 작업

            super.onPreExecute();
            Log.e("ANDROES", "1 ");
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Log.e("ANDROES", "2 ");

            phoneBook();
            return null;
        }

        //성공, 실패로 나누어서 실패시 재연결로 하기
        @Override
        protected void onPostExecute(Object o) {
            Log.e("ANDROES", "3 ");

            super.onPostExecute(o);
            hd.post(new splashhandler());
        }

    }

}
