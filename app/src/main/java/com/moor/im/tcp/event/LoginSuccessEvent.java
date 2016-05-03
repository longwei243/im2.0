package com.moor.im.tcp.event;

/**
 * eventbus 登录成功事件
 * Created by longwei on 2016/4/6.
 */
public class LoginSuccessEvent {
    public String connectionId;

    public LoginSuccessEvent() {}

    public LoginSuccessEvent(String connectionId) {
        this.connectionId = connectionId;
    }
}
