package com.moor.im.options.mobileassistant.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.common.views.roundimage.RoundImageView;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.chat.holder.ImageViewHolder;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.customer.CustCacheUtil;
import com.moor.im.options.mobileassistant.customer.adapter.CustomerStatusSPAdapter;
import com.moor.im.options.mobileassistant.customer.fragment.CustomerErpFragment;
import com.moor.im.options.mobileassistant.customer.fragment.CustomerHistoryFragment;
import com.moor.im.options.mobileassistant.customer.fragment.CustomerPlanFragment;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MACustomer;
import com.moor.im.options.mobileassistant.model.QueryData;
import com.moor.imkf.gson.Gson;
import com.moor.imkf.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by longwei on 16/8/23.
 */
public class CustomerDetailActivity extends BaseActivity{

    private CoordinatorLayout customer_detail_cl;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private List<Fragment> mFragmentList;
    private List<String> mTitleList;

    private VPAdapter mVPAdapter;

    private Fragment mCustomerErpFragment, mCustomerPlanFragment, mCustomerHistoryFragment;

    private TextView customer_detail_tv_cust_name, customer_detail_tv_cust_title,
            customer_detail_tv_user_name, customer_detail_tv_time;
    private RoundImageView customer_detail_iv_user_icon;
    private Spinner customer_detail_sp_status, customer_detail_sp_source;
    private CustomerStatusSPAdapter mCustomerStatusSPAdapter, mCustomerSourceSPAdapter;

    private ImageView customer_detail_iv_cust_type, customer_detail_iv_cust_info, customer_detail_iv_cust_edit;
    private LinearLayout customer_detail_ll_cust_type;
    private RelativeLayout customer_detail_ll_title;
    private Toolbar toolbar;

    private MACustomer mCustomer;
    private String custCacheStr;
    private String customerId;

    User user = UserDao.getInstance().getUser();

    private boolean isStatusFirst = true, isSourceFirst = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        Intent intent = getIntent();
        if(intent.getSerializableExtra("customerId") != null) {
            customerId = intent.getStringExtra("customerId");
        }

