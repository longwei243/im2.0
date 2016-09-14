package com.moor.im.options.mobileassistant.customer.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.views.GridViewInScrollView;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.cdr.adapter.SPAdapter;
import com.moor.im.options.mobileassistant.customer.adapter.CustomerCBAdapter;
import com.moor.im.options.mobileassistant.erp.activity.ErpActionProcessActivity;
import com.moor.im.options.mobileassistant.erp.adapter.ErpAgentSpAdapter;
import com.moor.im.options.mobileassistant.erp.adapter.ErpCBAdapter;
import com.moor.im.options.mobileassistant.erp.adapter.ErpSpAdapter;
import com.moor.im.options.mobileassistant.erp.dialog.UploadFileDialog;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MABusinessField;
import com.moor.im.options.mobileassistant.model.MACustomer;
import com.moor.im.options.mobileassistant.model.MAOption;
import com.moor.im.options.mobileassistant.model.Option;
import com.moor.im.options.mobileassistant.model.QueryData;
import com.moor.imkf.gson.Gson;
import com.moor.imkf.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by longwei on 16/9/12.
 */
public class CustomerEditActivity extends BaseActivity{

    private LinearLayout customer_edit_ll_stable_field, customer_edit_ll_custom_field, customer_edit_ll_file;
    private ImageButton titlebar_done;
    private String customerId;
    private String dbType;

