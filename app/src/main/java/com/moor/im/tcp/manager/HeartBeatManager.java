package com.moor.im.tcp.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.moor.im.common.utils.TimeUtil;

/**
 * 心跳管理器
 * @author LongWei
 *
 */
public class HeartBeatManager {

	/**
	 * 心跳间隔
	 */
	private int heartInterval = 4 * 60 * 1000;

    /**
     * 设置心跳间隔
     * @param heartInterval
     */
	public void setHeartInterval(int heartInterval) {
		this.heartInterval = heartInterval;
	}

	private final String ACTION_SENDING_HEARTBEAT = "com.moor.im.manager.heartbeatmanager";
    private PendingIntent pendingIntent;
	
    private Context context;
    private SocketManager socketManager;

    public HeartBeatManager(Context context, SocketManager socketManager) {
		this.context = context;
        this.socketManager = socketManager;
	}

    
    // 启动心跳
    public void startHeartBeat(){

        if(pendingIntent != null) {
            reset();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        context.registerReceiver(imReceiver, intentFilter);
        //获取AlarmManager系统服务
        scheduleHeartbeat(heartInterval);
        socketManager.logger.debug(TimeUtil.getCurrentTime()+"HeartBeatManager:开始发送心跳:"+heartInterval);
    }
    
    /**
     * 停止心跳
     */
    public void reset() {
        try {
        	context.unregisterReceiver(imReceiver);
            cancelHeartbeatTimer();
        }catch (Exception e){
        }
    }
    
    // ServerHandler tcp连接断了直接调用，重置心跳
    public void onServerDisconn(){
    	reset();
    }
    
    /**
     * 取消心跳
     */
    private void cancelHeartbeatTimer() {
        if (pendingIntent == null) {
            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }


    private void scheduleHeartbeat(int seconds){
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_SENDING_HEARTBEAT);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            if (pendingIntent == null) {
                return;
            }
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds, seconds, pendingIntent);
        socketManager.logger.debug(TimeUtil.getCurrentTime()+"HeartBeatManager:启动定时器");

    }
    
    private BroadcastReceiver imReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_SENDING_HEARTBEAT)) {
                sendHeartBeatPacket();
            }
        }
    };
    /**
     * 向服务器发送心跳包
     */
    public void sendHeartBeatPacket(){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "7moor_heartBeat_wakelock");
        wl.acquire();
        try {
            socketManager.sendData("3\n");
        } finally {
            wl.release();
        }
    }

}
