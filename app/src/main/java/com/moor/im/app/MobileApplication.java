package com.moor.im.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.csipsimple.service.SipService;
import com.moor.im.BuildConfig;
import com.moor.im.common.event.LoginKicked;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.CacheUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.utils.log.Settings;
import com.moor.im.options.chat.utils.FaceConversionUtil;
import com.moor.im.options.login.KickedActivity;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by longwei on 2016/3/16.
 */
public class MobileApplication extends Application{

    private static MobileApplication instance;

    public static CacheUtils cacheUtil;
    public static boolean isKFSDK = false;
    private List<Activity> activities = new ArrayList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        cacheUtil = CacheUtils.get(instance);

        String processName = getProcessName(this, android.os.Process.myPid());
        if (!TextUtils.isEmpty(processName) && processName.equals(this.getPackageName())) {//判断进程名，保证只有主进程运行
            //主进程初始化逻辑
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()
//                    .penaltyLog()
//                    .build());
            initLogUtil();
            startIMService();
            startSipService();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FaceConversionUtil.getInstace().getFileText(instance);
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    com.m7.imkfsdk.utils.FaceConversionUtil.getInstace().getFileText(instance);
                }
            }).start();

            addRxBusListener();

            //异常处理
//            CrashHandler crashHandler = CrashHandler.getInstance();
//            crashHandler.init(instance);
        }
        CrashReport.initCrashReport(instance, "900005144", false);
        LeakCanary.install(instance);
    }

    public static MobileApplication getInstance() {
        return instance;
    }

    /**
     * 获得进程名字
     * @param cxt
     * @param pid
     * @return
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    /**
     * 启动IMService
     */
    private void startIMService() {
        new Thread(){
            @Override
            public void run() {
                LogUtil.d("启动IMService");
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.moor.im", "com.moor.im.tcp.imservice.IMService"));
                startService(intent);
            }
        }.start();
    }

    private void startSipService() {
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(instance, SipService.class);
                startService(serviceIntent);
            }
        };
        t.start();
        LogUtil.d("MobileApplication", "启动SipService");
    }
    /**
     * 初始化日志工具,只用来打印主进程的日志
     */
    private void initLogUtil() {
        LogUtil.initialize(
                Settings.getInstance()
                        .isShowMethodLink(true)
                        .isShowThreadInfo(true)
                        .setMethodOffset(0)
                        .setLogPriority(BuildConfig.DEBUG ? Log.VERBOSE : Log.ASSERT)
        );
    }

    private void addRxBusListener() {
        RxBus.getInstance().toObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                if(o instanceof LoginKicked) {
                    Intent intent = new Intent(instance, KickedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    public void add(Activity a) {
        activities.add(a);
    }
    public void remove(Activity a) {
        activities.remove(a);
    }
    public void exit() {
        for (int i = 0; i < activities.size(); i++) {
            activities.get(i).finish();
        }
    }
}
