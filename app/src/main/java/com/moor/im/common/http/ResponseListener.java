package com.moor.im.common.http;

/**
 * Created by longwei on 2016/4/11.
 */
public interface ResponseListener {
    void onFailed();
    void onSuccess(String responseStr);
}
