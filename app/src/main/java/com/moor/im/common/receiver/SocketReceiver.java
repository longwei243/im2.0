package com.moor.im.common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.event.LoginFailed;
import com.moor.im.common.event.LoginSuccess;
import com.moor.im.common.event.NewMsgReceived;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.Group;
import com.moor.im.common.rxbus.RxBus;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * IMService发送出的广播接收器
 * Created by longwei on 2016/4/6.
 */
public class SocketReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action != null && !"".equals(action)) {
            if(M7Constant.ACTION_LOGIN_SUCCESS.equals(action)) {
                //发送事件
                RxBus.getInstance().send(new LoginSuccess());
            }else if(M7Constant.ACTION_LOGIN_FAILED.equals(action)) {
                //发送事件
                RxBus.getInstance().send(new LoginFailed());
            }else if(M7Constant.ACTION_LOGIN_KICKED.equals(action)) {
                //被踢了
            }else if(M7Constant.ACTION_NEW_MSG.equals(action)) {
                //接收了新消息
                RxBus.getInstance().send(new NewMsgReceived());
            }else if(M7Constant.ACTION_GROUP_UPDATE.equals(action)) {
                //更新群组
                HttpManager.getInstance().getGroupByUser(InfoDao.getInstance().getConnectionId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Group>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Group> groups) {
                        RxBus.getInstance().send(new NewMsgReceived());
                    }
                });
            }else if(M7Constant.ACTION_DISCUSSION_UPDATE.equals(action)) {
                //更新讨论组
                HttpManager.getInstance().getDiscussionByUser(InfoDao.getInstance().getConnectionId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<Discussion>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(List<Discussion> discussions) {
                                RxBus.getInstance().send(new NewMsgReceived());
                            }
                        });

            }
        }
    }


}