        customer_detail_cl = (CoordinatorLayout) findViewById(R.id.customer_detail_cl);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);

        customer_detail_tv_cust_name = (TextView) findViewById(R.id.customer_detail_tv_cust_name);
        customer_detail_tv_cust_title = (TextView) findViewById(R.id.customer_detail_tv_cust_title);
        customer_detail_tv_user_name = (TextView) findViewById(R.id.customer_detail_tv_user_name);
        customer_detail_tv_time = (TextView) findViewById(R.id.customer_detail_tv_time);

        customer_detail_iv_user_icon = (RoundImageView) findViewById(R.id.customer_detail_iv_user_icon);
        customer_detail_sp_status = (Spinner) findViewById(R.id.customer_detail_sp_status);
        customer_detail_sp_source = (Spinner) findViewById(R.id.customer_detail_sp_source);

        customer_detail_iv_cust_edit = (ImageView) findViewById(R.id.customer_detail_iv_cust_edit);
        customer_detail_iv_cust_info = (ImageView) findViewById(R.id.customer_detail_iv_cust_info);
        customer_detail_iv_cust_type = (ImageView) findViewById(R.id.customer_detail_iv_cust_type);
        customer_detail_ll_cust_type = (LinearLayout) findViewById(R.id.customer_detail_ll_cust_type);
        customer_detail_ll_title = (RelativeLayout) findViewById(R.id.customer_detail_ll_title);

        customer_detail_iv_cust_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerDetailActivity.this, CustomerInfoActivity.class);
                intent.putExtra("customerId", customerId);
                startActivity(intent);
            }
        });
        customer_detail_iv_cust_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerDetailActivity.this, CustomerEditActivity.class);
                intent.putExtra("customerId", customerId);
                startActivity(intent);
            }
        });





        custCacheStr = MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust);
        showLoadingDialog();
        HttpManager.getInstance().queryCustomerInfo(user._id, customerId, new ResponseListener() {
            @Override
            public void onFailed() {
                dismissLoadingDialog();
            }

            @Override
            public void onSuccess(String responseStr) {
                System.out.println("获取客户详情返回结果:"+responseStr);
                if(HttpParser.getSucceed(responseStr)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONObject cust = jsonObject.getJSONObject("data");
                        Gson gson = new Gson();

                        mCustomer = gson.fromJson(cust.toString(),
                                new TypeToken<MACustomer>() {
                                }.getType());
                        initDetailData();
                        dismissLoadingDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initViewPager() {
        mFragmentList = new ArrayList<>();

        mCustomerErpFragment = new CustomerErpFragment();
        mCustomerPlanFragment = new CustomerPlanFragment();
        mCustomerHistoryFragment = new CustomerHistoryFragment();

        mFragmentList.add(mCustomerPlanFragment);
        mFragmentList.add(mCustomerErpFragment);
        mFragmentList.add(mCustomerHistoryFragment);

        mTitleList = new ArrayList<>();
        mTitleList.add("联系计划");
        mTitleList.add("工单");
        mTitleList.add("联系历史");

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addTab(tabLayout.newTab().setText(mTitleList.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(mTitleList.get(1)));
        tabLayout.addTab(tabLayout.newTab().setText(mTitleList.get(2)));

        mVPAdapter = new VPAdapter(getSupportFragmentManager(), mFragmentList, mTitleList);
        viewPager.setAdapter(mVPAdapter);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(viewPager);

        customer_detail_cl.setVisibility(View.VISIBLE);
    }

    private void initDetailData() {

        if(mCustomer == null) {
            return;
        }
        int s = 0;
        try{
            s = Integer.parseInt(mCustomer.status.substring(6));
        }catch (Exception e) {}
        int resId = getResources().getIdentifier("customer_status"+s, "color", getPackageName());
        customer_detail_ll_cust_type.setBackgroundColor(getResources().getColor(resId));
        toolbar.setBackgroundColor(getResources().getColor(resId));

        int imgResId = getResources().getIdentifier("customer_icon_status"+s, "drawable", getPackageName());
        customer_detail_iv_cust_type.setImageResource(imgResId);

        String custName = mCustomer.name;
        String title = mCustomer.title;
        String createTime = mCustomer.createTime;


        String name = "无归属";
        String im_icon = "";
        MAAgent agent = MobileAssitantCache.getInstance().getAgentById(NullUtil.checkNull(mCustomer.owner));
        if (agent != null) {
            name = agent.displayName;
            im_icon = agent.im_icon;
        }

        if(im_icon != null && !"".equals(im_icon)) {
            GlideUtils.displayNet(customer_detail_iv_user_icon, im_icon);
        }

        customer_detail_tv_cust_name.setText(custName);
        customer_detail_tv_user_name.setText(name);
        customer_detail_tv_time.setText(createTime);

        if(title != null && !"".equals(title)) {
            customer_detail_tv_cust_title.setText(title);
        }else {
            customer_detail_ll_title.setVisibility(View.GONE);
        }

        List<QueryData> statusDatas = new ArrayList<>();
        List<QueryData> sourceDatas = new ArrayList<>();

        if(custCacheStr != null && !"".equals(custCacheStr)) {
            try{
                JSONObject custCache = new JSONObject(custCacheStr);
                JSONObject status = custCache.getJSONObject("status");
                Iterator<String> iterator = status.keys();
                while (iterator.hasNext()) {
                    QueryData queryData = new QueryData();
                    String key = iterator.next();
                    queryData.setName(status.getString(key));
                    queryData.setValue(key);
                    statusDatas.add(queryData);
                }

                JSONArray sourceArray = custCache.getJSONArray("source");
                for(int i=0; i<sourceArray.length(); i++) {
                    JSONObject source = sourceArray.getJSONObject(i);
                    QueryData queryData = new QueryData();
                    queryData.setName(source.getString("name"));
                    queryData.setValue(source.getString("key"));
                    sourceDatas.add(queryData);
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

        }

        mCustomerStatusSPAdapter = new CustomerStatusSPAdapter(CustomerDetailActivity.this, statusDatas);
        customer_detail_sp_status.setAdapter(mCustomerStatusSPAdapter);

        mCustomerSourceSPAdapter = new CustomerStatusSPAdapter(CustomerDetailActivity.this, sourceDatas);
        customer_detail_sp_source.setAdapter(mCustomerSourceSPAdapter);

        for(int i=0; i<statusDatas.size(); i++) {
            if(mCustomer.status.equals(statusDatas.get(i).getValue())) {
                customer_detail_sp_status.setSelection(i);
            }
        }
        for(int i=0; i<sourceDatas.size(); i++) {
            if(mCustomer.custsource1.equals(sourceDatas.get(i).getValue())) {
                customer_detail_sp_source.setSelection(i);
            }
        }

        customer_detail_sp_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!isStatusFirst) {
                    QueryData queryData = (QueryData) parent.getAdapter().getItem(position);

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("_id", mCustomer._id);
                    map.put("status", queryData.getValue());
                    HttpManager.getInstance().updateCustomerStatusOrSource(user._id, map, new ResponseListener() {
                        @Override
                        public void onFailed() {

                        }

                        @Override
                        public void onSuccess(String responseStr) {
                            if(HttpParser.getSucceed(responseStr)) {
                                Toast.makeText(CustomerDetailActivity.this, "状态改变成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                isStatusFirst = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        customer_detail_sp_source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!isSourceFirst) {
                    QueryData queryData = (QueryData) parent.getAdapter().getItem(position);

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("_id", mCustomer._id);
                    map.put("custsource1", queryData.getValue());
                    HttpManager.getInstance().updateCustomerStatusOrSource(user._id, map, new ResponseListener() {
                        @Override
                        public void onFailed() {

                        }

                        @Override
                        public void onSuccess(String responseStr) {
                            if(HttpParser.getSucceed(responseStr)) {
                                Toast.makeText(CustomerDetailActivity.this, "来源改变成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                isSourceFirst = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initViewPager();
    }


    public class VPAdapter extends FragmentPagerAdapter {

        private List<Fragment> list_fragment;
        private List<String> list_Title;



        public VPAdapter(FragmentManager fm, List<Fragment> list_fragment, List<String> list_Title) {
            super(fm);
            this.list_fragment = list_fragment;
            this.list_Title = list_Title;
        }

        @Override
        public Fragment getItem(int position) {
            return list_fragment.get(position);
        }

        @Override
        public int getCount() {
            return list_Title.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return list_Title.get(position % list_Title.size());
        }
    }

    public String getCustomerId() {
        if(mCustomer != null) {
            return mCustomer._id;
        }
        return "";
    }

    public MACustomer getCustomer() {
        return mCustomer;
    }
}
