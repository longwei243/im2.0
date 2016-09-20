package com.moor.im.options.mobileassistant.customer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.moor.im.common.utils.CacheUtils;
import com.moor.im.common.utils.ObservableUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.pulltorefresh.PullToRefreshBase;
import com.moor.im.common.views.pulltorefresh.PullToRefreshListView;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.cdr.activity.MACallDetailActivity;
import com.moor.im.options.mobileassistant.cdr.activity.MYCallHighQueryActivity;
import com.moor.im.options.mobileassistant.cdr.adapter.MyCallAdapter;
import com.moor.im.options.mobileassistant.cdr.adapter.SPAdapter;
import com.moor.im.options.mobileassistant.customer.activity.CustomerDetailActivity;
import com.moor.im.options.mobileassistant.customer.activity.CustomerHighQueryActivity;
import com.moor.im.options.mobileassistant.customer.adapter.CustomerSpAdapter;
import com.moor.im.options.mobileassistant.customer.adapter.MyCustomerAdapter;
import com.moor.im.options.mobileassistant.model.MACallLogData;
import com.moor.im.options.mobileassistant.model.MACustomer;
import com.moor.im.options.mobileassistant.model.QueryData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 16/8/23.
 */
public class MyCustomerFragment extends BaseLazyFragment{

    private static final String MYCUSTOMERQUERYTYPE = "myCustomerQueryType";

    private List<MACustomer> customerList;
    private PullToRefreshListView mPullRefreshListView;
    private MyCustomerAdapter mAdapter;

    private User user = UserDao.getInstance().getUser();

    private LoadingDialog loadingFragmentDialog;

    private int page = 2;

    private View view;
    private TextView mycustomer_tv_hignquery;
    private EditText mycustomer_et_numquery;
    private ImageButton mycustomer_ib_search;
    private Spinner mycustomer_sp_quickquery;

    private View footerView;

    private SharedPreferences myCustomerSp;
    private SharedPreferences.Editor myCustomerEditor;

    private TextView mycustomer_tv_queryitem;
    private ImageView mycustomer_btn_queryitem;
    private RelativeLayout mycustomer_rl_queryitem;

    private String dbType = "";

