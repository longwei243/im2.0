package com.moor.im.options.message.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moor.im.R;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.event.MsgRead;
import com.moor.im.common.event.NewMsgReceived;
import com.moor.im.common.event.SendMsg;
import com.moor.im.common.event.UnReadCount;
import com.moor.im.common.model.NewMessage;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.discussion.parser.DiscussionParser;
import com.moor.im.options.group.parser.GroupParser;
import com.moor.im.options.message.adapter.MessageAdapter;
import com.moor.im.options.message.listener.OnItemClickListener;
import com.moor.im.options.message.listener.OnItemLongClickListener;

import java.util.List;

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
//                    Intent systemActivity = new Intent(getActivity(), SystemActivity.class);
//                    startActivity(systemActivity);
                }
            }
        });
        mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View convertView, int position) {

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
        LogUtil.d("新消息数量是："+newMsgs.size());
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
        _subscriptions.unsubscribe();
    }
}
