package com.moor.im.options.mobileassistant.erp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.CacheUtils;
import com.moor.im.common.views.pulltorefresh.PullToRefreshBase;
import com.moor.im.common.views.pulltorefresh.PullToRefreshListView;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.MobileAssitantParser;
import com.moor.im.options.mobileassistant.erp.activity.ErpDetailActivity;
import com.moor.im.options.mobileassistant.erp.activity.ErpHighQueryActivity;
import com.moor.im.options.mobileassistant.erp.adapter.UserUnDealOrderAdapter;
import com.moor.im.options.mobileassistant.erp.event.ErpExcuteSuccess;
import com.moor.im.options.mobileassistant.erp.event.HaveOrderEvent;
import com.moor.im.options.mobileassistant.erp.event.NewOrderEvent;
import com.moor.im.options.mobileassistant.model.MABusiness;
import com.moor.im.options.mobileassistant.model.MABusinessFlow;
import com.moor.im.options.mobileassistant.model.MABusinessStep;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by longwei on 2016/5/4.
 */
public class DBFragment extends BaseLazyFragment{

    private static final String USERUNDEALQUERYTYPE = "userUnDealQueryType";

    private List<MABusiness> maBusinesses;
    private PullToRefreshListView mPullRefreshListView;
    private UserUnDealOrderAdapter mAdapter;

    private User user = UserDao.getInstance().getUser();

    private LoadingDialog loadingFragmentDialog;

    private int page = 2;

    private View view;
    private TextView userundeal_tv_hignquery;
    private EditText userundeal_et_numquery;
    private ImageButton userundeal_ib_search;
    private Spinner userundeal_sp_quickquery;

    private View footerView;

    private SharedPreferences myCallSp;
    private SharedPreferences.Editor myCallEditor;