    private String custCacheStr;
    private User user = UserDao.getInstance().getUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_edit);

        customerId = getIntent().getStringExtra("customerId");

        customer_edit_ll_stable_field = (LinearLayout) findViewById(R.id.customer_edit_ll_stable_field);
        customer_edit_ll_custom_field = (LinearLayout) findViewById(R.id.customer_edit_ll_custom_field);
        customer_edit_ll_file = (LinearLayout) findViewById(R.id.customer_edit_ll_file);
        titlebar_done = (ImageButton) findViewById(R.id.titlebar_done);

        titlebar_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomerInfo();
            }
        });

        HttpManager.getInstance().queryCustomerInfo(user._id, customerId, new ResponseListener() {
            @Override
            public void onFailed() {

            }

            @Override
            public void onSuccess(String responseStr) {
                System.out.println("customer edit获取详情数据:"+responseStr);
                initData(responseStr);
            }
        });
    }

    private void initData(String responseStr) {


        if (MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust) != null) {
            custCacheStr = MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust);

            try {
                JSONObject jsonObject1 = new JSONObject(responseStr);
                if ("true".equals(jsonObject1.getString("Succeed"))) {
                    JSONObject cust = jsonObject1.getJSONObject("data");
                    dbType = cust.getString("dbType");

                    JSONObject custCache = new JSONObject(custCacheStr);

                    createStableFieldView(custCache, cust);

                    createSourceView(custCache, cust);

                    createOwnerView(custCache, cust);

                    createFileView(custCache, cust);

                    createCustomFieldView(custCache, cust);

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveCustomerInfo() {
        HashMap<String, String> datas = new HashMap<>();
        HashMap<String, JSONArray> jadatas = new HashMap<>();

        //固定字段
        int stable_child_count = customer_edit_ll_stable_field.getChildCount();
        for(int i=0; i<stable_child_count; i++) {
            LinearLayout childView = (LinearLayout) customer_edit_ll_stable_field.getChildAt(i);
            String type = (String) childView.getTag();
            switch (type) {
                case "name":
                    EditText et = (EditText) childView.getChildAt(1);
                    String id = (String) et.getTag();
                    String value = et.getText().toString().trim();
                    TextView tv_single_required = (TextView) childView.getChildAt(0);
                    String fieldName = tv_single_required.getText().toString();
                    String required = (String) tv_single_required.getTag();
                    if("required".equals(required)) {
                        if("".equals(value)) {
                            Toast.makeText(CustomerEditActivity.this, fieldName + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id, value);
                    System.out.println("id is:" + id + "," + "value is:" + value);
                    break;
                case "single":
                    EditText et_single = (EditText) childView.getChildAt(1);
                    String id_single = (String) et_single.getTag();
                    String value_single = et_single.getText().toString().trim();
                    TextView tv_single_required_single = (TextView) childView.getChildAt(0);
                    String fieldName_single = tv_single_required_single.getText().toString();
                    String required_single = (String) tv_single_required_single.getTag();
                    if("required".equals(required_single)) {
                        if("".equals(value_single)) {
                            Toast.makeText(CustomerEditActivity.this, fieldName_single + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id_single, value_single);
                    System.out.println("single id is:" + id_single + "," + "value is:" + value_single);
                    break;
                case "mutli":
                    EditText et_mutli = (EditText) childView.getChildAt(1);
                    String id_mutli = (String) et_mutli.getTag();
                    String value_mutli = et_mutli.getText().toString().trim();
                    TextView tv_required_mutli = (TextView) childView.getChildAt(0);
                    String fieldName_mutli = tv_required_mutli.getText().toString();
                    String required_mutli = (String) tv_required_mutli.getTag();
                    if("required".equals(required_mutli)) {
                        if("".equals(value_mutli)) {
                            Toast.makeText(CustomerEditActivity.this, fieldName_mutli + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id_mutli, value_mutli);
                    System.out.println("mutli id is:" + id_mutli + "," + "value is:" + value_mutli);
                    break;
                case "sex":

                    RadioGroup radioGroup = (RadioGroup) childView.getChildAt(1);
                    String id_sex = (String) radioGroup.getTag();
                    int selectId = radioGroup.getCheckedRadioButtonId();

                    TextView tv_required_sex = (TextView) childView.getChildAt(0);
                    String fieldName_sex = tv_required_sex.getText().toString();
                    String required_sex = (String) tv_required_sex.getTag();
                    if("required".equals(required_sex)) {
                        if(selectId == -1) {
                            Toast.makeText(CustomerEditActivity.this, fieldName_sex + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if(selectId != -1) {
                        RadioButton rb = (RadioButton) radioGroup.findViewById(selectId);
                        String value_sex = (String) rb.getTag();
                        datas.put(id_sex, value_sex);
                        System.out.println("sex id is:" + id_sex + "," + "value is:" + value_sex);
                    }

                    break;
                case "birth":

                    EditText et_birth = (EditText) childView.getChildAt(1);
                    String id_birth = (String) et_birth.getTag();
                    String value_birth = et_birth.getText().toString().trim();
                    TextView tv_required_birth = (TextView) childView.getChildAt(0);
                    String fieldName_birth = tv_required_birth.getText().toString();
                    String required_birth = (String) tv_required_birth.getTag();
                    if("required".equals(required_birth)) {
                        if("".equals(value_birth)) {
                            Toast.makeText(CustomerEditActivity.this, fieldName_birth + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id_birth, value_birth);
                    System.out.println("birth id is:" + id_birth + "," + "value is:" + value_birth);
                    break;
                case "province":

                    Spinner sp1 = (Spinner) ((RelativeLayout)(childView.getChildAt(0))).getChildAt(1);
                    String id_dropdown1 = (String) sp1.getTag();
                    String value_dropdown1 = ((Option)sp1.getSelectedItem()).key;
                    datas.put(id_dropdown1, value_dropdown1);

                    System.out.println("id_dropdown1 is:"+id_dropdown1+",value_dropdown1:"+value_dropdown1);

                    Spinner sp2 = (Spinner) ((RelativeLayout)(childView.getChildAt(1))).getChildAt(1);
                    String id_dropdown2 = (String) sp2.getTag();
                    String value_dropdown2 = ((Option)sp2.getSelectedItem()).key;
                    datas.put(id_dropdown2, value_dropdown2);
                    System.out.println("id_dropdown2 is:"+id_dropdown2+",value_dropdown2:"+value_dropdown2);

                    break;
                case "status":

                    Spinner sp_status = (Spinner) childView.getChildAt(1);
                    String id_status = (String) sp_status.getTag();
                    String value_status = ((QueryData)sp_status.getSelectedItem()).getValue();
                    datas.put(id_status, value_status);
                    System.out.println("status id is:" + id_status + "," + "value is:" + value_status);

                    break;
                case "source":
                    Spinner sp_source = (Spinner) childView.getChildAt(1);
                    String id_source = (String) sp_source.getTag();
                    String value_source = ((QueryData)sp_source.getSelectedItem()).getValue();
                    datas.put(id_source, value_source);
                    System.out.println("source id is:" + id_source + "," + "value is:" + value_source);

                    break;
                case "owner":
                    Spinner sp_owner = (Spinner) childView.getChildAt(1);
                    String id_owner = (String) sp_owner.getTag();
                    String value_owner = ((MAAgent)sp_owner.getSelectedItem())._id;
                    datas.put(id_owner, value_owner);
                    System.out.println("owner id is:" + id_owner + "," + "value is:" + value_owner);

                    break;
                case "phone":
                    JSONArray jsonArray = new JSONArray();
                    LinearLayout phonell = (LinearLayout) childView.getChildAt(1);
                    int phoneItemCount = phonell.getChildCount();
                    for(int p=0; p<phoneItemCount; p++) {
                        RelativeLayout phoneItem = (RelativeLayout) phonell.getChildAt(p);
                        EditText et_num = (EditText) phoneItem.getChildAt(0);
                        String phone_num = et_num.getText().toString().trim();
                        EditText et_memo = (EditText) phoneItem.getChildAt(2);
                        String phone_memo = et_memo.getText().toString().trim();
                        System.out.println("phone num is:"+phone_num+",memo is:"+phone_memo);


                        JSONObject ja = new JSONObject();
                        try {
                            ja.put("tel", phone_num);
                            ja.put("memo", phone_memo);
                            jsonArray.put(ja);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    jadatas.put("phone", jsonArray);
                    break;
                case "weixin":


                    break;
                case "email":


                    break;
                default:
                    break;
            }
        }

        //附件
        RelativeLayout ll_file = (RelativeLayout) customer_edit_ll_file.getChildAt(0);
        LinearLayout filell = (LinearLayout) ll_file.getChildAt(1);
        int fileCount = filell.getChildCount();
        JSONArray ja = new JSONArray();
        for(int f=0; f<fileCount; f++) {
            JSONObject jb = new JSONObject();
            RelativeLayout rl = (RelativeLayout) filell.getChildAt(f);
            String filetype = (String) rl.getTag();
            TextView fileNameTv = (TextView) rl.getChildAt(1);
            String fileName = fileNameTv.getText().toString().trim();
            String fileKey = (String) fileNameTv.getTag();
            try {
                jb.put("id", fileKey);
                jb.put("name", fileName);
                jb.put("type", filetype);

                ja.put(jb);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("文件type is:"+filetype+",name is:"+fileName+",key is:"+fileKey);
        }
        jadatas.put("attachs", ja);

        //自定义字段
        final int custom_child_count = customer_edit_ll_custom_field.getChildCount();
        for(int c=0; c<custom_child_count; c++) {
            LinearLayout childView = (LinearLayout) customer_edit_ll_custom_field.getChildAt(c);
            String type = (String) childView.getTag();
            switch (type) {
                case "single":
                    EditText et_single = (EditText) childView.getChildAt(1);
                    String id_single = (String) et_single.getTag();
                    String value_single = et_single.getText().toString().trim();
                    TextView tv_single_required_single = (TextView) childView.getChildAt(0);
                    String fieldName_single = tv_single_required_single.getText().toString();
                    String required_single = (String) tv_single_required_single.getTag();
                    if("required".equals(required_single)) {
                        if("".equals(value_single)) {
                            Toast.makeText(CustomerEditActivity.this, fieldName_single + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    datas.put(id_single, value_single);
                    System.out.println("custom single id is:" + id_single + "," + "value is:" + value_single);

                    break;
                case "dropdown":
                    Spinner sp_status = (Spinner) childView.getChildAt(1);
                    String id_status = (String) sp_status.getTag();
                    String value_status = ((QueryData)sp_status.getSelectedItem()).getValue();
                    datas.put(id_status, value_status);
                    System.out.println("custom dropdown id is:" + id_status + "," + "value is:" + value_status);
                    break;
                case "checkbox":
                    JSONArray jsonArray = new JSONArray();
                    GridViewInScrollView gv = (GridViewInScrollView) childView.getChildAt(1);
                    String cbFieldId = (String) gv.getTag();
                    List<QueryData> options = ((CustomerCBAdapter)gv.getAdapter()).getOptions();
                    HashMap<Integer, Boolean> selected = ((CustomerCBAdapter)gv.getAdapter()).getIsSelected();
                    TextView tv_checkbox_required = (TextView) childView.getChildAt(0);
                    String fieldName_checkbox = tv_checkbox_required.getText().toString();
                    String required_checkbox = (String) tv_checkbox_required.getTag();
                    for (int o = 0; o < selected.size(); o++) {
                        if(selected.get(o)) {
                            QueryData option = options.get(o);
                            jsonArray.put(option.getValue());
                            System.out.println("checkbox name is:"+option.getName());
                        }
                    }
                    if("required".equals(required_checkbox)) {
                        if(jsonArray.length() == 0) {
                            Toast.makeText(CustomerEditActivity.this, fieldName_checkbox + "是必选项", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    jadatas.put(cbFieldId, jsonArray);

                    break;
                case "radio":
                    RadioGroup radioGroup = (RadioGroup) childView.getChildAt(1);
                    String id_sex = (String) radioGroup.getTag();
                    int selectId = radioGroup.getCheckedRadioButtonId();

                    TextView tv_required_sex = (TextView) childView.getChildAt(0);
                    String fieldName_sex = tv_required_sex.getText().toString();
                    String required_sex = (String) tv_required_sex.getTag();
                    if("required".equals(required_sex)) {
                        if(selectId == -1) {
                            Toast.makeText(CustomerEditActivity.this, fieldName_sex + "是必填项", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if(selectId != -1) {
                        RadioButton rb = (RadioButton) radioGroup.findViewById(selectId);
                        String value_sex = (String) rb.getTag();
                        datas.put(id_sex, value_sex);
                        System.out.println("custom radio id is:" + id_sex + "," + "value is:" + value_sex);
                    }
                    break;
                default:
                    break;
            }
        }
        if(!"".equals(customerId) && !"".equals(dbType)) {
            datas.put("_id", customerId);
            datas.put("dbType", dbType);

            HttpManager.getInstance().customer_update(user._id, datas, jadatas, new ResponseListener() {
                @Override
                public void onFailed() {
                    System.out.println("更新客户失败");
                }

                @Override
                public void onSuccess(String responseStr) {
                    System.out.println("更新客户返回结果:"+responseStr);
                    if(HttpParser.getSucceed(responseStr)) {
                        try{
                            JSONObject jsonObject = new JSONObject(responseStr);
                            JSONObject cust = jsonObject.getJSONObject("data");
                            Gson gson = new Gson();
                            MACustomer customer = gson.fromJson(cust.toString(),
                                    new TypeToken<MACustomer>() {
                                    }.getType());
                            RxBus.getInstance().send(customer);
                            finish();
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

    }

    LinearLayout erp_field_file_ll_already;
    /**
     * 上传文件界面
     */
    private void createFileView(JSONObject custCache, JSONObject cust) {
        RelativeLayout fileView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_file, null);
        fileView.setTag("file");

        TextView erp_field_file_tv_name = (TextView) fileView.findViewById(R.id.erp_field_file_tv_name);
        erp_field_file_tv_name.setText("附件");
        erp_field_file_ll_already = (LinearLayout) fileView.findViewById(R.id.erp_field_file_ll_already);
        Button erp_field_file_btn_uploadfile = (Button) fileView.findViewById(R.id.erp_field_file_btn_uploadfile);
        erp_field_file_btn_uploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//设置类型
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }else {
                    intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
                startActivityForResult(intent, 0x1111);
            }
        });

        customer_edit_ll_file.addView(fileView);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1111 && resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String path = "";
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = { "_data" };
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(uri, projection,null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        path =  cursor.getString(column_index);
                    }
                } catch (Exception e) {

                }
            }
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                path =  uri.getPath();
            }

            File file = new File(path);
            String fileSizeStr = "";
            if(file.exists()) {
                long fileSize = file.length();
                System.out.println("文件大小为:" + fileSize);
                if((fileSize / 1024 / 1024) > 10.0) {
                    //大于10M不能上传
                    Toast.makeText(CustomerEditActivity.this, "上传文件不能大于10M", Toast.LENGTH_SHORT).show();
                }else {
                    if(fileSize / 1024 < 1.0) {
                        //B
                        fileSizeStr = (int)(fileSize/1024) + "B";
                    }else if(fileSize / 1024 > 1.0 && fileSize / 1024 / 1024 < 1.0) {
                        //KB
                        fileSizeStr = (int)(fileSize/1024) + "KB";
                    }else if(fileSize / 1024 / 1024 < 10.0) {
                        //MB
                        fileSizeStr = (int)(fileSize/1024/1024) + "MB";
                    }

                    String fileName = path.substring(path.lastIndexOf("/") + 1);
                    System.out.println("filename is:"+fileName);
                    UploadFileDialog fileDialog = new UploadFileDialog();
                    Bundle b = new Bundle();
                    b.putString("fileName", fileName);
                    b.putString("fileSize", fileSizeStr);
                    b.putSerializable("file", file);
                    fileDialog.setArguments(b);
                    fileDialog.setOnFileUploadCompletedListener(fileUploadCompletedListener);
                    fileDialog.show(getFragmentManager(), "");
                }
            }
        }
    }

    ErpActionProcessActivity.OnFileUploadCompletedListener fileUploadCompletedListener = new ErpActionProcessActivity.OnFileUploadCompletedListener() {

        @Override
        public void onFailed() {

        }

        @Override
        public void onCompleted(String fileName, String key) {
            final RelativeLayout rl = (RelativeLayout) LayoutInflater.from(CustomerEditActivity.this).inflate(R.layout.erp_field_file_already, null);
            String type = "other";
            if(fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("png") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("jpg") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("jpeg") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("gif") ||
                    fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("bmp")) {
                type = "img";
            }
            rl.setTag(type);
            TextView erp_field_file_upload_already_tv_filename = (TextView) rl.findViewById(R.id.erp_field_file_upload_already_tv_filename);
            erp_field_file_upload_already_tv_filename.setText(fileName);
            erp_field_file_upload_already_tv_filename.setTag(key);

            ImageView erp_field_file_upload_already_btn_delete = (ImageView) rl.findViewById(R.id.erp_field_file_upload_already_btn_delete);
            erp_field_file_upload_already_btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //删除附件
                    erp_field_file_ll_already.removeView(rl);
                }
            });

            erp_field_file_ll_already.addView(rl);
        }
    };

    private void createCustomFieldView(JSONObject custCache, JSONObject cust) {

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
                    customer_edit_ll_custom_field.addView(singleView);
                }else if("multi".equals(cf.getString("type"))) {
                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_mutli, null);
                    singleView.setTag("single");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(cf.getString("name"));
                    erp_field_single_tv_name.setTag(cf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(cf.getString("_id"));
                    customer_edit_ll_custom_field.addView(singleView);
                }else if("number".equals(cf.getString("type"))) {
                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_single, null);
                    singleView.setTag("single");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(cf.getString("name"));
                    erp_field_single_tv_name.setTag(cf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(cf.getString("_id"));
                    customer_edit_ll_custom_field.addView(singleView);
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

                    customer_edit_ll_custom_field.addView(birthView);
                }else if("dropdown".equals(cf.getString("type"))) {
                    LinearLayout firstItemRL = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_dropdown, null);
                    firstItemRL.setTag("dropdown");
                    TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_dropdown_item_tv_name.setText(cf.getString("name"));
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
                    Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.customer_edit_field_sp);
                    erp_field_dropdown_item_sp_value.setTag(cf.getString("_id"));
                    SPAdapter adapter = new SPAdapter(CustomerEditActivity.this, datas);
                    erp_field_dropdown_item_sp_value.setAdapter(adapter);
                    customer_edit_ll_custom_field.addView(firstItemRL);

                }else if("checkbox".equals(cf.getString("type"))) {
                    initCheckBoxView(cf, cust);
                }else if("radio".equals(cf.getString("type"))) {
                    initRadioView(cf, cust);
                }

            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void createOwnerView(JSONObject custCache, JSONObject cust) {

        List<MAAgent> agents = MobileAssitantCache.getInstance().getAgents();
        if(agents != null && agents.size() > 0) {

            LinearLayout firstItemRL = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_dropdown, null);
            firstItemRL.setTag("owner");
            TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.ecustomer_edit_field_tv_name);
            erp_field_dropdown_item_tv_name.setText("归属");

            Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.customer_edit_field_sp);
            erp_field_dropdown_item_sp_value.setTag("owner");
            erp_field_dropdown_item_sp_value.setAdapter(new ErpAgentSpAdapter(CustomerEditActivity.this, agents));
            customer_edit_ll_stable_field.addView(firstItemRL);
        }
    }

    private void createSourceView(JSONObject custCache, JSONObject cust) {
        try{
            LinearLayout firstItemRL = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_dropdown, null);
            firstItemRL.setTag("source");
            TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.ecustomer_edit_field_tv_name);
            erp_field_dropdown_item_tv_name.setText("数据来源");
            List<QueryData> sourceDatas = new ArrayList<>();
            JSONArray sourceArray = custCache.getJSONArray("source");
            for(int i=0; i<sourceArray.length(); i++) {
                JSONObject source = sourceArray.getJSONObject(i);
                QueryData queryData = new QueryData();
                queryData.setName(source.getString("name"));
                queryData.setValue(source.getString("key"));
                sourceDatas.add(queryData);
            }
            Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.customer_edit_field_sp);
            erp_field_dropdown_item_sp_value.setTag("custsource1");
            SPAdapter adapter = new SPAdapter(CustomerEditActivity.this, sourceDatas);
            erp_field_dropdown_item_sp_value.setAdapter(adapter);
            customer_edit_ll_stable_field.addView(firstItemRL);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createStableFieldView(JSONObject custCache, JSONObject cust) {
        try {
            JSONArray stable_fields = custCache.getJSONArray("stable_fields");
            for(int i=0; i<stable_fields.length(); i++) {
                JSONObject sf = stable_fields.getJSONObject(i);
                if("name".equals(sf.getString("name"))) {
                    //客户名称
                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_single, null);
                    singleView.setTag("name");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(sf.getString("value"));
                    erp_field_single_tv_name.setTag(sf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(sf.getString("name"));
                    customer_edit_ll_stable_field.addView(singleView);

                    //客户状态
                    LinearLayout firstItemRL = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_dropdown, null);
                    firstItemRL.setTag("status");
                    TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_dropdown_item_tv_name.setText("客户状态");
                    List<QueryData> statusList = new ArrayList<>();
                    JSONObject status = custCache.getJSONObject("status");
                    Iterator<String> iterator = status.keys();
                    while (iterator.hasNext()) {
                        QueryData queryData = new QueryData();
                        String key = iterator.next();
                        queryData.setName(status.getString(key));
                        queryData.setValue(key);
                        statusList.add(queryData);
                    }
                    Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.customer_edit_field_sp);
                    erp_field_dropdown_item_sp_value.setTag("status");
                    SPAdapter adapter = new SPAdapter(CustomerEditActivity.this, statusList);
                    erp_field_dropdown_item_sp_value.setAdapter(adapter);
                    customer_edit_ll_stable_field.addView(firstItemRL);
                }else if("phone".equals(sf.getString("name"))) {
                    //客户电话
                    LinearLayout phone_field = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_phone, null);
                    phone_field.setTag("phone");
                    TextView erp_field_phone_item_tv_name = (TextView) phone_field.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_phone_item_tv_name.setText(sf.getString("value"));

                    final LinearLayout phone_ll = (LinearLayout) phone_field.findViewById(R.id.ecustomer_edit_field_phone_ll);
                    RelativeLayout phone_item = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_phone_item, null);
                    EditText ecustomer_edit_field_phone_item_et_number = (EditText) phone_item.findViewById(R.id.ecustomer_edit_field_phone_item_et_number);
                    EditText ecustomer_edit_field_phone_item_et_memo = (EditText) phone_item.findViewById(R.id.ecustomer_edit_field_phone_item_et_memo);
                    ImageView ecustomer_edit_field_phone_item_iv_add = (ImageView) phone_item.findViewById(R.id.ecustomer_edit_field_phone_item_iv_add);

                    ecustomer_edit_field_phone_item_iv_add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final RelativeLayout phone_item_reduce = (RelativeLayout) LayoutInflater.from(CustomerEditActivity.this).inflate(R.layout.customer_edit_field_phone_item, null);
                            EditText ecustomer_edit_field_phone_item_et_number = (EditText) phone_item_reduce.findViewById(R.id.ecustomer_edit_field_phone_item_et_number);
                            EditText ecustomer_edit_field_phone_item_et_memo = (EditText) phone_item_reduce.findViewById(R.id.ecustomer_edit_field_phone_item_et_memo);
                            ImageView ecustomer_edit_field_phone_item_iv_add = (ImageView) phone_item_reduce.findViewById(R.id.ecustomer_edit_field_phone_item_iv_add);
                            ecustomer_edit_field_phone_item_iv_add.setImageResource(R.drawable.customer_field_reduce);
                            ecustomer_edit_field_phone_item_iv_add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    phone_ll.removeView(phone_item_reduce);
                                }
                            });
                            phone_ll.addView(phone_item_reduce);
                        }
                    });
                    phone_ll.addView(phone_item);

                    customer_edit_ll_stable_field.addView(phone_field);
                }else if("email".equals(sf.getString("name"))) {

                }else if("weixin".equals(sf.getString("name"))) {

                }else if("province".equals(sf.getString("name"))) {
                    LinearLayout dropDownView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_filed_province, null);
                    dropDownView.setTag("province");
                    LinearLayout erp_field_dropdown_ll = (LinearLayout) dropDownView.findViewById(R.id.customer_edit_field_province_ll);
                    MAOption maoption = MobileAssitantCache.getInstance().getMAOption("d7b9c68a-b50f-21d1-d5fd-41ea93f5f49c");

                    final List<Option> firstOption = maoption.options;
                    RelativeLayout firstItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item1, null);
                    TextView erp_field_dropdown_item_tv_name = (TextView) firstItemRL.findViewById(R.id.erp_field_dropdown_item_tv_name);
                    erp_field_dropdown_item_tv_name.setText("省");

                    final Spinner erp_field_dropdown_item_sp_value = (Spinner) firstItemRL.findViewById(R.id.erp_field_dropdown_item_sp_value);
                    erp_field_dropdown_item_sp_value.setTag("province");
                    final ErpSpAdapter adapter = new ErpSpAdapter(CustomerEditActivity.this, firstOption);
                    erp_field_dropdown_item_sp_value.setAdapter(adapter);
                    erp_field_dropdown_ll.addView(firstItemRL);
                    String fieldName2 = maoption.headers.get(1);
                    RelativeLayout secondItemRL = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.erp_field_dropdown_item2, null);
                    TextView erp_field_dropdown_item_tv_name2 = (TextView) secondItemRL.findViewById(R.id.erp_field_dropdown_item2_tv_name);
                    erp_field_dropdown_item_tv_name2.setText(fieldName2);

                    final Spinner erp_field_dropdown_item_sp_value2 = (Spinner) secondItemRL.findViewById(R.id.erp_field_dropdown_item2_sp_value);
                    erp_field_dropdown_item_sp_value2.setTag("city");
                    erp_field_dropdown_ll.addView(secondItemRL);

                    erp_field_dropdown_item_sp_value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Option o = (Option) parent.getAdapter().getItem(position);
                            List<Option> secondOptions = getOptionsByKey(firstOption, o.key);
                            ErpSpAdapter adapter = new ErpSpAdapter(CustomerEditActivity.this, secondOptions);
                            erp_field_dropdown_item_sp_value2.setAdapter(adapter);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    customer_edit_ll_stable_field.addView(dropDownView);

                }else if("city".equals(sf.getString("name"))) {

                }else if("note".equals(sf.getString("name")) || "address".equals(sf.getString("name"))) {
                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_mutli, null);
                    singleView.setTag("mutli");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(sf.getString("value"));
                    erp_field_single_tv_name.setTag(sf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(sf.getString("name"));
                    customer_edit_ll_stable_field.addView(singleView);

                }else if("sex".equals(sf.getString("name"))) {
                    LinearLayout sexView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_sex, null);
                    sexView.setTag("sex");
                    TextView erp_field_single_tv_name = (TextView) sexView.findViewById(R.id.customer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(sf.getString("value"));
                    erp_field_single_tv_name.setTag(sf.getString("required"));

                    RadioGroup customer_edit_field_rg_sex = (RadioGroup) sexView.findViewById(R.id.customer_edit_field_rg_sex);
                    customer_edit_field_rg_sex.setTag(sf.getString("name"));
                    RadioButton customer_edit_field_rb_nan = (RadioButton) sexView.findViewById(R.id.customer_edit_field_rb_nan);
                    customer_edit_field_rb_nan.setTag("0");
                    RadioButton customer_edit_field_rb_nv = (RadioButton) sexView.findViewById(R.id.customer_edit_field_rb_nv);
                    customer_edit_field_rb_nv.setTag("1");

                    customer_edit_ll_stable_field.addView(sexView);


                }else if("birth".equals(sf.getString("name"))) {

                    LinearLayout birthView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_birth, null);
                    birthView.setTag("birth");
                    TextView erp_field_single_tv_name = (TextView) birthView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(sf.getString("value"));
                    erp_field_single_tv_name.setTag(sf.getString("required"));
                    final EditText erp_field_single_et_value = (EditText) birthView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(sf.getString("name"));
                    erp_field_single_et_value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDatePiker(erp_field_single_et_value);
                        }
                    });

                    customer_edit_ll_stable_field.addView(birthView);
                }else {

                    LinearLayout singleView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.customer_edit_field_single, null);
                    singleView.setTag("single");
                    TextView erp_field_single_tv_name = (TextView) singleView.findViewById(R.id.ecustomer_edit_field_tv_name);
                    erp_field_single_tv_name.setText(sf.getString("value"));
                    erp_field_single_tv_name.setTag(sf.getString("required"));
                    EditText erp_field_single_et_value = (EditText) singleView.findViewById(R.id.ecustomer_edit_field_et_value);
                    erp_field_single_et_value.setTag(sf.getString("name"));
                    customer_edit_ll_stable_field.addView(singleView);

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private List<Option> getOptionsByKey(List<Option> o, String key) {
        for(int i=0; i<o.size(); i++) {
            if(key.equals(o.get(i).key)) {
                return o.get(i).options;
            }
        }
        return null;
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
        DatePickerDialog dpd = new DatePickerDialog(CustomerEditActivity.this, new DatePickerDialog.OnDateSetListener() {
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


    private void initCheckBoxView(JSONObject cf, JSONObject cust) {
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
            final CustomerCBAdapter adapter = new CustomerCBAdapter(CustomerEditActivity.this, datas);
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

            customer_edit_ll_custom_field.addView(checkboxView);

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRadioView(JSONObject cf, JSONObject cust) {
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
                RadioButton rb = new RadioButton(CustomerEditActivity.this);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rb.setLayoutParams(lp);
                rb.setText(qd.getName());
                rb.setTag(qd.getValue());
                radioGroup.addView(rb);
            }
            customer_edit_ll_custom_field.addView(radioView);
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
