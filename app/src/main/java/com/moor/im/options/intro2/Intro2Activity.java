package com.moor.im.options.intro2;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import com.moor.im.R;
import com.moor.im.options.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 16/9/19.
 */
public class Intro2Activity extends AhoyOnboarderActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AhoyOnboarderCard ahoyOnboarderCard1 = new AhoyOnboarderCard("客服助手", "处理工单更及时", R.drawable.intro_kefu);
        AhoyOnboarderCard ahoyOnboarderCard2 = new AhoyOnboarderCard("网络电话", "电话沟通更便宜", R.drawable.intro_phone);
        AhoyOnboarderCard ahoyOnboarderCard3 = new AhoyOnboarderCard("在线聊天", "联系同事更方便", R.drawable.intro_chat);

        ahoyOnboarderCard1.setBackgroundColor(R.color.white);
        ahoyOnboarderCard2.setBackgroundColor(R.color.white);
        ahoyOnboarderCard3.setBackgroundColor(R.color.white);

        List<AhoyOnboarderCard> pages = new ArrayList<>();

        pages.add(ahoyOnboarderCard1);
        pages.add(ahoyOnboarderCard2);
        pages.add(ahoyOnboarderCard3);

        for (AhoyOnboarderCard page : pages) {
            page.setTitleColor(R.color.black);
            page.setDescriptionColor(R.color.grey_600);
        }

        setFinishButtonTitle("开启");
        showNavigationControls(false);

        List<Integer> colorList = new ArrayList<>();
        colorList.add(R.color.solid_one);
        colorList.add(R.color.solid_two);
        colorList.add(R.color.solid_three);

        setColorBackground(colorList);

        setOnboardPages(pages);
    }

    @Override
    public void onFinishButtonPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