    private TextView userundeal_tv_queryitem;
    private ImageView userundeal_btn_queryitem;
    private RelativeLayout userundeal_rl_queryitem;
    private RelativeLayout userundeal_rl_neworder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fargment_ma_userundeal, null);
        myCallSp = getActivity().getSharedPreferences(getResources().getString(R.string.mobileAssistant), 0);
        myCallEditor = myCallSp.edit();
        myCallEditor.clear();
        myCallEditor.commit();
        initViews(view);
        addRxBusLinester();
        return view;
    }
    private void addRxBusLinester() {
        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof ErpExcuteSuccess) {
                            refreshData();
                        }else if (event instanceof NewOrderEvent) {
                            //有新的工单
                            userundeal_rl_neworder.setVisibility(View.VISIBLE);
                            userundeal_rl_neworder.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    userundeal_rl_neworder.setVisibility(View.GONE);
                                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                                    refreshData();
                                }
                            });
                        }else if (event instanceof HaveOrderEvent) {
                            //领取了工单
                            if(((HaveOrderEvent) event).type == 1) {
                                refreshData();
                            }
                        }
                    }
                }));
    }

    private void refreshData() {
        userundeal_sp_quickquery.setSelection(0);
        myCallEditor.clear();
        myCallEditor.commit();
        HashMap<String, String> datas = new HashMap<>();
        HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
        userundeal_rl_queryitem.setVisibility(View.GONE);
    }

    private void initViews(View view) {
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.footer, null);
        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.userundeal_ptl);

        userundeal_tv_hignquery = (TextView) view.findViewById(R.id.userundeal_tv_hignquery);
        userundeal_tv_hignquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ErpHighQueryActivity.class);
                startActivityForResult(intent, 0x666);
            }
        });
        userundeal_rl_neworder = (RelativeLayout) view.findViewById(R.id.userundeal_rl_neworder);

        userundeal_et_numquery = (EditText) view.findViewById(R.id.userundeal_et_numquery);
        userundeal_ib_search = (ImageButton) view.findViewById(R.id.userundeal_ib_search);
        userundeal_ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = userundeal_et_numquery.getText().toString().trim();
                if (!"".equals(num)) {
                    HashMap<String, String> datas = new HashMap<String, String>();
                    datas.put("query", num);
                    HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
                    myCallEditor.putString(USERUNDEALQUERYTYPE, "number");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_DclQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    userundeal_rl_queryitem.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), "请输入客户名称后查询", Toast.LENGTH_SHORT).show();
                }

            }
        });

        userundeal_sp_quickquery = (Spinner) view.findViewById(R.id.userundeal_sp_quickquery);
        final String[] quickDatas = getResources().getStringArray(R.array.userundeal);
        ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, R.id.sp_tv, quickDatas) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_layout,
                        null);
                TextView label = (TextView) view
                        .findViewById(R.id.spinner_item_label);
                label.setText(quickDatas[position]);
                if (userundeal_sp_quickquery.getSelectedItemPosition() == position) {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.maincolor));
                } else {
                    view.setBackgroundColor(getResources().getColor(
                            R.color.maincolordark));
                }
                return view;
            }
        };
        userundeal_sp_quickquery.setAdapter(spAdapter);
        userundeal_sp_quickquery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(MobileAssitantCache.getInstance().getFlowMap().size() != 0) {
                    if (position == 1) {
                        myCallEditor.clear();
                        myCallEditor.commit();
                        HashMap<String, String> datas = new HashMap<>();
                        HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
                        loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                        userundeal_rl_queryitem.setVisibility(View.GONE);
                    } else if (position == 2) {
                        HashMap<String, String> datas = new HashMap<>();
                        HttpManager.getInstance().queryFollowedOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
                        myCallEditor.putString(USERUNDEALQUERYTYPE, "quick");
                        myCallEditor.putString("type", "follow");
                        myCallEditor.commit();
                        loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                        userundeal_rl_queryitem.setVisibility(View.GONE);
                    }else if (position == 3) {
                        HashMap<String, String> datas = new HashMap<>();
                        HttpManager.getInstance().queryAssignedOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
                        myCallEditor.putString(USERUNDEALQUERYTYPE, "quick");
                        myCallEditor.putString("type", "assign");
                        myCallEditor.commit();
                        loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                        userundeal_rl_queryitem.setVisibility(View.GONE);
                    }
                }else {
                    Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadingFragmentDialog = new LoadingDialog();
        userundeal_rl_queryitem = (RelativeLayout) view.findViewById(R.id.userundeal_rl_queryitem);
        userundeal_tv_queryitem = (TextView) view.findViewById(R.id.userundeal_tv_queryitem);
        userundeal_btn_queryitem = (ImageView) view.findViewById(R.id.userundeal_btn_queryitem);
        userundeal_btn_queryitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userundeal_rl_queryitem.setVisibility(View.GONE);
                loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                myCallEditor.clear();
                myCallEditor.commit();

                if(MobileAssitantCache.getInstance().getFlowMap().size() != 0) {
                    HashMap<String, String> datas = new HashMap<>();
                    HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
                }else {
                    Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                }

            }
        });

        if(MobileAssitantCache.getInstance().getFlowMap().size() != 0) {
            HashMap<String, String> datas = new HashMap<>();
            HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
            loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
        }else {
            Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
        }


    }

    class QueryUserUnDealOrderResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
            loadingFragmentDialog.dismiss();
            mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            Toast.makeText(getActivity(), "网络异常，数据加载失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String responseString) {
            try {
                JSONObject o = new JSONObject(responseString);
                if(o.getBoolean("Succeed")) {
                    BackTask backTask = new BackTask();
                    backTask.execute(responseString);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    class BackTask extends AsyncTask<String, Void, List<MABusiness>> {

        @Override
        protected List<MABusiness> doInBackground(String[] params) {
            maBusinesses = MobileAssitantParser.getBusiness(params[0]);
            return maBusinesses;
        }

        @Override
        protected void onPostExecute(List<MABusiness> businessList) {
            super.onPostExecute(businessList);

            mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
            mPullRefreshListView.getRefreshableView().removeFooterView(footerView);
            mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {

                @Override
                public void onPullDownToRefresh(PullToRefreshBase refreshView) {
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                    loadDatasMore();
                }
            });
            mAdapter = new UserUnDealOrderAdapter(getActivity(), businessList, user._id);
            mPullRefreshListView.setAdapter(mAdapter);

            if(businessList.size() < 10) {
                mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mPullRefreshListView.getRefreshableView().addFooterView(footerView);
            }

            page = 2;

            loadingFragmentDialog.dismiss();
            mPullRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MABusiness business = (MABusiness) parent.getAdapter().getItem(position);
                    if (business != null) {
                        Intent intent = new Intent(getActivity(), ErpDetailActivity.class);
                        intent.putExtra("busId", business._id);
                        intent.putExtra("customerName", business.name);
                        intent.putExtra("customerId", business.customer);
                        intent.putExtra("type", "userundeal");
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private void loadDatasMore() {

        String type = myCallSp.getString(USERUNDEALQUERYTYPE, "");
        if("".equals(type)) {
            HashMap<String, String> datas = new HashMap<>();
            datas.put("page", page + "");
            HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new GetUserUnDealOrderMoreResponseHandler());
        }else if("number".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_DclQueryData);
            datas.put("page", page + "");
            HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new GetUserUnDealOrderMoreResponseHandler());
        }else if("high".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_DclQueryData);
            datas.put("page", page + "");
            HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new GetUserUnDealOrderMoreResponseHandler());
        }else if("quick".equals(type)) {
            String s = myCallSp.getString("type", "");
            if("follow".equals(s)) {
                HashMap<String, String> datas = new HashMap<>();
                datas.put("page", page + "");
                HttpManager.getInstance().queryFollowedOrder(user._id, datas, new GetUserUnDealOrderMoreResponseHandler());
            }else if("assign".equals(s)) {
                HashMap<String, String> datas = new HashMap<>();
                datas.put("page", page + "");
                HttpManager.getInstance().queryAssignedOrder(user._id, datas, new GetUserUnDealOrderMoreResponseHandler());
            }

        }


    }
    class GetUserUnDealOrderMoreResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
            mPullRefreshListView.onRefreshComplete();
            Toast.makeText(getActivity(), "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String responseString) {
            if (HttpParser.getSucceed(responseString)) {

                BackTaskMore backTask = new BackTaskMore();
                backTask.execute(responseString);
            } else {
//				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//						.show();
            }
        }
    }

    class BackTaskMore extends AsyncTask<String, Void, List<MABusiness>> {

        @Override
        protected List<MABusiness> doInBackground(String[] params) {
            List<MABusiness> businesses = MobileAssitantParser.getBusiness(params[0]);
            return businesses;
        }

        @Override
        protected void onPostExecute(List<MABusiness> businesses) {
            super.onPostExecute(businesses);
            if(businesses.size() < 10) {
                //是最后一页了
                maBusinesses.addAll(businesses);
                mAdapter.notifyDataSetChanged();
                mPullRefreshListView.onRefreshComplete();

                mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                mPullRefreshListView.getRefreshableView().addFooterView(footerView);
            }else {
                maBusinesses.addAll(businesses);
                mAdapter.notifyDataSetChanged();
                mPullRefreshListView.onRefreshComplete();
                page++;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x666 && resultCode == Activity.RESULT_OK) {
            if(data.getSerializableExtra("highQueryData") != null && MobileAssitantCache.getInstance().getFlowMap().size() != 0) {
                loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                userundeal_sp_quickquery.setSelection(0);
                HashMap<String, String> datas = (HashMap<String, String>) data.getSerializableExtra("highQueryData");
                //显示查询的条件
                showQueryItem(datas);
                HttpManager.getInstance().queryUserUnDealOrder(user._id, datas, new QueryUserUnDealOrderResponseHandler());
                myCallEditor.putString(USERUNDEALQUERYTYPE, "high");
                myCallEditor.commit();
                MobileApplication.cacheUtil.put(CacheKey.CACHE_DclQueryData, datas, CacheUtils.TIME_HOUR * 2);
            }else {
                Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showQueryItem(HashMap<String, String> datas) {
        StringBuilder sb = new StringBuilder();
        sb.append("查询条件:");
        for(String key : datas.keySet()) {
            sb.append(" ");

            if("flow".equals(key)) {
                MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(datas.get(key));
                String name = flow.name;
                sb.append(name);
                continue;
            }

            if("step".equals(key)) {
                MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(datas.get(key));
                String stepName = step.name;
                sb.append(stepName);
                continue;
            }

            if("createTime".equals(key)) {
                String status = "";
                if("today".equals(datas.get(key))) {
                    status = "今天";
                }else if("threeDay".equals(datas.get(key))) {
                    status = "近三天";
                }else if("week".equals(datas.get(key))) {
                    status = "近一周";
                }else if("month".equals(datas.get(key))) {
                    status = "近一月";
                }
                sb.append(status);
                continue;
            }

            sb.append(datas.get(key));
        }
        userundeal_rl_queryitem.setVisibility(View.VISIBLE);
        userundeal_tv_queryitem.setText(sb.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
