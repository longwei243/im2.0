package com.m7.imkfsdk.receiver;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.m7.imkfsdk.chat.ChatActivity;
import com.moor.imkf.IMChatManager;

import java.util.List;

/**
 * 新消息接收器
 *
 */
public class NewMsgReceiver extends BroadcastReceiver{
    private NotificationManager notificationManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if(intent.getAction().equals(IMChatManager.NEW_MSG_ACTION)) {

            //看应用是否在前台
            if(isAppForground(context)) {
                context.sendBroadcast(new Intent("com.m7.imkfsdk.msgreceiver"));
            }else {
//                Notification notification = new Notification();
//                notification.kf_icon = R.drawable.kf_ic_launcher;
//                notification.defaults = Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;
//                notification.flags |= Notification.FLAG_AUTO_CANCEL;
//                notification.when = System.currentTimeMillis();
//                notification.tickerText = "有新消息来了";
//
//                Intent it = new Intent(context,
//                        ChatActivity.class);
//                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                it.setAction(Intent.ACTION_MAIN);
//                it.addCategory(Intent.CATEGORY_LAUNCHER);
//                PendingIntent contentIntent = PendingIntent.getActivity(context, 1,
//                        it, PendingIntent.FLAG_UPDATE_CURRENT);
//                notification.setLatestEventInfo(context, "客服消息", "新消息",
//                        contentIntent);
//                notificationManager.notify(1, notification);

            }

        }
    }

    /**
     * 判断聊天界面是否在前台
     * @param mContext
     * @return
     */
    public boolean isAppForground(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getClassName().equals(ChatActivity.class.getName())) {
                return false;
            }
        }
        return true;
    }
}