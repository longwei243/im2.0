package com.moor.im.options.mobileassistant.customer.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.model.MAAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by longwei on 16/8/23.
 */
public class CustomerInfoActivity extends BaseActivity{
    private User user = UserDao.getInstance().getUser();

    private TextView erp_customer_tv_name, erp_customer_tv_source, erp_customer_tv_status,
            erp_customer_tv_owner, erp_customer_tv_batchNo, erp_customer_tv_createTime,
            erp_customer_tv_lastUpdateTime, erp_customer_tv_lastContactTime;
    private LinearLayout erp_customer_ll_fields;
    private ScrollView erp_customer_sv;
    private String customerId;

    private String custCacheStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_info);

        customerId = getIntent().getStringExtra("customerId");

        erp_customer_tv_name = (TextView) findViewById(R.id.erp_customer_tv_name);
        erp_customer_tv_source = (TextView) findViewById(R.id.erp_customer_tv_source);
        erp_customer_tv_status = (TextView) findViewById(R.id.erp_customer_tv_status);
        erp_customer_tv_owner = (TextView) findViewById(R.id.erp_customer_tv_owner);
        erp_customer_tv_batchNo = (TextView) findViewById(R.id.erp_customer_tv_batchNo);
        erp_customer_tv_createTime = (TextView) findViewById(R.id.erp_customer_tv_createTime);
        erp_customer_tv_lastUpdateTime = (TextView) findViewById(R.id.erp_customer_tv_lastUpdateTime);
        erp_customer_tv_lastContactTime = (TextView) findViewById(R.id.erp_customer_tv_lastContactTime);

        erp_customer_ll_fields = (LinearLayout) findViewById(R.id.erp_customer_ll_fields);
        erp_customer_sv = (ScrollView) findViewById(R.id.erp_customer_sv);

        ImageButton titlebar_back = (ImageButton) findViewById(R.id.titlebar_back);
        titlebar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        HttpManager.getInstance().queryCustomerInfo(user._id, customerId, new ResponseListener() {
            @Override
            public void onFailed() {

            }

            @Override
            public void onSuccess(String responseStr) {
                System.out.println("customer info获取详情数据:"+responseStr);
                initData(responseStr);
            }
        });

    }

    private void initData(String responseStr) {
        try{
            JSONObject jsonObject1 = new JSONObject(responseStr);
            if("true".equals(jsonObject1.getString("Succeed"))) {
                JSONObject jsonObject = jsonObject1.getJSONObject("data");
                String name = jsonObject.getString("name");
                erp_customer_tv_name.setText(name);

                String createTime = jsonObject.getString("createTime");
                erp_customer_tv_createTime.setText(createTime);

                try{
                    String lastUpdateTime = jsonObject.getString("lastUpdateTime");
                    erp_customer_tv_lastUpdateTime.setText(lastUpdateTime);
                }catch (JSONException e){}

                try{
                    String lastContactTime = jsonObject.getString("lastContactTime");
                    erp_customer_tv_lastContactTime.setText(lastContactTime);
                }catch (JSONException e){}


//                    String batchNo = jsonObject.getString("batchNo");
//                    erp_customer_tv_batchNo.setText(batchNo);

                String agentId = jsonObject.getString("owner");
                MAAgent agent = MobileAssitantCache.getInstance().getAgentById(agentId);
                if(agent != null) {
                    erp_customer_tv_owner.setText(agent.displayName);
                }

                if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust) != null) {
                    custCacheStr = MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust);


                    String id = jsonObject.getString("dbType");
                    String statusStr = jsonObject.getString("status");
                    initStatus(custCacheStr, statusStr, id);
                    String sourceStr = jsonObject.getString("custsource1");
                    initSource(custCacheStr, sourceStr, id);

                    //固定字段
                    initStableFields(custCacheStr, id, jsonObject);

                    //自定义字段
                    initFields(custCacheStr, id, jsonObject);
                    erp_customer_sv.setVisibility(View.VISIBLE);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义字段
     * @param custCacheStr
     * @param id
     * @param jsonObject
     */
    private void initFields(String custCacheStr, String id, JSONObject jsonObject) {
        try {
            JSONArray fields = jsonObject.getJSONArray("fields");
            for (int i=0; i<fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                String t = field.getString("t");
                String k = field.getString("k");
                String name = field.getString("n");

                if("province".equals(k) || "city".equals(k) || "address".equals(k)) {
                    continue;
                }

                if("dropdown".equals(t) || "radio".equals(t) || "checkbox".equals(t)) {


                    StringBuilder result = new StringBuilder();

                        JSONObject cust = new JSONObject(custCacheStr);
                        if (id.equals(cust.getString("_id"))) {
                            JSONArray custom_fields = cust.getJSONArray("custom_fields");
                            for(int m=custom_fields.length()-1; m>=0; m--) {
                                JSONObject cf = custom_fields.getJSONObject(m);
                                if(cf.getString("_id").equals(k)) {
                                    JSONObject choices = cf.getJSONObject("choices");
                                    Iterator<String> iterator = choices.keys();
                                    while (iterator.hasNext()) {
                                        String key = iterator.next();
                                        if("checkbox".equals(t)) {
                                            JSONArray v = field.getJSONArray("v");
                                            for(int p=0; p<v.length(); p++) {
                                                if(key.equals(v.getString(p))) {
                                                    result.append(choices.getString(key) + " ");
                                                }
                                            }
                                        }else {
                                            String v = field.getString("v");
                                            if(key.equals(v)) {
                                                result.append(choices.getString(key) + " ");
                                            }
                                        }
                                    }

                                }
                            }
                        }

                    LinearLayout rl = (LinearLayout) LayoutInflater.from(CustomerInfoActivity.this).inflate(R.layout.erp_customer_field_item, null);
                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                    erp_customer_field_tv_name.setText(name);
                    erp_customer_field_tv_value.setText(result.toString());
                    erp_customer_ll_fields.addView(rl);
                }else if("sex".equals(k)) {
                    String v = field.getString("v");
                    String value = "";
                    if("0".equals(v)) {
                        value = "男";
                    }else if("1".equals(v)) {
                        value = "女";
                    }
                    LinearLayout rl = (LinearLayout) LayoutInflater.from(CustomerInfoActivity.this).inflate(R.layout.erp_customer_field_item, null);
                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                    erp_customer_field_tv_name.setText(name);
                    erp_customer_field_tv_value.setText(value);
                    erp_customer_ll_fields.addView(rl);

                }else {
                    String v = field.getString("v");
                    LinearLayout rl = (LinearLayout) LayoutInflater.from(CustomerInfoActivity.this).inflate(R.layout.erp_customer_field_item, null);
                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                    erp_customer_field_tv_name.setText(name);
                    erp_customer_field_tv_value.setText(v);
                    erp_customer_ll_fields.addView(rl);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 固定字段
     * @param custCacheStr
     * @param id
     */
    private void initStableFields(String custCacheStr, String id, JSONObject jb) {

        try {

                JSONObject cust = new JSONObject(custCacheStr);
                if(id.equals(cust.getString("_id"))) {
                    JSONArray stable_fields = cust.getJSONArray("stable_fields");
                    for(int j=0; j<stable_fields.length(); j++) {
                        JSONObject sf = stable_fields.getJSONObject(j);
                        if("name".equals(sf.getString("name"))) {
                            continue;
                        }
                        if("phone".equals(sf.getString("name"))) {
                            if(jb.getJSONArray("phone") != null) {
                                JSONArray phoneArray = jb.getJSONArray("phone");
                                for(int p=0; p<phoneArray.length(); p++) {
                                    JSONObject phone = phoneArray.getJSONObject(p);
                                    String name = sf.getString("value");
                                    final String tel = phone.getString("tel");
                                    LinearLayout rl = (LinearLayout) LayoutInflater.from(CustomerInfoActivity.this).inflate(R.layout.erp_customer_field_item_phone, null);
                                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                                    erp_customer_field_tv_name.setText(name);
                                    erp_customer_field_tv_value.setText(tel);

                                    erp_customer_ll_fields.addView(rl);
                                }
                            }

                        }else if("email".equals(sf.getString("name"))) {
                            if(jb.getJSONArray("email") != null) {
                                JSONArray phoneArray = jb.getJSONArray("email");
                                for(int p=0; p<phoneArray.length(); p++) {
                                    JSONObject phone = phoneArray.getJSONObject(p);
                                    String name = sf.getString("value");
                                    String tel = phone.getString("email");
                                    LinearLayout rl = (LinearLayout) LayoutInflater.from(CustomerInfoActivity.this).inflate(R.layout.erp_customer_field_item, null);
                                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                                    erp_customer_field_tv_name.setText(name);
                                    erp_customer_field_tv_value.setText(tel);
                                    erp_customer_ll_fields.addView(rl);
                                }
                            }

                        }else if("weixin".equals(sf.getString("name"))) {
                            if(jb.getJSONArray("weixin") != null) {
                                JSONArray phoneArray = jb.getJSONArray("weixin");
                                for(int p=0; p<phoneArray.length(); p++) {
                                    JSONObject phone = phoneArray.getJSONObject(p);
                                    String name = sf.getString("value");
                                    String tel = phone.getString("num");
                                    LinearLayout rl = (LinearLayout) LayoutInflater.from(CustomerInfoActivity.this).inflate(R.layout.erp_customer_field_item, null);
                                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                                    erp_customer_field_tv_name.setText(name);
                                    erp_customer_field_tv_value.setText(tel);
                                    erp_customer_ll_fields.addView(rl);
                                }
                            }
                        }

                    }

                }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 状态
     * @param custCacheStr
     * @param statusStr
     */
    private void initStatus(String custCacheStr, String statusStr, String id) {
        try {

                JSONObject cust = new JSONObject(custCacheStr);
                if(id.equals(cust.getString("_id"))) {
                    JSONObject status = cust.getJSONObject("status");
                    erp_customer_tv_status.setText(status.getString(statusStr));
                }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 来源
     * @param custCacheStr
     * @param sourceStr
     */
    private void initSource(String custCacheStr, String sourceStr, String id) {
        try {

                JSONObject cust = new JSONObject(custCacheStr);
                if(id.equals(cust.getString("_id"))) {
                    JSONArray source = cust.getJSONArray("source");
                    for (int j=0; j<source.length(); j++) {
                        JSONObject jb = source.getJSONObject(j);
                        if(sourceStr.equals(jb.getString("key"))) {
                            String value = jb.getString("name");
                            erp_customer_tv_source.setText(value);
                            break;
                        }
                    }
                }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
