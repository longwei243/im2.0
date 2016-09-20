package com.moor.im.options.mobileassistant.customer.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.CacheKey;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.Utils;
import com.moor.im.common.views.GridViewInScrollView;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.cdr.adapter.SPAdapter;
import com.moor.im.options.mobileassistant.customer.adapter.CustomerCBAdapter;
import com.moor.im.options.mobileassistant.erp.adapter.ErpSpAdapter;
import com.moor.im.options.mobileassistant.model.MAOption;
import com.moor.im.options.mobileassistant.model.Option;
import com.moor.im.options.mobileassistant.model.QueryData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by longwei on 16/9/20.
 */
public class CustomerHighQueryActivity extends BaseActivity{

    private String menu, dbType;
    private Spinner customer_high_query_sp_status,
            customer_high_query_sp_source, customer_high_query_sp_province,
            customer_high_query_sp_city;

    private EditText customer_high_query_et_name, customer_high_query_et_phone,
            customer_high_query_et_notifyTime_begin_date, customer_high_query_et_notifyTime_end_date,
            customer_high_query_et_lastUpdateTime_begin_date, customer_high_query_et_lastUpdateTime_end_date,
            customer_high_query_et_createTime_begin_date, customer_high_query_et_createTime_end_date;

    private LinearLayout customer_high_query_ll_field;