    private JSONObject custCache;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_customer, null);
        myCustomerSp = getActivity().getSharedPreferences(getResources().getString(R.string.mobileAssistant), 0);
        myCustomerEditor = myCustomerSp.edit();
        myCustomerEditor.clear();
        myCustomerEditor.commit();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.footer, null);
        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.mycustomer_ptl);

        mycustomer_tv_hignquery = (TextView) view.findViewById(R.id.mycustomer_tv_hignquery);
        mycustomer_tv_hignquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CustomerHighQueryActivity.class);
                intent.putExtra("menu", "customer_my");
                intent.putExtra("dbType", dbType);
                startActivityForResult(intent, 0x999);
            }
        });


        mycustomer_et_numquery = (EditText) view.findViewById(R.id.mycustomer_et_numquery);
        mycustomer_ib_search = (ImageButton) view.findViewById(R.id.mycustomer_ib_search);
        mycustomer_ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = mycustomer_et_numquery.getText().toString().trim();
                if (!"".equals(num)) {
                    try{
                        JSONObject map = new JSONObject();
                        map.put("menu", "customer_my");
                        map.put("dbType", dbType);
                        map.put("page", 1);
                        map.put("limit", 10);
                        map.put("combox", num);
                        MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCustomerueryData, map);
                        queryCustomerListData(map);
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        loadingFragmentDialog = new LoadingDialog();

        mycustomer_sp_quickquery = (Spinner) view.findViewById(R.id.mycustomer_sp_quickquery);

        mycustomer_rl_queryitem = (RelativeLayout) view.findViewById(R.id.mycustomer_rl_queryitem);
        mycustomer_tv_queryitem = (TextView) view.findViewById(R.id.mycustomer_tv_queryitem);
        mycustomer_btn_queryitem = (ImageView) view.findViewById(R.id.mycustomer_btn_queryitem);
        mycustomer_btn_queryitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        initData();
    }

    private void initSpinner(JSONObject status) {


        List<QueryData> datas = new ArrayList<>();
        QueryData allQD = new QueryData();
        allQD.setValue("");
        allQD.setName("全部");
        datas.add(allQD);

        Iterator<String> iterator = status.keys();
        while (iterator.hasNext()) {
            try {
                QueryData queryData = new QueryData();
                String key = iterator.next();
                queryData.setName(status.getString(key));
                queryData.setValue(key);
                datas.add(queryData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        CustomerSpAdapter spAdapter = new CustomerSpAdapter(getActivity(), datas);
        mycustomer_sp_quickquery.setAdapter(spAdapter);
        mycustomer_sp_quickquery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                QueryData queryData = (QueryData) parent.getAdapter().getItem(position);
                String value = queryData.getValue();

                try{
                    JSONObject map = new JSONObject();
                    map.put("menu", "customer_my");
                    map.put("dbType", dbType);
                    map.put("page", 1);
                    map.put("limit", 10);
                    if(!"".equals(value)) {
                        map.put("status", value);
                    }
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCustomerueryData, map);
                    queryCustomerListData(map);
                }catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initData() {
        loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");

        mCompositeSubscription.add(ObservableUtils.getErpCache(user._id)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return HttpManager.getInstance().getCustomerCache(user._id);
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

                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("客户缓存获取成功了");
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if(jsonObject.getBoolean("success")) {
                                JSONArray ja = jsonObject.getJSONArray("data");
                                if(ja.length() == 0) {

                                }else {
                                    custCache = ja.getJSONObject(0);
                                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MACust, custCache.toString());
                                    dbType = custCache.getString("_id");
                                    JSONObject status = custCache.getJSONObject("status");
                                    initSpinner(status);
//                                    queryCustomerListData(dbType, "");
                                }
                            }else {

                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("解析客户缓存报错了");
                        }
                    }
                }));


    }


    private void queryCustomerListData(JSONObject map) {
        loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
        HttpManager.getInstance().queryCustomerList(user._id, map, new GetCustomerListListener());
    }

    class GetCustomerListListener implements ResponseListener {

        @Override
        public void onFailed() {
            System.out.println("cccccc返回失败");
        }

        @Override
        public void onSuccess(String responseStr) {
            System.out.println("cccccc返回结果:"+responseStr);
            loadingFragmentDialog.dismiss();

            if(HttpParser.getSucceed(responseStr)) {

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

                customerList = HttpParser.getCustomers(responseStr);
                mAdapter = new MyCustomerAdapter(getActivity(), customerList, custCache.toString());
                mPullRefreshListView.setAdapter(mAdapter);

                if(customerList.size() < 10) {
                    mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                    mPullRefreshListView.getRefreshableView().addFooterView(footerView);
                }

                page = 2;
                mPullRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MACustomer customer = (MACustomer) parent.getAdapter().getItem(position);
                        if (customer != null) {

                            Intent intent = new Intent(getActivity(), CustomerDetailActivity.class);
                            intent.putExtra("customerId", customer._id);
                            startActivity(intent);
                        }
                    }
                });
            }

        }
    }

    private void loadDatasMore(){

        try{
            JSONObject map =   MobileApplication.cacheUtil.getAsJSONObject(CacheKey.CACHE_MyCustomerueryData);
            map.put("page", page);
            HttpManager.getInstance().queryCustomerList(user._id, map, new ResponseListener() {
                @Override
                public void onFailed() {
                    System.out.println("返回失败");
                }

                @Override
                public void onSuccess(String responseStr) {
                    System.out.println("客户加载更多返回数据:"+responseStr);
                    if(HttpParser.getSucceed(responseStr)) {
                        List<MACustomer> c = HttpParser.getCustomers(responseStr);
                        if(c.size() < 10) {
                            //是最后一页了
                            customerList.addAll(c);
                            mAdapter.notifyDataSetChanged();
                            mPullRefreshListView.onRefreshComplete();

                            mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
                            mPullRefreshListView.getRefreshableView().addFooterView(footerView);
                        }else {
                            customerList.addAll(c);
                            mAdapter.notifyDataSetChanged();
                            mPullRefreshListView.onRefreshComplete();
                            page++;
                        }
                    }
                }
            });
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x999 && resultCode == Activity.RESULT_OK) {
            if(data.getSerializableExtra("highQueryData") != null) {
                loadingFragmentDialog.show(getActivity().getSupportFragmentManager(), "");
                mycustomer_sp_quickquery.setSelection(0);
                String str = data.getStringExtra("highQueryData");
                try {
                    JSONObject datas = new JSONObject(str);
                    HttpManager.getInstance().queryCustomerList(user._id, datas, new GetCustomerListListener());
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MyCustomerueryData, datas);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }
    }

}
