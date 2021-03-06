package com.moor.im.options.welcome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.options.intro.IntroActivity;
import com.moor.im.options.intro2.Intro2Activity;
import com.moor.im.options.login.LoginActivity;
import com.moor.im.options.main.MainActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/3/17.
 */
public class WelcomeActivity extends Activity{

    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mSp = getSharedPreferences(M7Constant.MAIN_SP, 0);
        mEditor = mSp.edit();
        final String appVersion_sp = mSp.getString("appVersion", "");
        final String version = getVersion();
        if(version.equals(appVersion_sp)) {
            mEditor.putBoolean("versionChanged", false);
        }else {
            mEditor.putBoolean("versionChanged", true);
        }
        mEditor.putString("appVersion", version);
        mEditor.commit();
        Observable
                .timer(600, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        boolean isFirstRunApp = mSp.getBoolean(M7Constant.SP_FIRST_RUN_APP, true);
                        if(isFirstRunApp) {
                            //第一次启动app,显示引导页
                            mEditor.putBoolean(M7Constant.SP_FIRST_RUN_APP, false);
                            mEditor.commit();

                            Intent introIntent = new Intent(WelcomeActivity.this, Intro2Activity.class);
                            startActivity(introIntent);
                            finish();

                        }else {
                            //判断是否登录成功过而且版本号没变
                            boolean isLoginSucceed = mSp.getBoolean(M7Constant.SP_LOGIN_SUCCEED, false);
                            if(isLoginSucceed && version.equals(appVersion_sp)) {
                                //登录成功过，直接进主界面
                                Intent main = new Intent(WelcomeActivity.this, MainActivity.class);
                                startActivity(main);
                                finish();
                            }else{
                                //进入登录界面
                                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
    }


    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
