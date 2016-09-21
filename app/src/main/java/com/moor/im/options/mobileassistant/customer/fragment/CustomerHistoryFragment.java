package com.moor.im.options.mobileassistant.customer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.event.CustomerHistoryRefresh;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.customer.activity.CustomerDetailActivity;
import com.moor.im.options.mobileassistant.customer.adapter.CustomerHistoryAdapter;
import com.moor.im.options.mobileassistant.customer.model.CustomerHistory;
import com.moor.im.options.mobileassistant.customer.model.CustomerHistoryData;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MABusinessStep;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by longwei on 16/8/23.
 */
public class CustomerHistoryFragment extends BaseLazyFragment{

    private RecyclerView customer_history_rv;
    private List<CustomerHistoryData> datas;
    private User user = UserDao.getInstance().getUser();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_history, null);
        customer_history_rv = (RecyclerView) view.findViewById(R.id.customer_history_rv);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        customer_history_rv.setLayoutManager(manager);

        refreshHistoryData();

        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if(o instanceof CustomerHistoryRefresh) {
                            refreshHistoryData();
                        }
                    }
                }));

        return view;
    }

    private void refreshHistoryData() {
        String customerId = ((CustomerDetailActivity)getActivity()).getCustomerId();
        HttpManager.getInstance().customer_queryCommonHistory(user._id, customerId, new ResponseListener() {
            @Override
            public void onFailed() {

            }

            @Override
            public void onSuccess(String responseStr) {
                System.out.println("客户联系历史返回数据:"+responseStr);
                if(HttpParser.getSucceed(responseStr)) {
                    List<CustomerHistoryData> datas = HttpParser.getCustomerHistoryData(responseStr);
                    List<CustomerHistory> ch = processCustomerHistoryData(datas);


                    CustomerHistoryAdapter adapter = new CustomerHistoryAdapter(getActivity(), ch);
                    customer_history_rv.setAdapter(adapter);
                }
            }
        });
    }

    private List<CustomerHistory> processCustomerHistoryData(List<CustomerHistoryData> datas) {
        List<CustomerHistory> ch = new ArrayList<>();
        for(int i=0; i<datas.size(); i++) {
            CustomerHistoryData chd = datas.get(i);

            CustomerHistory customerHistory = new CustomerHistory();
            customerHistory.typeCode = CustomerHistoryData.getTypeCode(chd.type);
            customerHistory.typeName = CustomerHistoryData.getTypeString(chd.type);

            String agentName = "";
            MAAgent agent = MobileAssitantCache.getInstance().getAgentById(chd.agent);
            if(agent != null) {
                agentName = agent.displayName;
            }
            String action = "    "+agentName + customerHistory.typeName;
            customerHistory.action = action;

            try {
                String[] createTime = chd.createTime.split(" ");
                if(createTime.length == 2) {
                    customerHistory.date = createTime[0];
                    customerHistory.time = createTime[1].substring(0, 5);
                }else {
                    customerHistory.date = "00-00";
                    customerHistory.time = "00:00";
                }
            }catch (Exception e) {
                customerHistory.date = "00-00";
                customerHistory.time = "00:00";
            }

            customerHistory.comment = chd.comments;

            String statusStr = chd.status;
            if(customerHistory.typeCode == CustomerHistoryData.TYPE_NOTE) {
                if("finish".equals(chd.status)) {
                    statusStr = "计划完成";
                } else if("create".equals(chd.status)) {
                    statusStr = "进行中";
                }
            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_BUSSINESS) {
                MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(chd.status);
                if (step != null) {
                    statusStr = step.name;
                }
            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_CALL_IN || customerHistory.typeCode == CustomerHistoryData.TYPE_CALL_OUT) {

                if("dealing".equals(chd.status)) {
                    statusStr = "已接听";
                }else if("notDeal".equals(chd.status)) {
                    statusStr = "振铃未接听";
                }else if("queueLeak".equals(chd.status)) {
                    statusStr = "排队放弃";
                }else if("voicemail".equals(chd.status)) {
                    statusStr = "已留言";
                }else if("leak".equals(chd.status)) {
                    statusStr = "IVR放弃";
                }else if("blackList".equals(chd.status)) {
                    statusStr = "黑名单";
                }
            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_CHAT) {

            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_APPROVAL) {
                if("unPass".equals(chd.status)) {
                    statusStr = "审核不通过";
                }else {
                    statusStr = "审核通过";
                }
            }
            customerHistory.status = statusStr;


            ch.add(customerHistory);
        }


        return ch;
    }


}
