package com.moor.im.options.mobileassistant.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.report.model.Plan;

import java.util.List;

/**
 * Created by longwei on 16/7/4.
 */
public class PlanActivity extends BaseActivity {

    private LinearLayout plan_Layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        plan_Layout = (LinearLayout) findViewById(R.id.plan_Layout);

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("联系计划");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        List<Plan> plans = (List<Plan>) getIntent().getSerializableExtra("plan");
        for(int i=0; i<plans.size(); i++) {
            Plan plan = plans.get(i);
            String name = plan.name;
            String time = plan.notifyTime;
            String action = plan.action;

            LinearLayout plan_item = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.workbanch_item_plan, null);
            TextView workbanch_item_plan_tv_name = (TextView) plan_item.findViewById(R.id.workbanch_item_plan_tv_name);
            TextView workbanch_item_plan_tv_time = (TextView) plan_item.findViewById(R.id.workbanch_item_plan_tv_time);
            TextView workbanch_item_plan_tv_action = (TextView) plan_item.findViewById(R.id.workbanch_item_plan_tv_action);
            workbanch_item_plan_tv_name.setText(name);
            workbanch_item_plan_tv_time.setText(time);
            workbanch_item_plan_tv_action.setText(action);

            View view_sp = plan_item.findViewById(R.id.workbanch_item_plan_view_sp);
            if(i == plans.size()-1) {
                view_sp.setVisibility(View.GONE);
            }

            plan_Layout.addView(plan_item);
        }
    }
}
