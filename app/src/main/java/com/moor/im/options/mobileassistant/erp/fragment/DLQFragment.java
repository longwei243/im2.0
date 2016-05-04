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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.event.MsgRead;
import com.moor.im.common.event.NewMsgReceived;
import com.moor.im.common.event.SendMsg;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.CacheUtils;
import com.moor.im.common.utils.ObservableUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.pulltorefresh.PullToRefreshBase;
import com.moor.im.common.views.pulltorefresh.PullToRefreshListView;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.MobileAssitantParser;
import com.moor.im.options.mobileassistant.erp.activity.ErpDetailActivity;
import com.moor.im.options.mobileassistant.erp.activity.ErpHighQueryActivity;
import com.moor.im.options.mobileassistant.erp.adapter.RoalUnDealOrderAdapter;
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

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/5/4.
 */
public class DLQFragment extends BaseLazyFragment{
    private static final String ROALUNDEALQUERYTYPE = "roalUnDealQueryType";

    private List<MABusiness> maBusinesses;
    private PullToRefreshListView mPullRefreshListView;
    private RoalUnDealOrderAdapter mAdapter;

    private User user = UserDao.getInstance().getUser();

    private LoadingDialog loadingFragmentDialog;

    private int page = 2;

    private View view;
    private TextView roalundeal_tv_hignquery;
    private EditText roalundeal_et_numquery;
    private ImageButton roalundeal_ib_search;

    private View footerView;

    private SharedPreferences myCallSp;
    private SharedPreferences.Editor myCallEditor;

    private TextView roalundeal_tv_queryitem;
    private ImageView roalundeal_btn_queryitem;
    private RelativeLayout roalundeal_rl_queryitem;

