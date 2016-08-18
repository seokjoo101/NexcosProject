package com.example.seokjoo.contactex;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.example.seokjoo.contactex.global.Global;
import com.stephentuso.welcome.WelcomeScreenHelper;

import java.util.ArrayList;

public class MainActivity extends Activity {

    ListView listview ;
    ListViewAdapter adapter;


    private DbOpenHelper mDbOpenHelper;

    private DbInfo mInfoClass;
    private ArrayList<DbInfo> mInfoArray;
    private Cursor mCursor;
    public static Activity contextMain;

    WelcomeScreenHelper welcomeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeScreen = new WelcomeScreenHelper(this, MyWelcomeActivity.class);

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

        contextMain=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        //클릭 애니메이션
        final Animation anim = AnimationUtils.loadAnimation
                (this, // 현재화면 제어권자
                        R.anim.button_click);      // 에니메이션 설정한 파일

        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.info).startAnimation(anim);
                welcomeScreen.forceShow();
            }
        });

        findViewById(R.id.sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.sms).startAnimation(anim);
                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.putExtra("address", "01094037171");
                intent.putExtra("sms_body", "http://nexcos.com 에서 Nexcos를 설치해서 친구를 추가하세요.");
                intent.setType("vnd.android-dir/mms-sms");
                startActivity(intent);
            }
        });

    }

    public void addFriendList() {

        mCursor = null;
        mCursor = mDbOpenHelper.getAllColumns();
        Log.i(Global.TAG, "COUNT = " + mCursor.getCount());

        while (mCursor.moveToNext()) {

        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_face_black_36dp),
                mCursor.getString(mCursor.getColumnIndex("name")), mCursor.getString(mCursor.getColumnIndex("phone")),ContextCompat.getDrawable(this,R.drawable.call_mip));

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