    private Button customer_high_query_btn_reset, customer_high_query_btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_highquery);

        Intent intent = getIntent();
        menu = intent.getStringExtra("menu");
        dbType = intent.getStringExtra("dbType");

        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("高级查询");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        customer_high_query_sp_status = (Spinner) findViewById(R.id.customer_high_query_sp_status);
        customer_high_query_sp_source = (Spinner) findViewById(R.id.customer_high_query_sp_source);
        customer_high_query_sp_province = (Spinner) findViewById(R.id.customer_high_query_sp_province);
        customer_high_query_sp_city = (Spinner) findViewById(R.id.customer_high_query_sp_city);

        customer_high_query_et_name = (EditText) findViewById(R.id.customer_high_query_et_name);
        customer_high_query_et_phone = (EditText) findViewById(R.id.customer_high_query_et_phone);
        customer_high_query_et_notifyTime_begin_date = (EditText) findViewById(R.id.customer_high_query_et_notifyTime_begin_date);
        customer_high_query_et_notifyTime_end_date = (EditText) findViewById(R.id.customer_high_query_et_notifyTime_end_date);
        customer_high_query_et_lastUpdateTime_begin_date = (EditText) findViewById(R.id.customer_high_query_et_lastUpdateTime_begin_date);
        customer_high_query_et_lastUpdateTime_end_date = (EditText) findViewById(R.id.customer_high_query_et_lastUpdateTime_end_date);
        customer_high_query_et_createTime_begin_date = (EditText) findViewById(R.id.customer_high_query_et_createTime_begin_date);
        customer_high_query_et_createTime_end_date = (EditText) findViewById(R.id.customer_high_query_et_createTime_end_date);

        customer_high_query_ll_field = (LinearLayout) findViewById(R.id.customer_high_query_ll_field);

        customer_high_query_btn_reset = (Button) findViewById(R.id.customer_high_query_btn_reset);
        customer_high_query_btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        customer_high_query_btn_confirm = (Button) findViewById(R.id.customer_high_query_btn_confirm);
        customer_high_query_btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitQuery();
            }
        });

        initData();
    }

    private void initData() {



        if (MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust) != null) {
            String custCacheStr = MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust);
            try {
                JSONObject custCache = new JSONObject(custCacheStr);
                createCustomFieldView(custCache);

                List<QueryData> sourceDatas = new ArrayList<>();
                JSONArray sourceArray = custCache.getJSONArray("source");
                QueryData qd_source_null = new QueryData();
                qd_source_null.setName("请选择");
                qd_source_null.setValue("");
                sourceDatas.add(qd_source_null);
                for(int i=0; i<sourceArray.length(); i++) {
                    JSONObject source = sourceArray.getJSONObject(i);
                    QueryData queryData = new QueryData();
                    queryData.setName(source.getString("name"));
                    queryData.setValue(source.getString("key"));
                    sourceDatas.add(queryData);
                }
                SPAdapter sourceAdapter = new SPAdapter(CustomerHighQueryActivity.this, sourceDatas);
                customer_high_query_sp_source.setAdapter(sourceAdapter);



                List<QueryData> statusList = new ArrayList<>();
                JSONObject status = custCache.getJSONObject("status");
                QueryData qd_status_null = new QueryData();
                qd_status_null.setName("请选择");
                qd_status_null.setValue("");
                statusList.add(qd_source_null);
                Iterator<String> iterator = status.keys();
                while (iterator.hasNext()) {
                    QueryData queryData = new QueryData();
                    String key = iterator.next();
                    queryData.setName(status.getString(key));
                    queryData.setValue(key);
                    statusList.add(queryData);
                }
                SPAdapter adapter = new SPAdapter(CustomerHighQueryActivity.this, statusList);
                customer_high_query_sp_status.setAdapter(adapter);



                MAOption maoption = MobileAssitantCache.getInstance().getMAOption("d7b9c68a-b50f-21d1-d5fd-41ea93f5f49c");
                final List<Option> firstOption = maoption.options;
                Option op_null = new Option();
                op_null.key = "";
                op_null.name = "请选择";
                firstOption.add(0, op_null);
                final ErpSpAdapter provinceAdapter = new ErpSpAdapter(CustomerHighQueryActivity.this, firstOption);
                customer_high_query_sp_province.setAdapter(provinceAdapter);

                customer_high_query_sp_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Option o = (Option) parent.getAdapter().getItem(position);
                        List<Option> secondOptions = getOptionsByKey(firstOption, o.key);
                        if(secondOptions != null) {
                            Option op_null = new Option();
                            op_null.key = "";
                            op_null.name = "请选择";
                            secondOptions.add(0, op_null);
                            ErpSpAdapter adapter = new ErpSpAdapter(CustomerHighQueryActivity.this, secondOptions);
                            customer_high_query_sp_city.setAdapter(adapter);
                        }

                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


                customer_high_query_et_notifyTime_begin_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateTimePiker(customer_high_query_et_notifyTime_begin_date);
                    }
                });
                customer_high_query_et_notifyTime_end_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateTimePiker(customer_high_query_et_notifyTime_end_date);
                    }
                });
                customer_high_query_et_lastUpdateTime_begin_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateTimePiker(customer_high_query_et_lastUpdateTime_begin_date);
                    }
                });
                customer_high_query_et_lastUpdateTime_end_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateTimePiker(customer_high_query_et_lastUpdateTime_end_date);
                    }
                });
                customer_high_query_et_createTime_begin_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateTimePiker(customer_high_query_et_createTime_begin_date);
                    }
                });
                customer_high_query_et_createTime_end_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateTimePiker(customer_high_query_et_createTime_end_date);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCustomFieldView(JSONObject custCache) {

        try {
            JSONArray custom_fields = custCache.getJSONArray("custom_fields");
            for (int i = 0; i < custom_fields.length(); i++) {
                JSONObject cf = custom_fields.getJSONObject(i);

                if("single".equals(cf.getString("type"))) {
                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_single, null);
                    singleView.setTag("single");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(cf.getString("name"));
                    erp_field_single_tv_name.setTag(cf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(cf.getString("_id"));

                    customer_high_query_ll_field.addView(singleView);
                }else if("multi".equals(cf.getString("type"))) {
                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_mutli, null);
                    singleView.setTag("single");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(cf.getString("name"));
                    erp_field_single_tv_name.setTag(cf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(cf.getString("_id"));

                    customer_high_query_ll_field.addView(singleView);
                }else if("number".equals(cf.getString("type"))) {
                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_single, null);
                    singleView.setTag("single");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(cf.getString("name"));
                    erp_field_single_tv_name.setTag(cf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(cf.getString("_id"));

                    customer_high_query_ll_field.addView(singleView);
                }else if("date".equals(cf.getString("type"))) {
                    LinearLayout birthView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_birth, null);
                    birthView.setTag("single");
                    TextView erp_field_single_tv_name = (TextView) birthView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(cf.getString("name"));
                    erp_field_single_tv_name.setTag(cf.getString("required"));
                    final EditText erp_field_single_et_value = (EditText) birthView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(cf.getString("_id"));
                    erp_field_single_et_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDatePiker(erp_field_single_et_value);
                        }
                    });

                    customer_high_query_ll_field.addView(birthView);
                }else if("dropdown".equals(cf.getString("type"))) {
                    LinearLayout firstItemRL = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_dropdown, null);
                    firstItemRL.setTag("dropdown");
                    TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_dropdown_item_tv_name.setText(cf.getString("name"));
                    List<QueryData> datas = new ArrayList<>();
                    QueryData qd_null = new QueryData();
                    qd_null.setName("请选择");
                    qd_null.setValue("");
                    datas.add(qd_null);
                    JSONObject status = cf.getJSONObject("choices");
                    Iterator<String> iterator = status.keys();
                    while (iterator.hasNext()) {
                        QueryData queryData = new QueryData();
                        String key = iterator.next();
                        queryData.setName(status.getString(key));
                        queryData.setValue(key);
                        datas.add(queryData);
                    }
                    Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.customer_edit_field_sp);
                    erp_field_dropdown_item_sp_value.setTag(cf.getString("_id"));
                    SPAdapter adapter = new SPAdapter(CustomerHighQueryActivity.this, datas);
                    erp_field_dropdown_item_sp_value.setAdapter(adapter);

                    customer_high_query_ll_field.addView(firstItemRL);

                }else if("checkbox".equals(cf.getString("type"))) {
                    initCheckBoxView(cf);
                }else if("radio".equals(cf.getString("type"))) {
                    initRadioView(cf);
                }

            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDatePiker(final EditText et) {
        Calendar d = Calendar.getInstance(Locale.CHINA);
        //创建一个日历引用d，通过静态方法getInstance() 从指定时区 Locale.CHINA 获得一个日期实例
        Date myDate = new Date();
        //创建一个Date实例
        d.setTime(myDate);
        //设置日历的时间，把一个新建Date实例myDate传入
        int year = d.get(Calendar.YEAR);
        int month = d.get(Calendar.MONTH);
        int day = d.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(CustomerHighQueryActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String monthStr  = "";
                if((monthOfYear+1) < 10) {
                    monthStr = "0"+(monthOfYear+1);
                }else {
                    monthStr = (monthOfYear+1) + "";
                }
                String dayStr  = "";
                if(dayOfMonth < 10) {
                    dayStr = "0"+dayOfMonth;
                }else {
                    dayStr = dayOfMonth + "";
                }
                final String data = year+"-"+monthStr+"-"+dayStr;
                et.setText(data);
            }
        }, year, month, day) {
            @Override
            protected void onStop() {
//                super.onStop();
            }
        };
        dpd.show();
    }

    private void showDateTimePiker(final EditText editText) {
        Calendar d = Calendar.getInstance(Locale.CHINA);
        //创建一个日历引用d，通过静态方法getInstance() 从指定时区 Locale.CHINA 获得一个日期实例
        Date myDate = new Date();
        //创建一个Date实例
        d.setTime(myDate);
        //设置日历的时间，把一个新建Date实例myDate传入
        int year = d.get(Calendar.YEAR);
        int month = d.get(Calendar.MONTH);
        int day = d.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(CustomerHighQueryActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String monthStr  = "";
                if((monthOfYear+1) < 10) {
                    monthStr = "0"+(monthOfYear+1);
                }else {
                    monthStr = (monthOfYear+1) + "";
                }
                String dayStr  = "";
                if(dayOfMonth < 10) {
                    dayStr = "0"+dayOfMonth;
                }else {
                    dayStr = dayOfMonth + "";
                }
                final String data = year+"-"+monthStr+"-"+dayStr;
                Calendar d = Calendar.getInstance(Locale.CHINA);
                int hour = d.get(Calendar.HOUR_OF_DAY);
                int minute = d.get(Calendar.MINUTE);
                TimePickerDialog tpd = new TimePickerDialog(CustomerHighQueryActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hourStr  = "";
                        if(hourOfDay < 10) {
                            hourStr = "0"+hourOfDay;
                        }else {
                            hourStr = hourOfDay + "";
                        }
                        String minuteStr  = "";
                        if(minute < 10) {
                            minuteStr = "0"+minute;
                        }else {
                            minuteStr = minute + "";
                        }
                        String time = hourStr + ":" + minuteStr;
                        String result = data + " " + time;
                        editText.setText(result);
                    }
                }, hour, minute, true) {
                    @Override
                    protected void onStop() {
//                        super.onStop();
                    }
                };
                tpd.show();
            }
        }, year, month, day) {
            @Override
            protected void onStop() {
//                super.onStop();
            }
        };
        dpd.show();
    }

    private void initCheckBoxView(JSONObject cf) {
        try{
            LinearLayout checkboxView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_field_checkbox, null);
            checkboxView.setTag("checkbox");
            TextView erp_field_checkbox_tv_name = (TextView) checkboxView.findViewById(R.id.customer_field_checkbox_tv_name);
            erp_field_checkbox_tv_name.setText(cf.getString("name"));
            erp_field_checkbox_tv_name.setTag(cf.getString("required"));
            GridViewInScrollView checkbox_gv = (GridViewInScrollView) checkboxView.findViewById(R.id.customer_field_checkbox_gv_value);
            checkbox_gv.setTag(cf.getString("_id"));


            List<QueryData> datas = new ArrayList<>();
            JSONObject status = cf.getJSONObject("choices");
            Iterator<String> iterator = status.keys();
            while (iterator.hasNext()) {
                QueryData queryData = new QueryData();
                String key = iterator.next();
                queryData.setName(status.getString(key));
                queryData.setValue(key);
                datas.add(queryData);
            }
            final CustomerCBAdapter adapter = new CustomerCBAdapter(CustomerHighQueryActivity.this, datas);
            checkbox_gv.setAdapter(adapter);
            checkbox_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CustomerCBAdapter.ViewHolder holder = (CustomerCBAdapter.ViewHolder) view.getTag();
                    holder.cb.toggle();
                    if (holder.cb.isChecked()) {
                        adapter.getIsSelected().put(position, true);
                    } else {
                        adapter.getIsSelected().put(position, false);
                    }
                }
            });

            customer_high_query_ll_field.addView(checkboxView);

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRadioView(JSONObject cf) {
        try{
            LinearLayout radioView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_field_radio, null);
            radioView.setTag("radio");
            TextView erp_field_radio_tv_name = (TextView) radioView.findViewById(R.id.customer_field_radio_tv_name);
            erp_field_radio_tv_name.setText(cf.getString("name"));
            erp_field_radio_tv_name.setTag(cf.getString("required"));
            RadioGroup radioGroup = (RadioGroup) radioView.findViewById(R.id.customer_field_radio_rg_value);
            radioGroup.setTag(cf.getString("_id"));

            List<QueryData> datas = new ArrayList<>();
            JSONObject status = cf.getJSONObject("choices");
            Iterator<String> iterator = status.keys();
            while (iterator.hasNext()) {
                QueryData queryData = new QueryData();
                String key = iterator.next();
                queryData.setName(status.getString(key));
                queryData.setValue(key);
                datas.add(queryData);
            }
            if(datas.size() > 2) {
                radioGroup.setOrientation(RadioGroup.VERTICAL);
            }
            for (int i=0; i<datas.size(); i++) {
                QueryData qd = datas.get(i);
                RadioButton rb = new RadioButton(CustomerHighQueryActivity.this);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rb.setLayoutParams(lp);
                rb.setText(qd.getName());
                rb.setTag(qd.getValue());
                rb.setId(View.generateViewId());
                radioGroup.addView(rb);
            }
            customer_high_query_ll_field.addView(radioView);
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private List<Option> getOptionsByKey(List<Option> o, String key) {
        if("".equals(key)) {
            List<Option> options = new ArrayList<>();
            return options;
        }
        for(int i=0; i<o.size(); i++) {
            if(key.equals(o.get(i).key)) {
                return o.get(i).options;
            }
        }
        return null;
    }

    private void submitQuery() {
        try{

            JSONObject datas = new JSONObject();
            String name = customer_high_query_et_name.getText().toString().trim();
            if(!"".equals(name)) {
                datas.put("name", name);
            }
            String tel = customer_high_query_et_phone.getText().toString().trim();
            if(!"".equals(tel)) {
                datas.put("phone.tel", tel);
            }
            String notifyTime_begin_date = customer_high_query_et_notifyTime_begin_date.getText().toString().trim();
            if(!"".equals(notifyTime_begin_date)) {
                datas.put("notifyTime_begin_date", notifyTime_begin_date);
            }
            String notifyTime_end_date = customer_high_query_et_notifyTime_end_date.getText().toString().trim();
            if(!"".equals(notifyTime_end_date)) {
                datas.put("notifyTime_end_date", notifyTime_end_date);
            }
            String lastUpdateTime_begin_date = customer_high_query_et_lastUpdateTime_begin_date.getText().toString().trim();
            if(!"".equals(lastUpdateTime_begin_date)) {
                datas.put("lastUpdateTime_begin_date", lastUpdateTime_begin_date);
            }
            String lastUpdateTime_end_date = customer_high_query_et_lastUpdateTime_end_date.getText().toString().trim();
            if(!"".equals(lastUpdateTime_end_date)) {
                datas.put("lastUpdateTime_end_date", lastUpdateTime_end_date);
            }
            String createTime_begin_date = customer_high_query_et_createTime_begin_date.getText().toString().trim();
            if(!"".equals(createTime_begin_date)) {
                datas.put("createTime_begin_date", createTime_begin_date);
            }
            String createTime_end_date = customer_high_query_et_createTime_end_date.getText().toString().trim();
            if(!"".equals(createTime_end_date)) {
                datas.put("createTime_end_date", createTime_end_date);
            }


            QueryData statusqueryData = (QueryData) customer_high_query_sp_status.getSelectedItem();
            if(statusqueryData != null) {
                if(!"".equals(statusqueryData.getValue())) {
                    datas.put("status", statusqueryData.getValue());
                }
            }

            QueryData sourceData = (QueryData) customer_high_query_sp_source.getSelectedItem();
            if(sourceData != null) {
                if(!"".equals(sourceData.getValue())) {
                    datas.put("custsource1", sourceData.getValue());
                }
            }

            Option option = (Option) customer_high_query_sp_province.getSelectedItem();
            if(!"".equals(option.key)) {
                datas.put("province", option.key);
            }
            Option option_city = (Option) customer_high_query_sp_city.getSelectedItem();
            if(!"".equals(option_city.key)) {
                datas.put("city", option_city.key);
            }

            final int custom_child_count = customer_high_query_ll_field.getChildCount();
            for(int c=0; c<custom_child_count; c++) {
                LinearLayout childView = (LinearLayout) customer_high_query_ll_field.getChildAt(c);
                String type = (String) childView.getTag();
                switch (type) {
                    case "single":
                        EditText et_single = (EditText) childView.getChildAt(1);
                        String id_single = (String) et_single.getTag();
                        String value_single = et_single.getText().toString().trim();
                        datas.put(id_single, value_single);

                        break;
                    case "dropdown":
                        Spinner sp_status = (Spinner) childView.getChildAt(1);
                        String id_status = (String) sp_status.getTag();
                        String value_status = ((QueryData)sp_status.getSelectedItem()).getValue();
                        datas.put(id_status, value_status);
                        break;
                    case "checkbox":
                        JSONArray ja = new JSONArray();
                        GridViewInScrollView gv = (GridViewInScrollView) childView.getChildAt(1);
                        String cbFieldId = (String) gv.getTag();
                        List<QueryData> options = ((CustomerCBAdapter)gv.getAdapter()).getOptions();
                        HashMap<Integer, Boolean> selected = ((CustomerCBAdapter)gv.getAdapter()).getIsSelected();
                        for (int o = 0; o < selected.size(); o++) {
                            if(selected.get(o)) {
                                QueryData option_cb = options.get(o);
                                ja.put(option_cb.getValue());
                            }
                        }

                        datas.put(cbFieldId, ja);

                        break;
                    case "radio":
                        RadioGroup radioGroup = (RadioGroup) childView.getChildAt(1);
                        String id_sex = (String) radioGroup.getTag();
                        int selectId = radioGroup.getCheckedRadioButtonId();

                        if(selectId != -1) {
                            RadioButton rb = (RadioButton) radioGroup.findViewById(selectId);
                            String value_sex = (String) rb.getTag();
                            datas.put(id_sex, value_sex);
                        }
                        break;
                    default:
                        break;
                }
            }

            datas.put("menu", menu);
            datas.put("page", 1);
            datas.put("limit", 10);
            datas.put("dbType", dbType);

            Intent dataIntent = new Intent();
            dataIntent.putExtra("highQueryData", datas.toString());
            setResult(Activity.RESULT_OK, dataIntent);
            finish();
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

}
