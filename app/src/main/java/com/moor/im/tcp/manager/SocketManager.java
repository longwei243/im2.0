package com.moor.im.tcp.manager;

import android.content.Context;

import com.moor.im.app.MobileApplication;
import com.moor.im.app.RequestUrl;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.tcp.logger.Logger;
import com.moor.im.tcp.logger.LoggerFactory;
import com.moor.im.tcp.logger.appender.FileAppender;
import com.moor.im.tcp.logger.config.PropertyConfigurator;

import org.jboss.netty.channel.Channel;

/**
 * Created by longwei on 2016/3/23.
 */
public class SocketManager {

    public static final String SP_NAME = "sp_imservice";

    private SocketThread socketThread = null;

    private SocketStatus status = SocketStatus.NONE;

    private HeartBeatManager heartBeatManager;

    private static SocketManager instance;
    private Context context;

    private int channelId = -1;

    /**
     * 是否被踢
     */
    private boolean isLoginKicked = false;

    /**
     * 是否注销
     */
    private boolean isLoginOff = false;

    /**
     * 保存到文件中的日志
     */
    public Logger logger = LoggerFactory.getLogger(MobileApplication.class);

    private SocketManager (Context context) {
        this.context = context;
        //日志配置
//        PropertyConfigurator.getConfigurator(context).configure();
//        final FileAppender fa = (FileAppender)logger.getAppender(1);
//        fa.setAppend(true);
        heartBeatManager = new HeartBeatManager(context, this);
    }

    /**
     * 这里的context传应用全局的
     * @param context
     * @return
     */
    public static SocketManager getInstance(Context context) {
        if(instance == null) {
            synchronized (SocketManager.class) {
                if(instance == null) {
                    instance = new SocketManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 发送数据
     * @param data
     */
    public void sendData(String data) {
        if(socketThread != null && !socketThread.isClose()) {
            try {
                socketThread.sendData(data);
            }catch (Exception e) {
            }
        }
    }

    /**
     * 获得tcp状态
     * @return
     */
    public SocketStatus getStatus() {
        return status;
    }

    /**
     * 设置tcp状态
     * @param ss
     */
    public void setStatus(SocketStatus ss) {
        this.status = ss;
        logger.debug(TimeUtil.getCurrentTime()+"SocketManager:状态切换为:"+ss.name());
    }

    /**
     * 登录
     */
    public void login(String name, String password) {
        if(socketThread != null) {
            if(!socketThread.isClose()) {
                socketThread.close();
            }
            socketThread = null;
        }

        socketThread = new SocketThread(instance, RequestUrl.baseTcpHost, RequestUrl.baseTcpPort, new ServerMessageHandler(context), name, password);
        socketThread.start();

    }

    public void loginOff() {
        sendData("quit\n");
        setLoginOff(true);
    }

    /**
     * 断开tcp链接
     */
    public void disconnectServer() {
        if(socketThread != null) {
            if(!socketThread.isClose()) {
                socketThread.close();
            }
            socketThread = null;
        }
    }

    /**
     * 设置心跳间隔
     * @param second
     */
    public void setHeartBeatInterval(int second) {
        heartBeatManager.setHeartInterval(second);
    }

    /**
     * 启动心跳
     */
    public void startHeartBeat() {
        heartBeatManager.startHeartBeat();
    }

    public boolean isLoginKicked() {
        return isLoginKicked;
    }

    public void setLoginKicked(boolean loginKicked) {
        isLoginKicked = loginKicked;
    }

    public boolean isLoginOff() {
        return isLoginOff;
    }

    public void setLoginOff(boolean loginOff) {
        isLoginOff = loginOff;
    }

    public int getChannelId() {
        if(socketThread != null) {
            Channel channel = socketThread.getChannel();
            if(channel != null) {
                channelId = channel.getId();
            }
        }
        return channelId;
    }
}
