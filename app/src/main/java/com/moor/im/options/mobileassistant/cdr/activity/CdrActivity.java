package com.moor.im.options.mobileassistant.cdr.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.cdr.fragment.AllCdrFragment;
import com.moor.im.options.mobileassistant.cdr.fragment.MyCdrFragment;

/**
 * Created by longwei on 2016/5/3.
 */
public class CdrActivity extends BaseActivity{

    private RelativeLayout mycdr_layout, allcdr_layout;
    private TextView mycdr_text, allcdr_text;
    private Fragment mycdr_fragment, allcdr_fragment;
    private FragmentManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma_cdr);

        mycdr_layout = (RelativeLayout) findViewById(R.id.mycdr_layout);
        allcdr_layout = (RelativeLayout) findViewById(R.id.allcdr_layout);
        mycdr_text = (TextView) findViewById(R.id.mycdr_text);
        allcdr_text = (TextView) findViewById(R.id.allcdr_text);

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("通话记录");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mycdr_fragment = new MyCdrFragment();
        allcdr_fragment = new AllCdrFragment();

        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragment_layout, mycdr_fragment)
                .commit();

        mycdr_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mycdr_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg_normal));
                mycdr_text.setTextColor(Color.WHITE);

                allcdr_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg));
                allcdr_text.setTextColor(getResources().getColor(R.color.maincolor));

                fm.beginTransaction()
                        .hide(allcdr_fragment)
                        .show(mycdr_fragment)
                        .commit();
            }
        });

        allcdr_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mycdr_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg));
                mycdr_text.setTextColor(getResources().getColor(R.color.maincolor));

                allcdr_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg_normal));
                allcdr_text.setTextColor(Color.WHITE);

                if(allcdr_fragment.isAdded()) {
                    fm.beginTransaction()
                            .hide(mycdr_fragment)
                            .show(allcdr_fragment)
                            .commit();
                }else {
                    fm.beginTransaction()
                            .hide(mycdr_fragment)
                            .add(R.id.fragment_layout, allcdr_fragment)
                            .commit();
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobileAssitantCache.getInstance().clear();
    }
}
