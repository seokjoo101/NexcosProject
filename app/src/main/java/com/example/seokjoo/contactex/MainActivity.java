package com.example.seokjoo.contactex;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import com.example.seokjoo.contactex.global.Global;

import org.eclipse.paho.client.mqttv3.MqttCallback;

public class MainActivity extends Activity {

    private WebRtcClient client;

    ListView listview ;
    ListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Context mContext;
        mContext =  getApplicationContext();
        // Adapter 생성
        adapter = new ListViewAdapter(mContext) ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.list);
        listview.setAdapter(adapter);


        phoneBook();

        startService(new Intent(this,MqttService.class));
        Log.i(Global.TAG,"Mytopic " +Global.Mytopic);
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

                    // 개별 연락처 삭제
                    // rowNum = getBaseContext().getContentResolver().delete(RawContacts.CONTENT_URI, RawContacts._ID+ " =" + id,null);

                    // LogCat에 로그 남기기
                    if(phone[count]!=null){

                        Log.i("ANDROES", "id=" + id +", name["+count+"]=" + name[count]+", phone["+count+"]=" + phone[count]);

                        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_face_black_36dp),
                                name[count], phone[count],ContextCompat.getDrawable(this,R.drawable.ic_videocam_black_36dp));

                        phone_num[phone_num_count++]=phone[count];


                     }
                    count++;

                } while(cursor.moveToNext() || count > end);
            }
            //내 전화번호 찾기
            TelephonyManager telManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            String phoneNum = telManager.getLine1Number();

            Log.e("ANDROES", "내 전화번호 " + phoneNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