    private TextView mAllUnreadcount;
    private int unReadCount;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fargment_ma_roalundeal, null);
        myCallSp = getActivity().getSharedPreferences(getResources().getString(R.string.mobileAssistant), 0);
        myCallEditor = myCallSp.edit();
        myCallEditor.clear();
        myCallEditor.commit();
        initViews(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        addRxBusLinester();
    }

    private void addRxBusLinester() {

        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof ErpExcuteSuccess) {
                            HashMap<String, String> datas = new HashMap<>();
                            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());
                        }else if (event instanceof NewOrderEvent) {
                            //有新的工单
                            HashMap<String, String> datas = new HashMap<>();
                            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());
                        }else if (event instanceof HaveOrderEvent) {
                            //领取了工单
                            if(unReadCount-1 > 0) {
                                mAllUnreadcount.setVisibility(View.VISIBLE);
                                mAllUnreadcount.setText(unReadCount-1 + "");
                            }else {
                                mAllUnreadcount.setVisibility(View.GONE);
                            }

                        }
                    }
                }));
    }

    private void initViews(View view) {
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.footer, null);
        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.roalundeal_ptl);

        roalundeal_tv_hignquery = (TextView) view.findViewById(R.id.roalundeal_tv_hignquery);
        roalundeal_tv_hignquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ErpHighQueryActivity.class);
                startActivityForResult(intent, 0x777);
            }
        });


        roalundeal_et_numquery = (EditText) view.findViewById(R.id.roalundeal_et_numquery);
        roalundeal_ib_search = (ImageButton) view.findViewById(R.id.roalundeal_ib_search);
        roalundeal_ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = roalundeal_et_numquery.getText().toString().trim();
                if (!"".equals(num)) {
                    HashMap<String, String> datas = new HashMap<String, String>();
                    datas.put("query", num);
                    HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());
                    myCallEditor.putString(ROALUNDEALQUERYTYPE, "number");
                    myCallEditor.commit();
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_DlqQueryData, datas, CacheUtils.TIME_HOUR * 2);
                    loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                    roalundeal_rl_queryitem.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), "请输入客户名称后查询", Toast.LENGTH_SHORT).show();
                }

            }
        });
        loadingFragmentDialog = new LoadingDialog();
        roalundeal_rl_queryitem = (RelativeLayout) view.findViewById(R.id.roalundeal_rl_queryitem);
        roalundeal_tv_queryitem = (TextView) view.findViewById(R.id.roalundeal_tv_queryitem);
        roalundeal_btn_queryitem = (ImageView) view.findViewById(R.id.roalundeal_btn_queryitem);
        roalundeal_btn_queryitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roalundeal_rl_queryitem.setVisibility(View.GONE);
                loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                myCallEditor.clear();
                myCallEditor.commit();

                HashMap<String, String> datas = new HashMap<>();
                HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());
            }
        });
        //数量
        mAllUnreadcount = (TextView) getActivity().findViewById(R.id.all_unreadcount);

        initCache();
    }

    private void initCache() {
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent) == null || MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAQueue) == null || MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) == null) {
            loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
            mCompositeSubscription.add(ObservableUtils.getAgentCacheObservable(user._id)
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            return ObservableUtils.getQueueCacheObservable(user._id);
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            return ObservableUtils.getOptionCacheObservable(user._id);
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            return ObservableUtils.getBusinessFlowCacheObservable(user._id);
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            return ObservableUtils.getBusinessStepCacheObservable(user._id);
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            return ObservableUtils.getBusinessFieldCacheObservable(user._id);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            loadingFragmentDialog.dismiss();
                            Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(String s) {
                            HashMap<String, String> datas = new HashMap<>();
                            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());

                        }
                    }));


        }else if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow) == null || MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep) == null || MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessField) == null) {
            loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
            mCompositeSubscription.add(ObservableUtils.getBusinessFlowCacheObservable(user._id)
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            return ObservableUtils.getBusinessStepCacheObservable(user._id);
                        }
                    })
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String s) {
                            return ObservableUtils.getBusinessFieldCacheObservable(user._id);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            loadingFragmentDialog.dismiss();
                            Toast.makeText(getActivity(), "获取数据失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(String s) {
                            HashMap<String, String> datas = new HashMap<>();
                            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());

                        }
                    }));

        }else {
            HashMap<String, String> datas = new HashMap<>();
            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());
            loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
        }


    }

    class QueryRoleUnDealOrderResponseHandler implements ResponseListener {
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
            mAdapter = new RoalUnDealOrderAdapter(DLQFragment.this, businessList, user._id);
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
                        intent.putExtra("type", "roalundeal");
                        startActivity(intent);
                    }
                }
            });

            if(businessList.size() > 0) {
                mAllUnreadcount.setVisibility(View.VISIBLE);
                unReadCount = businessList.size();
                mAllUnreadcount.setText(unReadCount+"");
            }else {
                mAllUnreadcount.setVisibility(View.GONE);
            }
        }
    }

    private void loadDatasMore() {

        String type = myCallSp.getString(ROALUNDEALQUERYTYPE, "");
        if("".equals(type)) {
            HashMap<String, String> datas = new HashMap<>();
            datas.put("page", page + "");
            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new GetRoalUnDealOrderMoreResponseHandler());
        }else if("number".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_DlqQueryData);
            datas.put("page", page + "");
            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new GetRoalUnDealOrderMoreResponseHandler());
        }else if("high".equals(type)) {
            HashMap<String, String> datas = (HashMap<String, String>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_DlqQueryData);
            datas.put("page", page + "");
            HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new GetRoalUnDealOrderMoreResponseHandler());
        }


    }
    class GetRoalUnDealOrderMoreResponseHandler implements ResponseListener {
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

            if(maBusinesses.size() > 0) {
                mAllUnreadcount.setVisibility(View.VISIBLE);
                unReadCount = maBusinesses.size();
                mAllUnreadcount.setText(unReadCount + "");
            }else {
                mAllUnreadcount.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x777 && resultCode == Activity.RESULT_OK) {
            if(data.getSerializableExtra("highQueryData") != null) {
                loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                HashMap<String, String> datas = (HashMap<String, String>) data.getSerializableExtra("highQueryData");
                //显示查询的条件
                showQueryItem(datas);
                HttpManager.getInstance().queryRoleUnDealOrder(user._id, datas, new QueryRoleUnDealOrderResponseHandler());
                myCallEditor.putString(ROALUNDEALQUERYTYPE, "high");
                myCallEditor.commit();
                MobileApplication.cacheUtil.put(CacheKey.CACHE_DlqQueryData, datas, CacheUtils.TIME_HOUR * 2);
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
        roalundeal_rl_queryitem.setVisibility(View.VISIBLE);
        roalundeal_tv_queryitem.setText(sb.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
