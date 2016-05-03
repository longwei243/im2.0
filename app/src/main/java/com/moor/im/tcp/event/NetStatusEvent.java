package com.moor.im.tcp.event;

/**
 * Created by longwei on 2016/4/11.
 */
public enum NetStatusEvent {
    /**
     * 网络连接正常
     */
    NET_OK,
    /**
     * 没有网络连接
     */
    NET_BREAK,
    /**
     * 需要重连
     */
    NET_RECONNECT
}
