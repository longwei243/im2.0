package com.moor.im.options.mobileassistant.report;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.report.fragment.ReportFragment;
import com.moor.im.options.mobileassistant.report.fragment.WorkbanchFragment;

/**
 * Created by longwei on 2016/6/8.
 */
public class ReportActivity extends BaseActivity{

    private RelativeLayout workbanch_layout, report_layout;
    private TextView workbanch_text, report_text;
    private Fragment workbanch_fragment, report_fragment;
    private FragmentManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma_report);

        workbanch_layout = (RelativeLayout) findViewById(R.id.workbanch_layout);
        report_layout = (RelativeLayout) findViewById(R.id.report_layout);
        workbanch_text = (TextView) findViewById(R.id.workbanch_text);
        report_text = (TextView) findViewById(R.id.report_text);

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("仪表盘");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        workbanch_fragment = new WorkbanchFragment();
        report_fragment = new ReportFragment();

        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragment_layout, workbanch_fragment)
                .commit();

        workbanch_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workbanch_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg_normal));
                workbanch_text.setTextColor(Color.WHITE);

                report_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg));
                report_text.setTextColor(getResources().getColor(R.color.maincolor));

                fm.beginTransaction()
                        .hide(report_fragment)
                        .show(workbanch_fragment)
                        .commit();
            }
        });

        report_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workbanch_layout.setBackground(getResources().getDrawable(R.drawable.list_left_bg));
                workbanch_text.setTextColor(getResources().getColor(R.color.maincolor));

                report_layout.setBackground(getResources().getDrawable(R.drawable.list_right_bg_normal));
                report_text.setTextColor(Color.WHITE);

                if(report_fragment.isAdded()) {
                    fm.beginTransaction()
                            .hide(workbanch_fragment)
                            .show(report_fragment)
                            .commit();
                }else {
                    fm.beginTransaction()
                            .hide(workbanch_fragment)
                            .add(R.id.fragment_layout, report_fragment)
                            .commit();
                }

            }
        });

    }

}
