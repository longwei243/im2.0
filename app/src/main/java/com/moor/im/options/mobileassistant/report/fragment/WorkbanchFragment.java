package com.moor.im.options.mobileassistant.report.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.report.view.RoundProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/6/13.
 */
public class WorkbanchFragment extends BaseLazyFragment{
    private RoundProgressBar rpb_callin, rpb_callout;
    private LinearLayout workbanch_ll_plan, workbanch_customer_ll_line1, workbanch_customer_ll_line2;
    private TextView workbanch_callin_tv_timelength, workbanch_callin_tv_link, workbanch_callin_tv_count;
    private TextView workbanch_callout_tv_timelength, workbanch_callout_tv_link, workbanch_callout_tv_count;
    private TextView workbanch_erp_tv_dcl, workbanch_erp_tv_dlq;
    private TextView workbanch_webchat_tv_count, workbanch_pc_tv_count, workbanch_app_tv_count, workbanch_email_tv_count;
    private ProgressBar workbanch_webchat_pb,workbanch_pc_pb,workbanch_app_pb,workbanch_email_pb;

    private LinearLayout workbanch_layout;
    private LoadingDialog loadingDialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workbanch, null);
        loadingDialog = new LoadingDialog();
        initViews(view);
        loadingDialog.show(getFragmentManager(), "");
        HttpManager.getInstance().getWorkBenchInfo(UserDao.getInstance().getUser()._id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(String s) {
                        initData(s);
                    }
                });
        return view;
    }

    private void initData(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            //联系计划
            JSONArray planArray = jsonObject.getJSONArray("dateList");
            workbanch_ll_plan.removeAllViews();
            if(planArray.length() <= 3) {
                for(int i=0; i<planArray.length(); i++) {
                    JSONObject plan = planArray.getJSONObject(i);
                    String name = plan.getString("name");
                    String time = plan.getString("notifyTime");
                    String action = plan.getString("action");

                    LinearLayout plan_item = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.workbanch_item_plan, null);
                    TextView workbanch_item_plan_tv_name = (TextView) plan_item.findViewById(R.id.workbanch_item_plan_tv_name);
                    TextView workbanch_item_plan_tv_time = (TextView) plan_item.findViewById(R.id.workbanch_item_plan_tv_time);
                    TextView workbanch_item_plan_tv_action = (TextView) plan_item.findViewById(R.id.workbanch_item_plan_tv_action);
                    workbanch_item_plan_tv_name.setText(name);
                    workbanch_item_plan_tv_time.setText(time);
                    workbanch_item_plan_tv_action.setText(action);

                    workbanch_ll_plan.addView(plan_item);
                }
            }else {

            }

            //通话情况
            JSONArray callArray = jsonObject.getJSONArray("todaySummary");
            for (int i=0; i<callArray.length(); i++) {
                JSONObject call = callArray.getJSONObject(i);
                if("normal".equals(call.getString("_id"))) {
                    int time = call.getInt("linkedTime");
                    int count = call.getInt("count");
                    int link = call.getInt("linked");
                    int progress = (link  * 100 / count);

                    workbanch_callin_tv_timelength.setText("总通话时长:"+TimeUtil.getContactsLogTime(time)+"秒");
                    workbanch_callin_tv_link.setText(link+"");
                    workbanch_callin_tv_count.setText(count+"");
                    rpb_callin.setProgress(progress);
                }

                if("dialout".equals(call.getString("_id"))) {
                    int time = call.getInt("linkedTime");
                    int count = call.getInt("count");
                    int link = call.getInt("linked");
                    int progress = (link  * 100 / count);

                    workbanch_callout_tv_timelength.setText("总通话时长:"+TimeUtil.getContactsLogTime(time)+"秒");
                    workbanch_callout_tv_link.setText(link+"");
                    workbanch_callout_tv_count.setText(count+"");
                    rpb_callout.setProgress(progress);
                }
            }

            //工单
            int busToClaim = jsonObject.getInt("busToClaim");
            int busToDeal = jsonObject.getInt("busToDeal");
            workbanch_erp_tv_dcl.setText(busToDeal+"");
            workbanch_erp_tv_dlq.setText(busToClaim+"");

            //多渠道
            int totalCount = 0;
            int pcCount = 0;
            int webchatCount = 0;
            int appCount = 0;
            int emailCount = 0;
            JSONArray channelArray = jsonObject.getJSONArray("multichannel");
            for(int i=0; i<channelArray.length(); i++) {
                JSONObject channel = channelArray.getJSONObject(i);

                if("pc".equals(channel.getString("_id")) || "web".equals(channel.getString("_id")) || "wap".equals(channel.getString("_id"))) {
                    pcCount += channel.getInt("count");
                }else if("weixin".equals(channel.getString("_id"))) {
                    webchatCount = channel.getInt("count");
                }else if("sdk".equals(channel.getString("_id"))) {
                    appCount = channel.getInt("count");
                }else if("email".equals(channel.getString("_id"))) {
                    emailCount = channel.getInt("count");
                }
            }
            totalCount = pcCount + webchatCount + appCount + emailCount;

            int pcProgress = pcCount * 100 / totalCount;
            int webchatProgress = webchatCount * 100 / totalCount;
            int appProgress = appCount * 100 / totalCount;
            int emailProgress = emailCount * 100 / totalCount;

            workbanch_webchat_tv_count.setText(webchatCount+"条/占比"+webchatProgress+"%");
            workbanch_pc_tv_count.setText(pcCount+"条/占比"+pcProgress+"%");
            workbanch_app_tv_count.setText(appCount+"条/占比"+appProgress+"%");
            workbanch_email_tv_count.setText(emailCount+"条/占比"+emailProgress+"%");

            workbanch_webchat_pb.setProgress(webchatProgress);
            workbanch_pc_pb.setProgress(pcProgress);
            workbanch_app_pb.setProgress(appProgress);
            workbanch_email_pb.setProgress(emailProgress);

            //客户
            workbanch_customer_ll_line1.removeAllViews();
            workbanch_customer_ll_line2.removeAllViews();
            JSONArray custArray = jsonObject.getJSONArray("custStatusList");
            if(custArray.length() <= 5) {
                for (int i=0; i<custArray.length(); i++) {
                    JSONObject cust = custArray.getJSONObject(i);
                    LinearLayout cust_item = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.workbanch_item_cust, null);
                    TextView workbanch_item_cust_tv_count = (TextView) cust_item.findViewById(R.id.workbanch_item_cust_tv_count);
                    TextView workbanch_item_cust_tv_name = (TextView) cust_item.findViewById(R.id.workbanch_item_cust_tv_name);
                    workbanch_item_cust_tv_name.setText(cust.getString("name"));
                    workbanch_item_cust_tv_count.setText(cust.getInt("count")+"");
                    workbanch_customer_ll_line1.addView(cust_item);
                }
            }else if(custArray.length() > 5) {
                for (int i=0; i<custArray.length(); i++) {
                    if(i <= 4) {
                        JSONObject cust = custArray.getJSONObject(i);
                        LinearLayout cust_item = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.workbanch_item_cust, null);
                        TextView workbanch_item_cust_tv_count = (TextView) cust_item.findViewById(R.id.workbanch_item_cust_tv_count);
                        TextView workbanch_item_cust_tv_name = (TextView) cust_item.findViewById(R.id.workbanch_item_cust_tv_name);
                        workbanch_item_cust_tv_name.setText(cust.getString("name"));
                        workbanch_item_cust_tv_count.setText(cust.getInt("count")+"");
                        workbanch_customer_ll_line1.addView(cust_item);
                    }else {
                        JSONObject cust = custArray.getJSONObject(i);
                        LinearLayout cust_item = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.workbanch_item_cust, null);
                        TextView workbanch_item_cust_tv_count = (TextView) cust_item.findViewById(R.id.workbanch_item_cust_tv_count);
                        TextView workbanch_item_cust_tv_name = (TextView) cust_item.findViewById(R.id.workbanch_item_cust_tv_name);
                        workbanch_item_cust_tv_name.setText(cust.getString("name"));
                        workbanch_item_cust_tv_count.setText(cust.getInt("count")+"");
                        workbanch_customer_ll_line2.addView(cust_item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        workbanch_layout.setVisibility(View.VISIBLE);
        loadingDialog.dismiss();
    }

    private void initViews(View view) {
        workbanch_layout = (LinearLayout) view.findViewById(R.id.workbanch_layout);

        rpb_callin = (RoundProgressBar) view.findViewById(R.id.rpb_callin);
        rpb_callout = (RoundProgressBar) view.findViewById(R.id.rpb_callout);
        workbanch_ll_plan = (LinearLayout) view.findViewById(R.id.workbanch_ll_plan);
        workbanch_customer_ll_line1 = (LinearLayout) view.findViewById(R.id.workbanch_customer_ll_line1);
        workbanch_customer_ll_line2 = (LinearLayout) view.findViewById(R.id.workbanch_customer_ll_line2);
        workbanch_callin_tv_timelength = (TextView) view.findViewById(R.id.workbanch_callin_tv_timelength);
        workbanch_callin_tv_link = (TextView) view.findViewById(R.id.workbanch_callin_tv_link);
        workbanch_callin_tv_count = (TextView) view.findViewById(R.id.workbanch_callin_tv_count);
        workbanch_callout_tv_timelength = (TextView) view.findViewById(R.id.workbanch_callout_tv_timelength);
        workbanch_callout_tv_link = (TextView) view.findViewById(R.id.workbanch_callout_tv_link);
        workbanch_callout_tv_count = (TextView) view.findViewById(R.id.workbanch_callout_tv_count);
        workbanch_erp_tv_dcl = (TextView) view.findViewById(R.id.workbanch_erp_tv_dcl);
        workbanch_erp_tv_dlq = (TextView) view.findViewById(R.id.workbanch_erp_tv_dlq);
        workbanch_webchat_tv_count = (TextView) view.findViewById(R.id.workbanch_webchat_tv_count);
        workbanch_pc_tv_count = (TextView) view.findViewById(R.id.workbanch_pc_tv_count);
        workbanch_app_tv_count = (TextView) view.findViewById(R.id.workbanch_app_tv_count);
        workbanch_email_tv_count = (TextView) view.findViewById(R.id.workbanch_email_tv_count);
        workbanch_webchat_pb = (ProgressBar) view.findViewById(R.id.workbanch_webchat_pb);
        workbanch_pc_pb = (ProgressBar) view.findViewById(R.id.workbanch_pc_pb);
        workbanch_app_pb = (ProgressBar) view.findViewById(R.id.workbanch_app_pb);
        workbanch_email_pb = (ProgressBar) view.findViewById(R.id.workbanch_email_pb);

    }
}
