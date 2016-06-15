package com.moor.im.options.mobileassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.cdr.activity.CdrActivity;
import com.moor.im.options.mobileassistant.erp.activity.ErpActivity;
import com.moor.im.options.mobileassistant.report.ReportActivity;


/**
 * Created by longwei on 2016/5/12.
 */
public class MAActivity extends BaseActivity{

    LinearLayout ma_cdr, ma_erp, ma_report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ma);

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("客服助手");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ma_cdr = (LinearLayout) findViewById(R.id.ma_cdr);
        ma_erp = (LinearLayout) findViewById(R.id.ma_erp);
        ma_report = (LinearLayout) findViewById(R.id.ma_report);

        ma_cdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mobileIntent = new Intent(MAActivity.this, CdrActivity.class);
				startActivity(mobileIntent);
            }
        });
        ma_erp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent erpIntent = new Intent(MAActivity.this, ErpActivity.class);
				startActivity(erpIntent);
            }
        });
        ma_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reportIntent = new Intent(MAActivity.this, ReportActivity.class);
				startActivity(reportIntent);
            }
        });
    }


}
