package com.moor.im.options.dial.dialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipProfile;
import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.Utils;
import com.moor.im.options.dial.CallingActivity;

/**
 * Created by longwei on 2016/5/23.
 */
public class CallChoiseDialog extends Activity implements View.OnClickListener{

    private ImageButton call_choise_btn_sip,call_choise_btn_back,call_choise_btn_phone,call_choise_btn_cancel;
    private String phone = "";

    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_choise);

        Intent intent = getIntent();
        phone = intent.getStringExtra(M7Constant.PHONE_NUM);

        call_choise_btn_sip = (ImageButton) findViewById(R.id.call_choise_btn_sip);
        call_choise_btn_back = (ImageButton) findViewById(R.id.call_choise_btn_back);
        call_choise_btn_phone = (ImageButton) findViewById(R.id.call_choise_btn_phone);
        call_choise_btn_cancel = (ImageButton) findViewById(R.id.call_choise_btn_cancel);

        call_choise_btn_sip.setOnClickListener(this);
        call_choise_btn_back.setOnClickListener(this);
        call_choise_btn_phone.setOnClickListener(this);
        call_choise_btn_cancel.setOnClickListener(this);

        bindService(new Intent().setComponent(new ComponentName("com.moor.im", "com.csipsimple.service.SipService"))
                , connection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_choise_btn_sip:
                if(Utils.isNetWorkConnected(this)) {
                    if(!"".equals(phone)) {
                        makeCall(phone);
                    }else {
                        Toast.makeText(CallChoiseDialog.this, "请输入电话号码拨打", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this, "网络错误，请重试！",
                            Toast.LENGTH_LONG).show();
                }
                finish();
                break;
            case R.id.call_choise_btn_back:
                makeBackCall(phone);
                finish();
                break;
            case R.id.call_choise_btn_phone:
                Intent intent = new Intent(Intent.ACTION_CALL, Uri
                        .parse("tel:" + phone));
                startActivity(intent);
                finish();
                break;
            case R.id.call_choise_btn_cancel:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    public void makeCall(String callee) {
        Long id = -1L;
        Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, null,
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                id = c.getLong(c.getColumnIndex("id"));
            }
        }
        if(service != null) {
            try {
                service.makeCall(callee, id.intValue());
            } catch (RemoteException e) {
                Toast.makeText(CallChoiseDialog.this, "拨打电话失败", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void makeBackCall(String phone) {
        User user = UserDao.getInstance().getUser();
        String mobile = user.mobile;
        if(mobile == null || "".equals(mobile)) {
            Toast.makeText(this, "未绑定手机，不能进行回拨", Toast.LENGTH_SHORT).show();
        }else {
            // TODO Auto-generated method stub
            if (Utils.isNetWorkConnected(this)) {
                // 跳转到正在通话页面
                Intent calling = new Intent(this,
                        CallingActivity.class);
                calling.putExtra("phone_number", phone);
                startActivity(calling);
            } else {
                Toast.makeText(this, "网络错误，请重试！",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
