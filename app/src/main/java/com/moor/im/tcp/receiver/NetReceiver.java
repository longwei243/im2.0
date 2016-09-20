package com.moor.im.tcp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.moor.im.app.MobileApplication;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.tcp.event.NetStatusEvent;
import com.moor.im.tcp.eventbus.EventBus;
import com.moor.im.tcp.manager.SocketManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2016/4/11.
 */
public class NetReceiver extends BroadcastReceiver{

    private String mNetworkType;
    private boolean mConnected = false;
    private String mRoutes = "";
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        boolean connected = (info != null && info.isConnected());
        String networkType = connected ? info.getTypeName() : "null";
        String currentRoutes = dumpRoutes();
        String oldRoutes;
        synchronized (mRoutes) {
            oldRoutes = mRoutes;
        }

        // Ignore the event if the current active network is not changed.
        if (connected == mConnected && networkType.equals(mNetworkType) && currentRoutes.equals(oldRoutes)) {
            return;
        }

        // Now process the event
        synchronized (mRoutes) {
            mRoutes = currentRoutes;
        }
        mConnected = connected;
        mNetworkType = networkType;

        if (connected) {
            scheduleHeartbeatInterval(info);
            EventBus.getDefault().post(NetStatusEvent.NET_OK);
            SocketManager.getInstance(MobileApplication.getInstance()).logger.debug(TimeUtil.getCurrentTime()+"NetReceiver 网络重新连接上，发送了重连的事件");
            //给主进程发送广播

        }else {
            mConnected = false;
            EventBus.getDefault().post(NetStatusEvent.NET_BREAK);
            SocketManager.getInstance(MobileApplication.getInstance()).logger.debug(TimeUtil.getCurrentTime()+"NetReceiver 网络断了");

        }

    }

    /**
     * 设置心跳间隔
     * @param info
     */
    private void scheduleHeartbeatInterval(NetworkInfo info) {
        if(info.getType() == ConnectivityManager.TYPE_WIFI) {
            //wifi 10s
        SocketManager.getInstance(MobileApplication.getInstance()).setHeartBeatInterval(10 * 1000);
        }else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            //手机网络
            switch(info.getSubtype()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    //2g 使用默认间隔
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    //3g 使用默认间隔
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    //4g 30s
                    SocketManager.getInstance(MobileApplication.getInstance()).setHeartBeatInterval(30 * 1000);
                    break;
            }
        }
    }

    private static final String PROC_NET_ROUTE = "/proc/net/route";
    private String dumpRoutes() {
        String routes = "";
        FileReader fr = null;
        try {
            fr = new FileReader(PROC_NET_ROUTE);
            if(fr != null) {
                StringBuffer contentBuf = new StringBuffer();
                BufferedReader buf = new BufferedReader(fr);
                String line;
                while ((line = buf.readLine()) != null) {
                    contentBuf.append(line+"\n");
                }
                routes = contentBuf.toString();
                buf.close();
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }finally {
            try {
                if(fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
            }
        }

        // Clean routes that point unique host
        // this aims to workaround the fact android 4.x wakeup 3G layer when position is retrieve to resolve over 3g position
        String finalRoutes = routes;
        if(!TextUtils.isEmpty(routes)) {
            String[] items = routes.split("\n");
            List<String> finalItems = new ArrayList<String>();
            int line = 0;
            for(String item : items) {
                boolean addItem = true;
                if(line > 0){
                    String[] ent = item.split("\t");
                    if(ent.length > 8) {
                        String maskStr = ent[7];
                        if(maskStr.matches("^[0-9A-F]{8}$")) {
                            int lastMaskPart = Integer.parseInt(maskStr.substring(0, 2), 16);
                            if(lastMaskPart > 192) {
                                // if more than 255.255.255.192 : ignore this line
                                addItem = false;
                            }
                        }else {
                        }
                    }
                }

                if(addItem) {
                    finalItems.add(item);
                }
                line ++;
            }
            finalRoutes = TextUtils.join("\n", finalItems);
        }

        return finalRoutes;
    }

}
