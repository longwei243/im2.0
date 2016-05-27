package com.moor.im.options.mobileassistant.erp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.chat.holder.ImageViewHolder;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.erp.fragment.DBFragment;
import com.moor.im.options.mobileassistant.erp.fragment.DLQFragment;

/**
 * Created by longwei on 2016/5/4.
 */
public class ErpActivity extends BaseActivity{

    private RelativeLayout dlq_layout, db_layout;
    private TextView dlq_text, db_text;
    private ImageView dlq_icon, db_icon;
    private Fragment dlq_fragment, db_fragment;
    private FragmentManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma_erp);

        dlq_layout = (RelativeLayout) findViewById(R.id.dlq_layout);
        db_layout = (RelativeLayout) findViewById(R.id.db_layout);
        dlq_text = (TextView) findViewById(R.id.dlq_text);
        db_text = (TextView) findViewById(R.id.db_text);

        dlq_icon = (ImageView) findViewById(R.id.dlq_icon);
        db_icon = (ImageView) findViewById(R.id.db_icon);

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("工单助手");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        dlq_fragment = new DLQFragment();
        db_fragment = new DBFragment();

        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragment_layout, dlq_fragment)
                .commit();

        dlq_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlq_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg_normal));
                dlq_text.setTextColor(Color.WHITE);
                dlq_icon.setImageResource(R.drawable.erp_dlq_icon_1);

                db_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg));
                db_text.setTextColor(getResources().getColor(R.color.grey));
                db_icon.setImageResource(R.drawable.erp_db_icon_2);

                fm.beginTransaction()
                        .hide(db_fragment)
                        .show(dlq_fragment)
                        .commit();
            }
        });

        db_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlq_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg));
                dlq_text.setTextColor(getResources().getColor(R.color.grey));
                dlq_icon.setImageResource(R.drawable.erp_dlq_icon_2);

                db_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg_normal));
                db_text.setTextColor(Color.WHITE);
                db_icon.setImageResource(R.drawable.erp_db_icon_1);

                if(db_fragment.isAdded()) {
                    fm.beginTransaction()
                            .hide(dlq_fragment)
                            .show(db_fragment)
                            .commit();
                }else {
                    fm.beginTransaction()
                            .hide(dlq_fragment)
                            .add(R.id.fragment_layout, db_fragment)
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
