package com.moor.im.tcp.imservice;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.db.dao.MessageDao;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.event.LoginFailed;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.model.Info;
import com.moor.im.common.model.NewMessage;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.common.utils.Utils;
import com.moor.im.options.discussion.parser.DiscussionParser;
import com.moor.im.options.group.parser.GroupParser;
import com.moor.im.options.main.MainActivity;
import com.moor.im.options.mobileassistant.erp.activity.ErpActivity;
import com.moor.im.tcp.event.LoginFailedEvent;
import com.moor.im.tcp.event.LoginKickedEvent;
import com.moor.im.tcp.event.LoginSuccessEvent;
import com.moor.im.tcp.event.MsgEvent;
import com.moor.im.tcp.event.NetStatusEvent;
import com.moor.im.tcp.event.NewOrderEvent;
import com.moor.im.tcp.eventbus.EventBus;
import com.moor.im.tcp.manager.SocketManager;
import com.moor.im.tcp.manager.SocketStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by longwei on 2016/3/23.
 */
public class IMService extends Service{

    private Messenger mMessenger;
    private SocketManager mSocketManager;
    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;
    private static final Random random = new Random(System.currentTimeMillis());
    private NotificationManager notificationManager;
    private List<FromToMessage> fromToMessage;
    private String largeMsgId;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == M7Constant.HANDLER_LOGIN) {
                //客户端登录
                mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:接收到客户端发送的登录请求");
                String name = msg.getData().getString("name");
                String password = msg.getData().getString("password");
                login(name, password);
            }else if(msg.what == M7Constant.HANDLER_LOGINOFF) {
                //注销
                mSocketManager.loginOff();
                mEditor.clear();
                mEditor.commit();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSocketManager = SocketManager.getInstance(MobileApplication.getInstance());
        mMessenger = new Messenger(mHandler);
        mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:进入onCreate方法");
        EventBus.getDefault().register(this);
        mSp = getSharedPreferences(M7Constant.IMSERVICE_SP, 0);
        mEditor = mSp.edit();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:进入onStartCommand方法");

        boolean isLoginSucceed = mSp.getBoolean(M7Constant.SP_LOGIN_SUCCEED, false);
        //进行自动登录
        if(isLoginSucceed
                && Utils.isNetWorkConnected(MobileApplication.getInstance())
                && !mSocketManager.isLoginKicked()
                && !mSocketManager.isLoginOff()) {
            reLogin();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void login(String name, String password) {
        Info info = new Info();
        info.name = name;
        info.password = password;
        info.isSucceed = "false";
        InfoDao.getInstance().insertInfoToDao(info);
        mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:用户登录信息被保存了");
        mSocketManager.login(name, password);
    }

    /**
     * 重连登录
     */
    private void reLogin() {
        Info info = InfoDao.getInstance().getInfo();
        if(info != null) {
            String isSuccess = info.isSucceed;
            if("true".equals(isSuccess)) {
                String name = info.name;
                String password = info.password;
                mSocketManager.login(name, password);
            }
        }
    }

    //=====================事件总线==========================
    public void onEventMainThread(LoginSuccessEvent loginSuccessEvent) {
        mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:接收到登录成功的事件");
        //可以启动心跳了
        mSocketManager.startHeartBeat();
        mEditor.putBoolean(M7Constant.SP_LOGIN_SUCCEED, true);
        mEditor.commit();
        //登录成功,发送广播通知主进程
        Intent loginSuccessIntent = new Intent();
        loginSuccessIntent.putExtra(M7Constant.CONNECTION_ID, loginSuccessEvent.connectionId);
        loginSuccessIntent.setAction(M7Constant.ACTION_LOGIN_SUCCESS);
        sendBroadcast(loginSuccessIntent);
    }

    public void onEventMainThread(LoginFailedEvent loginFailedEvent) {
        //登录失败,发送广播通知主进程
        mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:接收到登录失败的事件");
        Intent loginFailedIntent = new Intent();
        loginFailedIntent.setAction(M7Constant.ACTION_LOGIN_FAILED);
        sendBroadcast(loginFailedIntent);
    }
    public void onEventMainThread(LoginKickedEvent loginKickedEvent) {
        //被踢了,发送广播通知主进程
        mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:接收到被踢的事件");
        Intent loginKickedIntent = new Intent();
        loginKickedIntent.setAction(M7Constant.ACTION_LOGIN_KICKED);
        sendBroadcast(loginKickedIntent);
    }

    public void onEventMainThread(NetStatusEvent netStatusEvent) {
        if(NetStatusEvent.NET_RECONNECT.equals(netStatusEvent)) {
            //收到重连事件,判断状态，是否需要重连
            mSocketManager.logger.debug(TimeUtil.getCurrentTime()+"IMService:接收到重连的事件");
            //有网,状态是tcp断了，登录状态不能是被踢和注销
            if(Utils.isNetWorkConnected(MobileApplication.getInstance()) && SocketStatus.BREAK.equals(mSocketManager.getStatus()) && !mSocketManager.isLoginKicked() && !mSocketManager.isLoginOff()) {
                reLogin();
            }
        }
    }

    /**
     * 新工单
     */
    public void onEventMainThread(NewOrderEvent orderEvent){
        if(isErpForground(this)) {
            //工单页面在最上面
            mSocketManager.logger.debug("工单页在前台");
            Intent intent = new Intent(M7Constant.ACTION_NEW_ORDER);
            sendBroadcast(intent);
        }else {
            mSocketManager.logger.debug("工单页在后台， 发送了通知");
            Intent contentIntent = new Intent(this,
                    ErpActivity.class);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            MobileApplication.getInstance(),
                            0,
                            contentIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(MobileApplication.getInstance());
            Notification notification = builder.setTicker("新工单")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(resultPendingIntent)
                    .setContentTitle("您有新的工单")
                    .setContentText("")
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(1, notification);
        }
    }

    public boolean isErpForground(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getClassName().equals("com.moor.im.options.mobileassistant.erp.activity.ErpActivity")) {
                return false;
            }
        }
        return true;
    }

    //有新消息了
    public void onEventMainThread(MsgEvent msgEvent) {
        getMsg();
    }

    private void getMsg() {
        ArrayList<String> array = MessageDao.getInstance()
                .getUnReadDao();
        HttpManager.getInstance().getMsg(InfoDao.getInstance().getConnectionId(), array,
                new ResponseListener() {
                    Notification notification;
                    @Override
                    public void onFailed() {

                    }

                    @Override
                    public void onSuccess(String responseStr) {
                        boolean isLargeMsg = HttpParser.isLargeMsg(responseStr);
                        System.out.println("isLargeMsg的值为："+isLargeMsg);
                        // 获取数据成功并且不是大量数据
                        if (HttpParser.getSucceed(responseStr)) {
                            System.out.println("取消息返回数据:"+responseStr);
                            if(isLargeMsg) {
                                //有大量的数据
                                mSocketManager.logger.debug("有大量消息要来了");
//                                getLargeMsgsFromNet(largeMsgId);
                            }else {
                                //没有大量的数据
                                fromToMessage = HttpParser.getMsgs(responseStr);
                                if(MessageDao.getInstance().contains(fromToMessage)) {
                                    return;
                                }
                                // 判断数据是否被读取、及时更新
                                MessageDao.getInstance().updateMsgsIdDao();
                                // 存入手机数据库
                                MessageDao.getInstance().insertGetMsgsToDao(fromToMessage);
                                mSocketManager.logger.debug("消息存到了数据库中");
                                // 不等于0说明有新消息
                                if (fromToMessage.size() != 0) {
                                    String fromStr = "";
                                    String messageStr = "";
                                    if("User".equals(fromToMessage.get(0).type)) {
                                        fromStr = ContactsDao.getInstance().getContactsName(
                                                fromToMessage.get(0).from);
                                        if(FromToMessage.MSG_TYPE_TEXT.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = fromToMessage.get(0).message;
                                        }else if(FromToMessage.MSG_TYPE_IMAGE.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = "[图片]";
                                        }else if(FromToMessage.MSG_TYPE_AUDIO.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = "[语音]";
                                        }
                                    }else if("Group".equals(fromToMessage.get(0).type)) {
                                        fromStr = GroupParser.getInstance().getNameById(fromToMessage.get(0).sessionId);
                                        String fromName = ContactsDao.getInstance().getContactsName(
                                                fromToMessage.get(0).from);
                                        if(FromToMessage.MSG_TYPE_TEXT.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = fromName + ":"+ fromToMessage.get(0).message;
                                        }else if(FromToMessage.MSG_TYPE_IMAGE.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = fromName + ":"+ "[图片]";
                                        }else if(FromToMessage.MSG_TYPE_AUDIO.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = fromName + ":"+ "[语音]";
                                        }
                                    }else if("Discussion".equals(fromToMessage.get(0).type)) {
                                        fromStr = DiscussionParser.getInstance().getNameById(fromToMessage.get(0).sessionId);
                                        String fromName = ContactsDao.getInstance().getContactsName(
                                                fromToMessage.get(0).from);
                                        if(FromToMessage.MSG_TYPE_TEXT.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = fromName + ":"+ fromToMessage.get(0).message;
                                        }else if(FromToMessage.MSG_TYPE_IMAGE.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = fromName + ":"+ "[图片]";
                                        }else if(FromToMessage.MSG_TYPE_AUDIO.equals(fromToMessage.get(0).msgType)) {
                                            messageStr = fromName + ":"+ "[语音]";
                                        }
                                    }

                                    Intent contentIntent = new Intent(MobileApplication.getInstance(), MainActivity.class);
                                    contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    PendingIntent resultPendingIntent =
                                            PendingIntent.getActivity(
                                                    MobileApplication.getInstance(),
                                                    0,
                                                    contentIntent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                            );

                                    //新的通知
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MobileApplication.getInstance());
                                    notification = builder.setTicker("您有新的消息")
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setWhen(System.currentTimeMillis())
                                            .setContentIntent(resultPendingIntent)
                                            .setContentTitle(fromStr)
                                            .setContentText(messageStr)
                                            .setAutoCancel(true)
                                            .build();

                                    // 查询是否有某个人的消息如果没有则插入，如果有则先删除在插入
                                    for (int i = 0; i < fromToMessage.size(); i++) {

                                        mSocketManager.logger.debug("IMService:消息的类型是:"+fromToMessage.get(i).type);
                                        if("User".equals(fromToMessage.get(i).type)) {
                                            mSocketManager.logger.debug("IMService:接收到个人发的消息");
                                            List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
                                                    fromToMessage.get(i).sessionId);
                                            if (newMsgs.size() == 0) {

                                                NewMessageDao.getInstance().insertNewMsgs(
                                                        fromToMessage.get(i).sessionId,
                                                        fromToMessage.get(i).message,
                                                        fromToMessage.get(i).msgType,
                                                        ContactsDao.getInstance().getContactsName(
                                                                fromToMessage.get(i).from)
                                                                + "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
                                            } else {

                                                NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
                                                nm.message = fromToMessage.get(i).message;
                                                nm.msgType = fromToMessage.get(i).msgType;
                                                nm.fromName = ContactsDao.getInstance().getContactsName(
                                                        fromToMessage.get(i).from);
                                                nm.time = fromToMessage.get(i).when;
                                                nm.unReadCount = nm.unReadCount + 1;
                                                nm.type = fromToMessage.get(i).type;
                                                nm.from = fromToMessage.get(i).from;
                                                NewMessageDao.getInstance().updateMsg(nm);

                                            }
                                        }else if("Group".equals(fromToMessage.get(i).type)) {
                                            // 群组的最新消息
                                            if("System".equals(fromToMessage.get(i).from)) {
                                                if(fromToMessage.get(i).message.contains("解散")) {
                                                    NewMessageDao.getInstance().deleteMsgById(fromToMessage.get(i).sessionId);
                                                }
                                                fromToMessage.get(i).sessionId = "System";
                                                fromToMessage.get(i).type = "System";
                                                MessageDao.getInstance().updateMsgToDao(fromToMessage.get(i));
                                                //获取一次群组的数据, 通知主进程刷新群组数据
                                                Intent groupIntent = new Intent(M7Constant.ACTION_GROUP_UPDATE);
                                                sendBroadcast(groupIntent);
//                                                getGroupDataFromNet();
                                            }
                                            List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
                                                    fromToMessage.get(i).sessionId);
                                            if (newMsgs.size() == 0) {
                                                NewMessageDao.getInstance().insertNewMsgs(
                                                        fromToMessage.get(i).sessionId,
                                                        fromToMessage.get(i).message,
                                                        fromToMessage.get(i).msgType,
                                                        GroupParser.getInstance().getNameById(fromToMessage.get(i).sessionId)
                                                                + "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
                                            } else {

                                                NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
                                                nm.message = fromToMessage.get(i).message;
                                                nm.msgType = fromToMessage.get(i).msgType;
                                                nm.fromName = GroupParser.getInstance().getNameById(fromToMessage.get(i).sessionId);
                                                nm.time = fromToMessage.get(i).when;
                                                nm.unReadCount = nm.unReadCount + 1;
                                                nm.type = fromToMessage.get(i).type;
                                                nm.from = fromToMessage.get(i).from;
                                                NewMessageDao.getInstance().updateMsg(nm);

                                            }
                                        }else if("Discussion".equals(fromToMessage.get(i).type)) {
                                            // 讨论组的最新消息
                                            if("System".equals(fromToMessage.get(i).from)) {
                                                if(fromToMessage.get(i).message.contains("解散")) {
                                                    NewMessageDao.getInstance().deleteMsgById(fromToMessage.get(i).sessionId);
                                                }
                                                fromToMessage.get(i).sessionId = "System";
                                                fromToMessage.get(i).type = "System";
                                                MessageDao.getInstance().updateMsgToDao(fromToMessage.get(i));
                                                //获取一次讨论组的数据, 通知主进程刷新群组数据
                                                Intent discussionIntent = new Intent(M7Constant.ACTION_DISCUSSION_UPDATE);
                                                sendBroadcast(discussionIntent);
//                                                getDiscussionDataFromNet();
                                            }
                                            List<NewMessage> newMsgs = NewMessageDao.getInstance().isQueryMessage(
                                                    fromToMessage.get(i).sessionId);
                                            if (newMsgs.size() == 0) {

                                                NewMessageDao.getInstance().insertNewMsgs(
                                                        fromToMessage.get(i).sessionId,
                                                        fromToMessage.get(i).message,
                                                        fromToMessage.get(i).msgType,
                                                        DiscussionParser.getInstance().getNameById(fromToMessage.get(i).sessionId)
                                                                + "", fromToMessage.get(i).when, 1, fromToMessage.get(i).type, fromToMessage.get(i).from);
                                            } else {
                                                NewMessage nm = NewMessageDao.getInstance().getNewMsg(fromToMessage.get(i).sessionId);
                                                nm.message = fromToMessage.get(i).message;
                                                nm.msgType = fromToMessage.get(i).msgType;
                                                nm.fromName = DiscussionParser.getInstance().getNameById(fromToMessage.get(i).sessionId);
                                                nm.time = fromToMessage.get(i).when;
                                                nm.unReadCount = nm.unReadCount + 1;
                                                nm.type = fromToMessage.get(i).type;
                                                nm.from = fromToMessage.get(i).from;
                                                NewMessageDao.getInstance().updateMsg(nm);

                                            }
                                        }

                                    }
                                }

                            }

                            // 收到消息通知页面
                            if(isAppForground()) {
                                mSocketManager.logger.debug("应用在前台");

                            }else {
                                mSocketManager.logger.debug("应用在后台");
                                notificationManager.notify(1, notification);
                                mSocketManager.logger.debug("显示了通知栏");
                            }

                            Intent newMsgIntent = new Intent();
                            newMsgIntent.setAction(M7Constant.ACTION_NEW_MSG);
                            sendBroadcast(newMsgIntent);
                        }
                    }
                });
    }


    public boolean isAppForground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null) {
            mSocketManager.logger.debug("appProcesses is null");
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if ("com.moor.im".equals(appProcess.processName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

}
