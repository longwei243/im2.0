package com.moor.im.options.mobileassistant.customer.activity;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.dialog.dialogplus.DialogPlus;
import com.moor.im.common.dialog.dialogplus.ListHolder;
import com.moor.im.common.dialog.dialogplus.OnItemClickListener;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.roundimage.RoundImageView;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.chat.holder.ImageViewHolder;
import com.moor.im.options.contacts.activity.ContactsDetailActivity;
import com.moor.im.options.dial.dialog.CallChoiseDialog;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.customer.CustCacheUtil;
import com.moor.im.options.mobileassistant.customer.adapter.CustomerStatusSPAdapter;
import com.moor.im.options.mobileassistant.customer.adapter.SimpleAdapter;
import com.moor.im.options.mobileassistant.customer.fragment.CustomerErpFragment;
import com.moor.im.options.mobileassistant.customer.fragment.CustomerHistoryFragment;
import com.moor.im.options.mobileassistant.customer.fragment.CustomerPlanFragment;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MACustomer;
import com.moor.im.options.mobileassistant.model.MACustomerEmail;
import com.moor.im.options.mobileassistant.model.MACustomerPhone;
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

import rx.functions.Action1;

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

    private ImageView customer_iv_phone, customer_iv_sms, customer_iv_email;

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

        ImageButton titlebar_back = (ImageButton) findViewById(R.id.titlebar_back);
        titlebar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        customer_iv_phone = (ImageView) findViewById(R.id.customer_iv_phone);
        customer_iv_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mCustomer != null) {
                    List<String> phoneNumList = new ArrayList<String>();
                    List<MACustomerPhone> phones = mCustomer.phone;
                    if(phones != null && phones.size() > 0) {
                        if(phones.size() == 1) {
                            String num = phones.get(0).tel.trim();
                            if (!"".equals(num)) {
                                Intent intent = new Intent(CustomerDetailActivity.this, CallChoiseDialog.class);
                                intent.putExtra(M7Constant.PHONE_NUM, num);
                                startActivity(intent);
                            }
                        }else {
                            for(int p=0; p<phones.size(); p++) {
                                MACustomerPhone phone = phones.get(p);
                                String num = phone.tel.trim();
                                if(!"".equals(num)) {
                                    phoneNumList.add(num);
                                }
                            }
                            showPhonePopDialog(phoneNumList);
                        }
                    }else {
                        Toast.makeText(CustomerDetailActivity.this, "客户没有电话", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        customer_iv_sms = (ImageView) findViewById(R.id.customer_iv_sms);
        customer_iv_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mCustomer != null) {
                    List<String> phoneNumList = new ArrayList<String>();
                    List<MACustomerPhone> phones = mCustomer.phone;
                    if(phones != null && phones.size() > 0) {
                        if(phones.size() == 1) {
                            String num = phones.get(0).tel.trim();
                            if (!"".equals(num)) {
                                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+num));
                                startActivity(intent);
                            }
                        }else {
                            for(int p=0; p<phones.size(); p++) {
                                MACustomerPhone phone = phones.get(p);
                                String num = phone.tel.trim();
                                if(!"".equals(num)) {
                                    phoneNumList.add(num);
                                }
                            }
                            showSmsPopDialog(phoneNumList);
                        }
                    }else {
                        Toast.makeText(CustomerDetailActivity.this, "客户没有电话", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        customer_iv_email = (ImageView) findViewById(R.id.customer_iv_email);
        customer_iv_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mCustomer != null) {
                    List<String> emailNumList = new ArrayList<String>();
                    List<MACustomerEmail> emails = mCustomer.email;
                    if(emails != null && emails.size() > 0) {
                        if(emails.size() == 1) {
                            String num = emails.get(0).email.trim();
                            if (!"".equals(num)) {
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:"+num));
                                startActivity(intent);
                            }
                        }else {
                            for(int p=0; p<emails.size(); p++) {
                                MACustomerEmail email = emails.get(p);
                                String num = email.email.trim();
                                if(!"".equals(num)) {
                                    emailNumList.add(num);
                                }
                            }
                            showEmailPopDialog(emailNumList);
                        }
                    }else {
                        Toast.makeText(CustomerDetailActivity.this, "客户没有邮箱", Toast.LENGTH_SHORT).show();
                    }
                }

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

        mCompositeSubscription.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if(o instanceof MACustomer) {
                            HttpManager.getInstance().queryCustomerInfo(user._id, customerId, new ResponseListener() {
                                @Override
                                public void onFailed() {

                                }

                                @Override
                                public void onSuccess(String responseStr) {
                                    if(HttpParser.getSucceed(responseStr)) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(responseStr);
                                            JSONObject cust = jsonObject.getJSONObject("data");
                                            Gson gson = new Gson();
                                            mCustomer = gson.fromJson(cust.toString(),
                                                    new TypeToken<MACustomer>() {
                                                    }.getType());
                                            isStatusFirst = true;
                                            isSourceFirst = true;
                                            initDetailData();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }));
    }

    private void showPhonePopDialog(final List<String> phoneNumList) {

        SimpleAdapter adapter = new SimpleAdapter(CustomerDetailActivity.this, phoneNumList);

        DialogPlus.newDialog(this)
                .setContentHolder(new ListHolder())
                .setCancelable(true)
                .setAdapter(adapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        Intent intent = new Intent(CustomerDetailActivity.this, CallChoiseDialog.class);
                        intent.putExtra(M7Constant.PHONE_NUM, phoneNumList.get(position));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

    }

    private void showSmsPopDialog(final List<String> phoneNumList) {

        SimpleAdapter adapter = new SimpleAdapter(CustomerDetailActivity.this, phoneNumList);

        DialogPlus.newDialog(this)
                .setContentHolder(new ListHolder())
                .setCancelable(true)
                .setAdapter(adapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumList.get(position)));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

    }

    private void showEmailPopDialog(final List<String> emailNumList) {

        SimpleAdapter adapter = new SimpleAdapter(CustomerDetailActivity.this, emailNumList);

        DialogPlus.newDialog(this)
                .setContentHolder(new ListHolder())
                .setCancelable(true)
                .setAdapter(adapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"+emailNumList.get(position)));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

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
        String time = mCustomer.lastUpdateTime;


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
        customer_detail_tv_time.setText(time);

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
