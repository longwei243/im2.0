package com.moor.im.options.login;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.db.dao.UserRoleDao;
import com.moor.im.common.event.LoginFailed;
import com.moor.im.common.event.LoginSuccess;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.User;
import com.moor.im.common.model.UserRole;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.Utils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.progress.ProgressWheel;
import com.moor.im.options.main.MainActivity;
import com.moor.im.tcp.imservice.IMService;

import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 登录界面
 * Created by longwei on 2016/3/16.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout login_til_name, login_til_password;
    private Button login_btn_submit;
    private ProgressWheel login_pw;
    private Messenger messenger;

    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    private CompositeSubscription _subscriptions;

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

        setContentView(R.layout.activity_login);

        mSp = getSharedPreferences(M7Constant.MAIN_SP, 0);
        mEditor = mSp.edit();

        if(Build.VERSION.SDK_INT < 23) {
            init();
        }else {
            //6.0
            if(ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //该权限已经有了
                init();
            }else {
                //申请该权限
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x1111);
            }
        }
    }

    private void init() {
        login_til_name = (TextInputLayout) findViewById(R.id.login_til_name);
        login_til_password = (TextInputLayout) findViewById(R.id.login_til_password);

        //测试
        login_til_name.getEditText().setText("8001@phoneTest");
        login_til_password.getEditText().setText("8001");

        login_btn_submit = (Button) findViewById(R.id.login_btn_submit);
        login_pw = (ProgressWheel) findViewById(R.id.login_pw);

        bindService(new Intent(this, IMService.class), conn, Context.BIND_AUTO_CREATE);

        //使用rxbus来接收登录成功的事件
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof LoginSuccess) {
                            LogUtil.d("登录成功了");
                            HttpManager.getInstance().getUserInfo(InfoDao.getInstance().getConnectionId(), new ResponseListener() {
                                @Override
                                public void onFailed() {
                                    LogUtil.d("获取用户信息失败");
                                }

                                @Override
                                public void onSuccess(String responseStr) {
                                    LogUtil.d("获取用户信息成功返回数据:"+responseStr);
                                    if(HttpParser.getSucceed(responseStr)) {
                                        processUserInfoData(responseStr);
                                    }else {
                                        login_btn_submit.setVisibility(View.VISIBLE);
                                        login_pw.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }else if(event instanceof LoginFailed) {
                            LogUtil.d("登录失败");
                            login_btn_submit.setVisibility(View.VISIBLE);
                            login_pw.setVisibility(View.GONE);
                        }
                    }
                }));

        login_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = login_til_name.getEditText().getText().toString().trim();
                String password = login_til_password.getEditText().getText().toString().trim();
                if("".equals(name)) {
                    Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                }else if("".equals(password)) {
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                }else {
                    if(Utils.isNetWorkConnected(LoginActivity.this)) {
                        login(name, password);
                    }else {
                        Toast.makeText(LoginActivity.this, "当前无网络连接", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * 客户端登录
     * @param name
     * @param password
     */
    private void login(String name, String password) {

        if (messenger == null) {
            return;
        }

        login_btn_submit.setVisibility(View.GONE);
        login_pw.setVisibility(View.VISIBLE);
        LogUtil.d("客户端登录");
        Message msg1 = Message.obtain(null, M7Constant.HANDLER_LOGIN);
        Bundle b = new Bundle();
        b.putString("name", name);
        b.putString("password", password);
        msg1.setData(b);
        try {
            messenger.send(msg1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        _subscriptions.unsubscribe();
    }

    /**
     * 处理用户信息数据
     * @param responseStr
     */
    private void processUserInfoData(String responseStr) {
        boolean succeed = HttpParser.getSucceed(responseStr);
        if(succeed) {
            User user = HttpParser.getUserInfo(responseStr);
            LogUtil.d("user name is:"+user.displayName);
            // 用户信息存入数据库
            UserDao.getInstance().deleteUser();
            UserRoleDao.getInstance().deleteUserRole();
            UserDao.getInstance().insertUser(user);
            List<String> userRoles = user.role;
            for (String role : userRoles) {
                UserRole ur = new UserRole();
                ur.role = role;
                ur.user = user;
                UserRoleDao.getInstance().insertUserRole(ur);
            }
            //保存登陆成功到sp文件
            mEditor.putBoolean(M7Constant.SP_LOGIN_SUCCEED ,true);
            mEditor.commit();

            Intent main = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(main);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0x1111:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    init();
                } else {
                    finish();
                }
                break;
        }
    }

}
