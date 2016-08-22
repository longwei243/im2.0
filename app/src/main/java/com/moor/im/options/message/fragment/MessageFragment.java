package com.moor.im.options.message.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.event.MsgRead;
import com.moor.im.common.event.NewMsgReceived;
import com.moor.im.common.event.SendMsg;
import com.moor.im.common.event.UnReadCount;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.NewMessage;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.Utils;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.discussion.parser.DiscussionParser;
import com.moor.im.options.group.parser.GroupParser;
import com.moor.im.options.message.activity.SystemMsgActivity;
import com.moor.im.options.message.adapter.MessageAdapter;
import com.moor.im.options.message.listener.OnItemClickListener;
import com.moor.im.options.message.listener.OnItemLongClickListener;
import com.moor.im.options.mobileassistant.MAActivity;
import com.moor.im.options.mobileassistant.model.MAAction;

import java.util.List;
import java.util.UUID;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by longwei on 2016/3/16.
 */
public class MessageFragment extends Fragment{

    private RecyclerView mRecycleView;
    private MessageAdapter mAdapter;
    private List<NewMessage> newMsgs;

    private CompositeSubscription _subscriptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, null);

        mRecycleView = (RecyclerView) view.findViewById(R.id.message_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(linearLayoutManager);
        mAdapter = new MessageAdapter(this);
        mRecycleView.setAdapter(mAdapter);

        updateMessage();

        _subscriptions = new CompositeSubscription();
        _subscriptions.add(RxBus.getInstance().toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof SendMsg) {
                            //更新列表数据
                            LogUtil.d("接收到发送消息的通知");
                            updateMessage();
                        }else if (event instanceof NewMsgReceived) {
                            LogUtil.d("接收到有新消息来的通知");
                            updateMessage();
                        }else if (event instanceof MsgRead) {
                            LogUtil.d("接收到消息被读的通知");
                            updateMessage();
                        }
                    }
                }));
        return view;
    }

    private void initListener() {
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View convertView, int position) {
                NewMessage newMessage = newMsgs.get(position);
                // 跳转到聊天页面
                Intent chatActivity = new Intent(getActivity(), ChatActivity.class);
                if("User".equals(newMessage.type)) {
                    chatActivity.putExtra("_id", newMessage.from);
                    chatActivity.putExtra("otherName", newMessage.fromName);
                    chatActivity.putExtra("type", newMessage.type);
                    getActivity().startActivity(chatActivity);
                }else if("Group".equals(newMessage.type)) {
                    chatActivity.putExtra("_id", newMessage.sessionId);
                    chatActivity.putExtra("otherName", newMessage.fromName);
                    chatActivity.putExtra("type", newMessage.type);
                    getActivity().startActivity(chatActivity);
                }else if("Discussion".equals(newMessage.type)) {
                    chatActivity.putExtra("_id", newMessage.sessionId);
                    chatActivity.putExtra("otherName", newMessage.fromName);
                    chatActivity.putExtra("type", newMessage.type);
                    getActivity().startActivity(chatActivity);
                }else if("System".equals(newMessage.type)) {
                    Intent systemActivity = new Intent(getActivity(), SystemMsgActivity.class);
                    startActivity(systemActivity);
                }else if("MA".equals(newMessage.type)) {
                    NewMessage maMsg = NewMessageDao.getInstance().getNewMsg("MA");
                    if(maMsg != null) {
                        maMsg.time = System.currentTimeMillis();
                        NewMessageDao.getInstance().updateMsg(maMsg);
                    }
                    Intent maActivity = new Intent(getActivity(), MAActivity.class);
                    startActivity(maActivity);
                }
            }
        });
        mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View convertView, int position) {
                //根据不同情况弹对话框
                NewMessage newMessage = newMsgs.get(position);
                openDialog(newMessage);
                return false;
            }
        });
    }

    public void updateMessage() {

        newMsgs = NewMessageDao.getInstance().queryMessage();

        for (int i=0; i<newMsgs.size(); i++) {
            if("User".equals(newMsgs.get(i).type)) {
                if("".equals(newMsgs.get(i).fromName)) {
                    String name = ContactsDao.getInstance().getContactsName(
                            newMsgs.get(i).from);
                    newMsgs.get(i).fromName = name;
                    NewMessageDao.getInstance().updateMsg(newMsgs.get(i));
                }
            }else if("Group".equals(newMsgs.get(i).type)) {
                if("".equals(newMsgs.get(i).fromName)) {
                    if(!"".equals(GroupParser.getInstance().getNameById(
                            newMsgs.get(i).sessionId))) {
                        String name = GroupParser.getInstance().getNameById(
                                newMsgs.get(i).sessionId);
                        newMsgs.get(i).fromName = name;
                        NewMessageDao.getInstance().updateMsg(newMsgs.get(i));
                    }

                }
            }else if("Discussion".equals(newMsgs.get(i).type)) {
                if("".equals(newMsgs.get(i).fromName)) {
                    if(!"".equals(DiscussionParser.getInstance().getNameById(
                            newMsgs.get(i).sessionId))) {
                        String name = DiscussionParser.getInstance().getNameById(
                                newMsgs.get(i).sessionId);
                        newMsgs.get(i).fromName = name;
                        NewMessageDao.getInstance().updateMsg(newMsgs.get(i));
                    }

                }
            }

        }

        newMsgs = NewMessageDao.getInstance().queryMessage();
        mAdapter.setDatas(newMsgs);
        mAdapter.notifyDataSetChanged();

        // 所有的未读消息
        int unReadCount = NewMessageDao.getInstance().getAllUnReadCount();
        if (unReadCount > 0 && unReadCount < 99) {

        } else if (unReadCount >= 99) {
            unReadCount = 99;
        } else {
            unReadCount = 0;
        }
        //通知主界面进行角标显示
        RxBus.getInstance().send(new UnReadCount(unReadCount));

        initListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(_subscriptions != null) {
            _subscriptions.unsubscribe();
        }
    }

    private void openDialog(final NewMessage msg) {
        LayoutInflater myInflater = LayoutInflater.from(getActivity());
        final View myDialogView = myInflater.inflate(R.layout.msg_list_dialog,
                null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setView(myDialogView);
        final AlertDialog alert = dialog.show();
        alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
        alert.getWindow().setGravity(Gravity.CENTER);

        // 置顶
        LinearLayout ll_zhiding = (LinearLayout) myDialogView
                .findViewById(R.id.ll_zhiding);
        final TextView tv_top = (TextView) myDialogView.findViewById(R.id.tv_zhiding);
        if(msg.isTop == 1) {
            tv_top.setText("取消置顶");
        }else {
            tv_top.setText("置顶");
        }
        ll_zhiding.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alert.dismiss();
                if(msg.isTop == 1) {
                    //取消置顶
                    msg.isTop = 0;
                }else {
                    msg.isTop = 1;
                }
                NewMessageDao.getInstance().updateMsg(msg);
                updateMessage();
            }
        });

        // 删除聊天
        LinearLayout ll_delete_msg = (LinearLayout) myDialogView
                .findViewById(R.id.ll_delete_msg);
        if("MA".equals(msg.type)) {
            ll_delete_msg.setVisibility(View.GONE);
        }
        ll_delete_msg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alert.dismiss();
                NewMessageDao.getInstance().deleteMsgById(msg.sessionId);
                updateMessage();
            }
        });
    }

}
