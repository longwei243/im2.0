package com.moor.im.options.mobileassistant.customer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import com.moor.im.options.mobileassistant.model.MABusinessFlow;
import com.moor.im.options.mobileassistant.model.MABusinessStep;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by longwei on 16/8/23.
 */
public class CustomerHistoryFragment extends BaseLazyFragment{

    private RecyclerView customer_history_rv;
    private User user = UserDao.getInstance().getUser();
    private int page = 1;
    private List<CustomerHistory> mCustomerHistoryList = new ArrayList<>();

    private RelativeLayout customer_history_rl_empty;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_history, null);
        customer_history_rv = (RecyclerView) view.findViewById(R.id.customer_history_rv);
        customer_history_rl_empty = (RelativeLayout) view.findViewById(R.id.customer_history_rl_empty);
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
        final String customerId = ((CustomerDetailActivity)getActivity()).getCustomerId();
        HttpManager.getInstance().customer_queryCommonHistory(user._id, customerId, 1, new ResponseListener() {
            @Override
            public void onFailed() {

            }

            @Override
            public void onSuccess(String responseStr) {
                System.out.println("客户联系历史返回数据:"+responseStr);
                if(HttpParser.getSucceed(responseStr)) {
                    final List<CustomerHistoryData> datas = HttpParser.getCustomerHistoryData(responseStr);
                    if(datas.size() == 0) {
                        customer_history_rl_empty.setVisibility(View.VISIBLE);
                        customer_history_rv.setVisibility(View.GONE);
                    }else {
                        customer_history_rl_empty.setVisibility(View.GONE);
                        customer_history_rv.setVisibility(View.VISIBLE);
                    }

                    mCustomerHistoryList = processCustomerHistoryData(datas);

                    final CustomerHistoryAdapter adapter = new CustomerHistoryAdapter(getActivity(), mCustomerHistoryList, customer_history_rv);
                    customer_history_rv.setAdapter(adapter);

                    if(mCustomerHistoryList.size() == 20) {
                        adapter.setOnMoreDataLoadListener(new CustomerHistoryAdapter.LoadMoreDataListener() {
                            @Override
                            public void loadMoreData() {
                                //加入null值此时adapter会判断item的type
                                mCustomerHistoryList.add(null);
                                adapter.notifyDataSetChanged();

                                page++;

                                HttpManager.getInstance().customer_queryCommonHistory(user._id, customerId, page, new ResponseListener() {
                                    @Override
                                    public void onFailed() {
                                        mCustomerHistoryList.remove(mCustomerHistoryList.size() - 1);
                                        adapter.notifyDataSetChanged();
                                        adapter.setLoaded();
                                    }

                                    @Override
                                    public void onSuccess(String responseStr) {

                                        if(HttpParser.getSucceed(responseStr)) {
                                            final List<CustomerHistoryData> datas = HttpParser.getCustomerHistoryData(responseStr);
                                            if(datas.size() != 0) {
                                                List<CustomerHistory> ch_more = processCustomerHistoryData(datas);
                                                mCustomerHistoryList.remove(mCustomerHistoryList.size() - 1);
                                                adapter.notifyDataSetChanged();
                                                mCustomerHistoryList.addAll(ch_more);
                                                adapter.notifyDataSetChanged();
                                                adapter.setLoaded();

                                                if (datas.size() < 20) {
                                                    adapter.setOnMoreDataLoadListener(null);
                                                    page = 1;
                                                }
                                            }else {
                                                adapter.setOnMoreDataLoadListener(null);
                                                page = 1;
                                                mCustomerHistoryList.remove(mCustomerHistoryList.size() - 1);
                                                adapter.notifyDataSetChanged();
                                                adapter.setLoaded();
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }

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

            String agentName = "";
            MAAgent agent = MobileAssitantCache.getInstance().getAgentById(chd.agent);
            if(agent != null) {
                agentName = agent.displayName;
            }

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

            String action = "";

            String statusStr = chd.status;
            if(customerHistory.typeCode == CustomerHistoryData.TYPE_NOTE) {
                if("finish".equals(chd.status)) {
                    statusStr = "计划完成";
                    action = agentName + "完成了联系计划";
                } else if("create".equals(chd.status)) {
                    statusStr = "进行中";
                    action = agentName + "制定了联系计划";
                }
            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_BUSSINESS) {
                MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(chd.status);
                if (step != null) {
                    statusStr = step.name;
                }
                MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(chd.businessType);
                if(flow != null) {
                    action = agentName + "处理了"+flow.name+"的工单";
                }else {
                    action = agentName + "处理了工单";
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

                if(chd.recordFile != null && !"".equals(chd.recordFile)) {
                    customerHistory.recordFile = chd.recordFile;
                }
                if(customerHistory.typeCode == CustomerHistoryData.TYPE_CALL_IN) {
                    action = agentName + "来电呼入";
                }else {
                    action = agentName + "外呼去电";
                }
            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_CHAT) {

                statusStr = chd.dispose;
                action = agentName + "在线咨询";

            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_APPROVAL) {
                if("unPass".equals(chd.status)) {
                    statusStr = "审核不通过";
                }else {
                    statusStr = "审核通过";
                }
                action = agentName + "审批";
            }else if(customerHistory.typeCode == CustomerHistoryData.TYPE_EMAIL) {
                statusStr = chd.dispose;
                action = agentName + "处理邮件";
            }
            customerHistory.status = statusStr;
            customerHistory.action = action;

            ch.add(customerHistory);
        }
        return ch;
    }


}
