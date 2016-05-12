package com.moor.im.options.mobileassistant.erp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.mobileassistant.erp.event.ErpExcuteSuccess;
import com.moor.im.options.mobileassistant.model.MAErpDetail;

/**
 * Created by longwei on 2016/3/29.
 */
public class ErpActionBackActivity extends BaseActivity{

    private EditText erp_action_back_et;
    private Button erp_action_back_btn;

    User user = UserDao.getInstance().getUser();
    private MAErpDetail business;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erp_action_back);

        Intent intent = getIntent();
        business = (MAErpDetail) intent.getSerializableExtra("business");

        erp_action_back_et = (EditText) findViewById(R.id.erp_action_back_et);
        erp_action_back_btn = (Button) findViewById(R.id.erp_action_back_btn);
        erp_action_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = erp_action_back_et.getText().toString().trim();
                if(!"".equals(content)) {
                    showLoadingDialog();
                    HttpManager.getInstance().excuteBusinessBackAction(user._id, business._id, content, new ExcuteBusBackHandler());
                }else {
                    Toast.makeText(ErpActionBackActivity.this, "请填写原因", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class ExcuteBusBackHandler implements ResponseListener{

        @Override
        public void onFailed() {
            dismissLoadingDialog();
            Toast.makeText(ErpActionBackActivity.this, "退回失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String s) {
            dismissLoadingDialog();
            if (HttpParser.getSucceed(s)) {
                Toast.makeText(ErpActionBackActivity.this, "退回成功", Toast.LENGTH_SHORT).show();
                RxBus.getInstance().send(new ErpExcuteSuccess());
                finish();
            }else if("403".equals(HttpParser.getErrorCode(s))) {
                Toast.makeText(ErpActionBackActivity.this, "该工单步骤已被执行或您已无权限执行", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(ErpActionBackActivity.this, "退回失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
