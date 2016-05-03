package com.moor.im.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.moor.im.BuildConfig;
import com.moor.im.common.utils.CacheUtils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.utils.log.Settings;
import com.moor.im.options.chat.utils.FaceConversionUtil;

import java.util.List;

/**
 * Created by longwei on 2016/3/16.
 */
public class MobileApplication extends Application{

    private static MobileApplication instance;

    public static CacheUtils cacheUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        cacheUtil = CacheUtils.get(instance);

        String processName = getProcessName(this, android.os.Process.myPid());
        if (!TextUtils.isEmpty(processName) && processName.equals(this.getPackageName())) {//判断进程名，保证只有主进程运行
            //主进程初始化逻辑
            initLogUtil();
            startIMService();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FaceConversionUtil.getInstace().getFileText(instance);
                }
            }).start();
        }

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
}
