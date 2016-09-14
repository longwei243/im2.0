package com.moor.im.options.mobileassistant.customer.activity;

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
import com.moor.im.options.mobileassistant.customer.fragment.AllCustomerFragment;
import com.moor.im.options.mobileassistant.customer.fragment.MyCustomerFragment;

/**
 * Created by longwei on 16/8/23.
 */
public class CustomerActivity extends BaseActivity{

    private RelativeLayout mycustomer_layout, allcustomer_layout;
    private TextView mycustomer_text, allcustomer_text;
    private Fragment mycustomer_fragment, allcustomer_fragment;
    private FragmentManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma_customer);

        mycustomer_layout = (RelativeLayout) findViewById(R.id.mycustomer_layout);
        allcustomer_layout = (RelativeLayout) findViewById(R.id.allcustomer_layout);
        mycustomer_text = (TextView) findViewById(R.id.mycustomer_text);
        allcustomer_text = (TextView) findViewById(R.id.allcustomer_text);

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("客户");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mycustomer_fragment = new MyCustomerFragment();
        allcustomer_fragment = new AllCustomerFragment();

        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragment_layout, mycustomer_fragment)
                .commit();

        mycustomer_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mycustomer_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg_normal));
                mycustomer_text.setTextColor(Color.WHITE);

                allcustomer_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg));
                allcustomer_text.setTextColor(getResources().getColor(R.color.maincolor));

                fm.beginTransaction()
                        .hide(allcustomer_fragment)
                        .show(mycustomer_fragment)
                        .commit();
            }
        });

        allcustomer_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mycustomer_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg));
                mycustomer_text.setTextColor(getResources().getColor(R.color.maincolor));

                allcustomer_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg_normal));
                allcustomer_text.setTextColor(Color.WHITE);

                if(allcustomer_fragment.isAdded()) {
                    fm.beginTransaction()
                            .hide(mycustomer_fragment)
                            .show(allcustomer_fragment)
                            .commit();
                }else {
                    fm.beginTransaction()
                            .hide(mycustomer_fragment)
                            .add(R.id.fragment_layout, allcustomer_fragment)
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
