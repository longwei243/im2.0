package com.moor.im.options.mobileassistant.customer.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.common.utils.log.ObjParser;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.cdr.adapter.SPAdapter;
import com.moor.im.options.mobileassistant.customer.activity.CustomerDetailActivity;
import com.moor.im.options.mobileassistant.model.MACustomer;
import com.moor.im.options.mobileassistant.model.QueryData;
import com.moor.imkf.gson.Gson;
import com.moor.imkf.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.pjsip.pjsua.SWIGTYPE_p_p_pjmedia_port;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by longwei on 16/8/23.
 */
public class CustomerPlanFragment extends BaseLazyFragment{

    private EditText customer_plan_et_content, customer_plan_et_time;
    private Spinner customer_plan_sp_quicktime;
    private Button customer_plan_btn_cancel, customer_plan_btn_submit;

    private LinearLayout customer_plan_ll_add, customer_plan_ll_show;

    private CheckBox customer_plan_cb_done;
    private TextView customer_plan_tv_action, customer_plan_tv_notifytime;

    private SPAdapter mSPAdapter;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    SimpleDateFormat defaultsdf = new SimpleDateFormat("yyyy-MM-dd");

    User user = UserDao.getInstance().getUser();
    MACustomer customer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_plan, null);
        customer_plan_et_content = (EditText) view.findViewById(R.id.customer_plan_et_content);
        customer_plan_et_time = (EditText) view.findViewById(R.id.customer_plan_et_time);
        customer_plan_sp_quicktime = (Spinner) view.findViewById(R.id.customer_plan_sp_quicktime);
        customer_plan_btn_cancel = (Button) view.findViewById(R.id.customer_plan_btn_cancel);
        customer_plan_btn_submit = (Button) view.findViewById(R.id.customer_plan_btn_submit);

        customer_plan_ll_add = (LinearLayout) view.findViewById(R.id.customer_plan_ll_add);
        customer_plan_ll_show = (LinearLayout) view.findViewById(R.id.customer_plan_ll_show);

        customer_plan_tv_action = (TextView) view.findViewById(R.id.customer_plan_tv_action);
        customer_plan_tv_notifytime = (TextView) view.findViewById(R.id.customer_plan_tv_notifytime);
        customer_plan_cb_done = (CheckBox) view.findViewById(R.id.customer_plan_cb_done);

        initSp();
        customer_plan_et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customer_plan_sp_quicktime.setSelection(0);
                showDatePiker();
            }
        });
        customer_plan_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = customer_plan_et_content.getText().toString().trim();
                if(!"".equals(content)) {
                    String dateStr = customer_plan_et_time.getText().toString().trim();
                    if(!"".equals(dateStr)) {
                        saveNote(content, dateStr);
                    }else {
                        Toast.makeText(getActivity(), "联系时间不能为空", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getActivity(), "内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initData();
        return view;
    }

    private void initData() {
        customer = ((CustomerDetailActivity)getActivity()).getCustomer();
        if(customer.actionId != null && !"".equals(customer.actionId)) {
            showActionView(customer.action, customer.notifyTime, customer.actionId, customer._id);
        }else {
            showAddNoteView();
        }



//        if(customer != null) {
//            HttpManager.getInstance().queryCustomerInfo(user._id, customer._id, new ResponseListener() {
//                @Override
//                public void onFailed() {
//
//                }
//
//                @Override
//                public void onSuccess(String responseStr) {
//                    if(HttpParser.getSucceed(responseStr)) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(responseStr);
//                            JSONObject cust = jsonObject.getJSONObject("data");
//                            Gson gson = new Gson();
//
//                            customer = gson.fromJson(cust.toString(),
//                                    new TypeToken<MACustomer>() {
//                                    }.getType());
//                            if(customer.actionId != null && !"".equals(customer.actionId)) {
//                                showActionView(customer.action, customer.notifyTime, customer.actionId, customer._id);
//                            }else {
//                                showAddNoteView();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//        }


    }

    private void saveNote(String content, String dateStr) {
        final String customerId = ((CustomerDetailActivity)getActivity()).getCustomerId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", user._id);
        map.put("accountId", user.accountId);
        map.put("remark", content);
        map.put("notifyTime", dateStr);
        map.put("customerId",customerId);
        HttpManager.getInstance().customer_addNote(user._id, map, new ResponseListener() {
            @Override
            public void onFailed() {
                Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String responseStr) {
                System.out.println("保存联系计划返回结果:"+responseStr);
                if(HttpParser.getSucceed(responseStr)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONObject jb = jsonObject.getJSONObject("data");
                        if(jb.getString("actionId") != null) {
                            customer.actionId = jb.getString("actionId");
                            customer.notifyTime = jb.getString("notifyTime");
                            customer.action = jb.getString("action");
                            showActionView(jb.getString("action"), jb.getString("notifyTime"), jb.getString("actionId"), customerId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
    private void showAddNoteView() {
        customer_plan_ll_add.setVisibility(View.VISIBLE);
        customer_plan_ll_show.setVisibility(View.GONE);
        customer_plan_et_content.setText("");
        customer_plan_sp_quicktime.setSelection(0);
        customer_plan_et_time.setText("");

    }

    private void showActionView(String content, String notifyTime, final String actionId, final String customerId) {
        customer_plan_ll_add.setVisibility(View.GONE);
        customer_plan_ll_show.setVisibility(View.VISIBLE);
        customer_plan_tv_action.setText("");
        customer_plan_tv_notifytime.setText("");
        customer_plan_cb_done.setChecked(false);

        customer_plan_tv_action.setText(content);
        customer_plan_tv_notifytime.setText(notifyTime);

        customer_plan_cb_done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("actionId", actionId);
                    map.put("_id", customerId);
                    map.put("status", "1");
                    HttpManager.getInstance().customer_dealNote(user._id, map, new ResponseListener() {
                        @Override
                        public void onFailed() {

                        }

                        @Override
                        public void onSuccess(String responseStr) {
                            if(HttpParser.getSucceed(responseStr)) {
                                showAddNoteView();
                            }
                        }
                    });
                }
            }
        });
    }

    private void initSp() {
        List<QueryData> spDatas = new ArrayList<>();
        QueryData q1 = new QueryData();
        q1.setName("快速选择");
        q1.setValue("");
        spDatas.add(q1);
        QueryData q2 = new QueryData();
        q2.setName("半小时后");
        q2.setValue("half");
        spDatas.add(q2);
        QueryData q3 = new QueryData();
        q3.setName("一小时后");
        q3.setValue("hour");
        spDatas.add(q3);
        QueryData q4 = new QueryData();
        q4.setName("明天");
        q4.setValue("tomorrow");
        spDatas.add(q4);
        QueryData q5 = new QueryData();
        q5.setName("后天");
        q5.setValue("aftertomorrow");
        spDatas.add(q5);
        QueryData q6 = new QueryData();
        q6.setName("三天后");
        q6.setValue("week");
        spDatas.add(q6);
        QueryData q7 = new QueryData();
        q7.setName("两周后");
        q7.setValue("2week");
        spDatas.add(q7);
        QueryData q8 = new QueryData();
        q8.setName("一月后");
        q8.setValue("month");
        spDatas.add(q8);

        mSPAdapter = new SPAdapter(getActivity(), spDatas);
        customer_plan_sp_quicktime.setAdapter(mSPAdapter);
        customer_plan_sp_quicktime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                QueryData queryData = (QueryData) parent.getAdapter().getItem(position);
                if("".equals(queryData.getValue())) {
                    customer_plan_et_time.setText("");
                }else if("half".equals(queryData.getValue())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, + 30);
                    Date d = calendar.getTime();
                    String currentStr = sdf.format(d);
                    customer_plan_et_time.setText(currentStr);
                }else if("hour".equals(queryData.getValue())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, + 60);
                    Date d = calendar.getTime();
                    String currentStr = sdf.format(d);
                    customer_plan_et_time.setText(currentStr);
                }else if("tomorrow".equals(queryData.getValue())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_WEEK, + 1);
                    Date d = calendar.getTime();
                    String currentStr = defaultsdf.format(d);
                    customer_plan_et_time.setText(currentStr+" 09:00");
                }else if("aftertomorrow".equals(queryData.getValue())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_WEEK, + 2);
                    Date d = calendar.getTime();
                    String currentStr = defaultsdf.format(d);
                    customer_plan_et_time.setText(currentStr+" 09:00");
                }else if("week".equals(queryData.getValue())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_WEEK, + 3);
                    Date d = calendar.getTime();
                    String currentStr = defaultsdf.format(d);
                    customer_plan_et_time.setText(currentStr+" 09:00");
                }else if("2week".equals(queryData.getValue())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.WEEK_OF_MONTH, + 2);
                    Date d = calendar.getTime();
                    String currentStr = defaultsdf.format(d);
                    customer_plan_et_time.setText(currentStr+" 09:00");
                }else if("month".equals(queryData.getValue())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, + 1);
                    Date d = calendar.getTime();
                    String currentStr = defaultsdf.format(d);
                    customer_plan_et_time.setText(currentStr+" 09:00");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showDatePiker() {
        Calendar d = Calendar.getInstance(Locale.CHINA);
        //创建一个日历引用d，通过静态方法getInstance() 从指定时区 Locale.CHINA 获得一个日期实例
        Date myDate = new Date();
        //创建一个Date实例
        d.setTime(myDate);
        //设置日历的时间，把一个新建Date实例myDate传入
        int year = d.get(Calendar.YEAR);
        int month = d.get(Calendar.MONTH);
        int day = d.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
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
                TimePickerDialog tpd = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
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
                        customer_plan_et_time.setText(result);
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
}
