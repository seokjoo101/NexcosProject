package com.example.seokjoo.contactex;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.seokjoo.contactex.global.Global;

import java.util.ArrayList;

/**
 * Created by Seokjoo on 2016-08-01.
 */
public class ListViewAdapter extends BaseAdapter
{
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    Context mContext;
    // ListViewAdapter의 생성자
    public ListViewAdapter(Context context) {
        mContext=context;
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView manImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        final ImageButton callImageView = (ImageButton) convertView.findViewById(R.id.imageView2) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        manImageView.setImageDrawable(listViewItem.getManDrawable());
        callImageView.setImageDrawable(listViewItem.getCallDrawable());
        titleTextView.setText(listViewItem.getTitle());
        descTextView.setText(listViewItem.getDesc());


        //클릭 애니메이션
        final Animation anim = AnimationUtils.loadAnimation
                (mContext, // 현재화면 제어권자
                        R.anim.button_click);      // 에니메이션 설정한 파일

        //상대방에게 전화 거는 이미지뷰 클릭
        callImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(Global.TAG, "click " + pos);
                callImageView.startAnimation(anim);
                MqttService.getInstance().publish(Global.ToTopic,"calling");
                mContext.startActivity(new Intent(mContext,CallActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));


            }
        });
        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(Drawable manIcon, String title, String desc,Drawable callIcon) {
        ListViewItem item = new ListViewItem();

        item.setManDrawable(manIcon);
        item.setCallDrawable(callIcon);
        item.setTitle(title);
        item.setDesc(desc);

        listViewItemList.add(item);
    }
}
