package com.moor.im.options.login;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.dialog.MaterialDialog;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.tcp.imservice.IMService;

/**
 * Created by longwei on 2016/5/20.
 */
public class KickedActivity extends Activity{
    private Messenger messenger;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName cn) {
        }

        @Override
        public void onServiceConnected(ComponentName cn, IBinder ibiner) {
            messenger = new Messenger(ibiner);//得到Service中的Messenger
            LogUtil.d("bind imservice success");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_kicked);
        bindService(new Intent(this, IMService.class), conn, Context.BIND_AUTO_CREATE);
        final MaterialDialog mMaterialDialog = new MaterialDialog(KickedActivity.this);
        mMaterialDialog.setTitle("温馨提示")
                .setMessage("您的账号在其他设备登录,重新登录吗?")
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        loginForKicked();
                        mMaterialDialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("取消",
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                mMaterialDialog.dismiss();
                                cancel();
                                finish();
                            }
                        })
                .setCanceledOnTouchOutside(false)
                .show();
    }

    private void loginForKicked() {
        Message msg = Message.obtain(null, M7Constant.HANDLER_LOGIN_FOR_KICKED);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void cancel() {
        //退出应用就行了
        MobileApplication.getInstance().exit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
