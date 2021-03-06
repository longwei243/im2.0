package com.moor.im.options.mobileassistant.report.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.MobileAssitantParser;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.report.model.WorkLoadData;
import com.moor.imkf.gson.Gson;
import com.moor.imkf.gson.reflect.TypeToken;
import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.report.adapter.DividerItemDecoration;
import com.moor.im.options.mobileassistant.report.adapter.ItemDragHelperCallback;
import com.moor.im.options.mobileassistant.report.adapter.ReportAdapter;
import com.moor.im.options.mobileassistant.report.model.CallInData;
import com.moor.im.options.mobileassistant.report.model.CallOutData;
import com.moor.im.options.mobileassistant.report.model.CustData;
import com.moor.im.options.mobileassistant.report.model.IMData;
import com.moor.im.options.mobileassistant.report.model.QueueData;
import com.moor.im.options.mobileassistant.report.model.ReportData;
import com.moor.im.options.mobileassistant.report.model.SessionData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/6/13.
 */
public class ReportFragment extends BaseLazyFragment{

    private RecyclerView report_rv;
    private LoadingDialog loadingDialog;

    private JSONArray userLimitArray;
    private boolean showCallin, showCallout, showQueue, showAgent, showIm, showSession=true, showCust;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, null);

        report_rv = (RecyclerView) view.findViewById(R.id.report_rv);
        loadingDialog = new LoadingDialog();
        loadingDialog.show(getFragmentManager(), "");

        userLimitArray = MobileApplication.cacheUtil.getAsJSONArray("userLimit");
        if(userLimitArray != null) {
            try {
                for (int i=0; i<userLimitArray.length(); i++) {

                    if("callin_report".equals(userLimitArray.getString(i))) {
                        showCallin = true;
                    }
                    if("callout_report".equals(userLimitArray.getString(i))) {
                        showCallout = true;
                    }
                    if("queue_report".equals(userLimitArray.getString(i))) {
                        showQueue = true;
                    }
                    if("call_report_agent".equals(userLimitArray.getString(i))) {
                        showAgent = true;
                    }
                    if("im_report_msg".equals(userLimitArray.getString(i))) {
                        showIm = true;
                    }
                    if("customer_report_increase".equals(userLimitArray.getString(i))) {
                        showCust = true;
                    }
                    if("im_report_session_time".equals(userLimitArray.getString(i))) {
                        showSession = true;
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        HttpManager.getInstance().doReport(UserDao.getInstance().getUser()._id, "queryall", "day")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingDialog.dismiss();
                        LogUtil.d("获取报表数据失败");
                    }

                    @Override
                    public void onNext(String s) {
//                        LogUtil.d("报表返回数据:"+s);
                        loadingDialog.dismiss();
                        processReportData(s);
                    }
                });
        return view;
    }

    private void processReportData(String s) {
        List<ReportData> reportDataList = new ArrayList<>();
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.getBoolean("Succeed")) {


                if(showAgent) {
                    //座席工作量
                    JSONObject agentwork = jsonObject.getJSONObject("agentwork");
                    if(agentwork.getBoolean("success")) {

                        JSONArray agentArray = agentwork.getJSONObject("data").getJSONArray("agents");
                        List<MAAgent> agents = gson.fromJson(agentArray.toString(),
                                new TypeToken<List<MAAgent>>() {
                                }.getType());
                        MobileAssitantCache.getInstance().setAgentMap(MobileAssitantParser.transformAgentData(agents));

                        JSONArray workloadArray = agentwork.getJSONObject("data").getJSONArray("workload");
                        ReportData agentRd = new ReportData();
                        agentRd.type = ReportData.TYPE_AGENT;
                        agentRd.name = "座席工作量";
                        agentRd.workLoadDatas = gson.fromJson(workloadArray.toString(),
                                new TypeToken<List<WorkLoadData>>() {
                                }.getType());
                        reportDataList.add(agentRd);
                    }
                }


                if(showCallin) {
                    //呼入
                    JSONObject callin = jsonObject.getJSONObject("callin");
                    if(callin.getBoolean("success")) {
                        ReportData callinRd = new ReportData();
                        callinRd.type = ReportData.TYPE_CALL_IN;
                        callinRd.name = "呼入";
                        callinRd.callInDatas = gson.fromJson(callin.getJSONArray("data").toString(),
                                new TypeToken<List<CallInData>>() {
                                }.getType());
                        reportDataList.add(callinRd);
                    }
                }

                if(showCallout) {
                    //呼出
                    JSONObject callout = jsonObject.getJSONObject("callout");
                    if(callout.getBoolean("success")) {
                        ReportData calloutRd = new ReportData();
                        calloutRd.type = ReportData.TYPE_CALL_OUT;
                        calloutRd.name = "呼出";
                        calloutRd.callOutDatas = gson.fromJson(callout.getJSONArray("data").toString(),
                                new TypeToken<List<CallOutData>>() {
                                }.getType());
                        reportDataList.add(calloutRd);
                    }
                }

                if(showQueue) {
                    //技能组
                    JSONObject skillgroup = jsonObject.getJSONObject("skillgroup");
                    if(skillgroup.getBoolean("success")) {
                        ReportData skillgroupRd = new ReportData();
                        skillgroupRd.type = ReportData.TYPE_QUEUE;
                        skillgroupRd.name = "技能组";
                        skillgroupRd.queueDatas = gson.fromJson(skillgroup.getJSONArray("data").toString(),
                                new TypeToken<List<QueueData>>() {
                                }.getType());
                        reportDataList.add(skillgroupRd);
                    }
                }

                if(showIm) {
                    //im
                    JSONObject immessage = jsonObject.getJSONObject("immessage");
                    if(immessage.getBoolean("success")) {
                        ReportData immessageRd = new ReportData();
                        immessageRd.type = ReportData.TYPE_IM;
                        immessageRd.name = "在线客服消息";
                        immessageRd.imDatas = gson.fromJson(immessage.getJSONArray("data").toString(),
                                new TypeToken<List<IMData>>() {
                                }.getType());
                        reportDataList.add(immessageRd);
                    }
                }

                if(showSession) {
                    //session
                    JSONObject imsession = jsonObject.getJSONObject("imsession");
                    if(imsession.getBoolean("success")) {
                        ReportData imsessionRd = new ReportData();
                        imsessionRd.type = ReportData.TYPE_SESSION;
                        imsessionRd.name = "会话数";
                        imsessionRd.sessionDatas = gson.fromJson(imsession.getJSONArray("data").toString(),
                                new TypeToken<List<SessionData>>() {
                                }.getType());
                        reportDataList.add(imsessionRd);
                    }
                }


                if(showCust) {
                    //客户来源
                    JSONObject customerinc = jsonObject.getJSONObject("customerinc");
                    if(customerinc.getBoolean("success")) {
                        ReportData customerincRd = new ReportData();
                        customerincRd.type = ReportData.TYPE_CUSTOMER;
                        customerincRd.name = "客户来源";
                        customerincRd.custDatas = gson.fromJson(customerinc.getJSONArray("data").toString(),
                                new TypeToken<List<CustData>>() {
                                }.getType());
                        reportDataList.add(customerincRd);
                    }
                }

                initData(reportDataList);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initData(List<ReportData> reportDataList) {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        report_rv.setLayoutManager(manager);
        report_rv.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));
        ItemDragHelperCallback callback = new ItemDragHelperCallback(){
            @Override
            public boolean isLongPressDragEnabled() {
                // 长按拖拽打开
                return false;
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(report_rv);

        ReportAdapter adapter = new ReportAdapter(getActivity(), reportDataList);
        report_rv.setAdapter(adapter);
    }

}
