package com.moor.im.tcp.manager;

/**
 * Created by longwei on 2016/3/23.
 * socket状态
 */
public enum SocketStatus {

    /**
     * 没有进行连接
     */
    NONE,
    /**
     * 连接被断开
     */
    BREAK,
    /**
     * 正在连接服务器
     */
    CONNECTING,
    /**
     * 连接服务器成功了
     */
    CONNECTED,
    /**
     * 等待登陆结果
     */
    WAIT_LOGIN,
    /**
     * 已登陆
     */
    LOGINED,
    /**
     * 登录失败
     */
    LOGINFAILED,
    /**
     * 被踢了
     */
    KICKED
}
