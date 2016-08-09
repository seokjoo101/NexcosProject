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
import android.widget.ListView;

import com.example.seokjoo.contactex.global.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebRtcClient client;

    ListView listview ;
    ListViewAdapter adapter;


    private DbOpenHelper mDbOpenHelper;

    private DbInfo mInfoClass;
    private ArrayList<DbInfo> mInfoArray;
    private Cursor mCursor;

    String myPhoneNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mInfoArray = new ArrayList<DbInfo>();


        // Adapter 생성
        adapter = new ListViewAdapter(getApplicationContext()) ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.list);
        listview.setAdapter(adapter);





        addFriendList();
        Log.i(Global.TAG,"Mytopic " +Global.Mytopic);


        for (DbInfo i : mInfoArray) {
            Log.i(Global.TAG, "ID = " + i._id);
            Log.i(Global.TAG, "name = " + i.name);
            Log.i(Global.TAG, "phone = " + i.phone);
        }
    }



    private void addFriendList() {

        mCursor = null;
        mCursor = mDbOpenHelper.getAllColumns();
        Log.i(Global.TAG, "COUNT = " + mCursor.getCount());

        while (mCursor.moveToNext()) {

        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_face_black_36dp),
                mCursor.getString(mCursor.getColumnIndex("name")), mCursor.getString(mCursor.getColumnIndex("phone")),ContextCompat.getDrawable(this,R.drawable.ic_videocam_black_36dp));

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
