package com.moor.im.options.setup.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.moor.im.R;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.event.UserInfoUpdate;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.base.BaseActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by longwei on 2015/8/27.
 */
public class EditActivity extends BaseActivity{

    private TextInputLayout group_update_title_til_name;
    String type = "";
    User user = UserDao.getInstance().getUser();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_update_title);
        group_update_title_til_name = (TextInputLayout) findViewById(R.id.group_update_title_til_name);
        Intent intent = getIntent();
        type = intent.getStringExtra("edittype");
        if(type == null) {
            type = "";
        }
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageButton titlebar_done = (ImageButton) findViewById(R.id.titlebar_done);
        titlebar_done.setVisibility(View.VISIBLE);

        if(type.equals("name")) {
            titlebar_name.setText("修改名字");
            group_update_title_til_name.getEditText().setText(NullUtil.checkNull(user.displayName));
            titlebar_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = group_update_title_til_name.getEditText().getText().toString().trim();
                    if("".equals(name)) {
                        Toast.makeText(EditActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                    }else {
                        showLoadingDialog();
                        HttpManager.getInstance().editUserInfo(InfoDao.getInstance().getConnectionId(),
                                user._id, name, user.email, user.mobile,
                                user.product, new EditUserResponseHandler());
                    }
                }
            });
        }else if(type.equals("phone")) {
            titlebar_name.setText("修改手机");
            group_update_title_til_name.getEditText().setText(NullUtil.checkNull(user.mobile));
            group_update_title_til_name.getEditText().setInputType(EditorInfo.TYPE_CLASS_PHONE);
            titlebar_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String phone = group_update_title_til_name.getEditText().getText().toString().trim();
                    if("".equals(phone)) {
                        Toast.makeText(EditActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                    }else {
                        showLoadingDialog();
                        HttpManager.getInstance().editUserInfo(InfoDao.getInstance().getConnectionId(),
                                user._id, user.displayName, user.email, phone,
                                user.product, new EditUserResponseHandler());
                    }
                }
            });
        }else if(type.equals("email")) {
            titlebar_name.setText("修改邮箱");
            group_update_title_til_name.getEditText().setText(NullUtil.checkNull(user.email));
            group_update_title_til_name.getEditText().setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            titlebar_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = group_update_title_til_name.getEditText().getText().toString().trim();
                    if("".equals(email)) {
                        Toast.makeText(EditActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                    }else {
                        showLoadingDialog();
                        HttpManager.getInstance().editUserInfo(InfoDao.getInstance().getConnectionId(),
                                user._id, user.displayName, email, user.mobile,
                                user.product, new EditUserResponseHandler());
                    }
                }
            });
        }
    }

    class EditUserResponseHandler implements ResponseListener {
        @Override
        public void onFailed() {
            dismissLoadingDialog();
            Toast.makeText(EditActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String responseString) {
            String message = HttpParser.getMessage(responseString);
            if (HttpParser.getSucceed(responseString)) {
                dismissLoadingDialog();
                RxBus.getInstance().send(new UserInfoUpdate());
                Toast.makeText(EditActivity.this, "信息修改成功", Toast.LENGTH_SHORT)
                        .show();
                finish();
            } else {
                if ("408".equals(message)) {
                    JSONObject o;
                    try {
                        o = new JSONObject(responseString);
                        JSONArray ja = o.getJSONArray("RepeatList");
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < ja.length(); i++) {
                            if("email".equals(ja.get(i))) {
                                sb.append("您的邮箱与别人重复");
                            }
                            if("mobile".equals(ja.get(i))) {
                                sb.append("您的电话与别人重复");
                            }
                            if("name".equals(ja.get(i))) {
                                sb.append("您的姓名与别人重复");
                            }
                        }
                        Toast.makeText(EditActivity.this, sb.toString(), Toast.LENGTH_SHORT)
                                .show();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }else {
                    Toast.makeText(EditActivity.this, "网络不稳定，请稍后重试", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        }
    }

}
