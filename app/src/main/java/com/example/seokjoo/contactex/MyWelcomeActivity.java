package com.example.seokjoo.contactex;

import com.stephentuso.welcome.WelcomeScreenBuilder;
import com.stephentuso.welcome.ui.WelcomeActivity;
import com.stephentuso.welcome.util.WelcomeScreenConfiguration;

/**
 * Created by Seokjoo on 2016-08-16.
 */
public class MyWelcomeActivity extends WelcomeActivity {
    @Override
    protected WelcomeScreenConfiguration configuration() {
        return new WelcomeScreenBuilder(this)
                .theme(R.style.CustomWelcomeScreenTheme)
                .titlePage(R.drawable.info_face, "반갑습니다", R.color.orange_background)
                .titlePage(R.drawable.info1, "Nexcos는 핸드폰 화면을 \n공유하는 동시에 음성 통화\n혹은 영상 통화를 할 수\n있습니다", R.color.purple_background)
                .basicPage(R.drawable.info2, "영상 회의", "동료와 내 핸드폰 화면을 보면서 \n영상회의를 할 수 있습니다", R.color.darkblue_background)
                .basicPage(R.drawable.info3, "친구와 정보 공유", "여행 계획을 짜거나 약속을 정할 때 \n" + "편리합니다", R.color.red_background)
                .basicPage(R.drawable.info4, "함께 보는 비디오", "떨어져 있어도 같은 곳에서 함께 비디오를 \n보는 듯한 재미를 느낄 수 있습니다 ", R.color.teal_background)
                .titlePage(R.drawable.info5, "\n그럼 시작해볼까요?", R.color.blue_background)
                .swipeToDismiss(true)
                .exitAnimation(android.R.anim.fade_out)
                .build();
    }
}
