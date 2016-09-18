package com.moor.im.options.mobileassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.cdr.activity.CdrActivity;
import com.moor.im.options.mobileassistant.customer.activity.CustomerActivity;
import com.moor.im.options.mobileassistant.customer.activity.CustomerDetailActivity;
import com.moor.im.options.mobileassistant.erp.activity.ErpActivity;
import com.moor.im.options.mobileassistant.report.ReportActivity;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * Created by longwei on 2016/5/12.
 */
public class MAActivity extends BaseActivity{

    LinearLayout ma_cdr, ma_erp, ma_report, ma_customer;
    View ma_cdr_sp, ma_erp_sp, ma_report_sp, ma_customer_sp;
    private JSONArray userLimitArray;
    private boolean showCall = true, showErp = true, showReport = true, showCustomer = true;

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
        ma_customer = (LinearLayout) findViewById(R.id.ma_customer);

        ma_cdr_sp = findViewById(R.id.ma_cdr_sp);
        ma_erp_sp = findViewById(R.id.ma_erp_sp);
        ma_report_sp = findViewById(R.id.ma_report_sp);
        ma_customer_sp = findViewById(R.id.ma_customer_sp);

//        userLimitArray = MobileApplication.cacheUtil.getAsJSONArray("userLimit");
//        if(userLimitArray != null && userLimitArray.length() > 0) {
//            try {
//                for (int i = 0; i < userLimitArray.length(); i++) {
//
//                    if ("nav_call".equals(userLimitArray.getString(i))) {
//                        showCall = true;
//                    }
//                    if ("nav_business".equals(userLimitArray.getString(i))) {
//                        showErp = true;
//                    }
//                    if ("nav_report".equals(userLimitArray.getString(i))) {
//                        showReport = true;
//                    }
//                    if ("nav_customer".equals(userLimitArray.getString(i))) {
//                        showCustomer = true;
//                    }
//                }
//            }catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

        if(!showCall) {
            ma_cdr.setVisibility(View.GONE);
            ma_cdr_sp.setVisibility(View.GONE);
        }
        if(!showErp) {
            ma_erp.setVisibility(View.GONE);
            ma_erp_sp.setVisibility(View.GONE);
        }
        if(!showReport) {
            ma_report.setVisibility(View.GONE);
            ma_report_sp.setVisibility(View.GONE);
        }
        if(!showCustomer) {
            ma_customer.setVisibility(View.GONE);
            ma_customer_sp.setVisibility(View.GONE);
        }


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
        ma_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reportIntent = new Intent(MAActivity.this, CustomerActivity.class);
                startActivity(reportIntent);
            }
        });
    }


}
