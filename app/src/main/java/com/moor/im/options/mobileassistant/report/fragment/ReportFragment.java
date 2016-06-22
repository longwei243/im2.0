package com.moor.im.options.mobileassistant.report.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.report.adapter.DividerItemDecoration;
import com.moor.im.options.mobileassistant.report.adapter.ItemDragHelperCallback;
import com.moor.im.options.mobileassistant.report.adapter.ReportAdapter;
import com.moor.im.options.mobileassistant.report.model.CallInData;
import com.moor.im.options.mobileassistant.report.model.CallOutData;
import com.moor.im.options.mobileassistant.report.model.IMData;
import com.moor.im.options.mobileassistant.report.model.QueueData;
import com.moor.im.options.mobileassistant.report.model.ReportData;
import com.moor.im.options.mobileassistant.report.model.SessionData;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, null);

        report_rv = (RecyclerView) view.findViewById(R.id.report_rv);
        HttpManager.getInstance().doReport(UserDao.getInstance().getUser()._id, "queryall", "day")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.d("获取报表数据失败");
                    }

                    @Override
                    public void onNext(String s) {
                        LogUtil.d("报表返回数据:"+s);
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
                //呼出
                JSONObject callout = jsonObject.getJSONObject("callout");
                if(callout.getBoolean("success")) {
                    ReportData calloutRd = new ReportData();
                    calloutRd.type = ReportData.TYPE_CALL_OUT;
                    calloutRd.name = "呼出";
                    calloutRd.callOutDatas = gson.fromJson(callin.getJSONArray("data").toString(),
                            new TypeToken<List<CallOutData>>() {
                            }.getType());
                    reportDataList.add(calloutRd);
                }
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

                //session
                JSONObject imsession = jsonObject.getJSONObject("imsession");
                if(imsession.getBoolean("success")) {
                    ReportData imsessionRd = new ReportData();
                    imsessionRd.type = ReportData.TYPE_SESSION;
                    imsessionRd.name = "会话数";
                    imsessionRd.imDatas = gson.fromJson(imsession.getJSONArray("data").toString(),
                            new TypeToken<List<SessionData>>() {
                            }.getType());
                    reportDataList.add(imsessionRd);
                }

                //客户来源
                JSONObject customerinc = jsonObject.getJSONObject("customerinc");
                if(customerinc.getBoolean("success")) {
                    ReportData customerincRd = new ReportData();
                    customerincRd.type = ReportData.TYPE_CUSTOMER;
                    customerincRd.name = "客户来源";
                    customerincRd.imDatas = gson.fromJson(customerinc.getJSONArray("data").toString(),
                            new TypeToken<List<SessionData>>() {
                            }.getType());
                    reportDataList.add(customerincRd);
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
                return true;
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(report_rv);

        ReportAdapter adapter = new ReportAdapter(getActivity(), reportDataList);
        report_rv.setAdapter(adapter);
    }

}
