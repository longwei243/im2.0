package com.moor.im.options.message.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.common.db.dao.MessageDao;
import com.moor.im.common.db.dao.NewMessageDao;
import com.moor.im.common.event.MsgRead;
import com.moor.im.common.event.NewMsgReceived;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.options.base.BaseActivity;
import com.moor.im.options.chat.view.ChatListView;
import com.moor.im.options.message.adapter.SystemListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class SystemMsgActivity extends BaseActivity implements ChatListView.OnRefreshListener {

    private ChatListView mChatList;

    private SystemListAdapter adapter;

    List<FromToMessage> fromToMessage;

    private List<FromToMessage> descFromToMessage = new ArrayList<FromToMessage>();

    ArrayList<FromToMessage> list = new ArrayList<FromToMessage>();

    private int i = 2;

    private Boolean JZflag = true;
    private View header;// 加载更多头
    private int height;

    ImageView title_back;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                // 加载更多的时候
                JZMoreMessage();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);
        TextView titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_name.setText("系统消息");
        findViewById(R.id.titlebar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mChatList = (ChatListView) findViewById(R.id.system_list);
        header = View.inflate(this, R.layout.chatlist_header, null);
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        header.measure(w, h);
        height = header.getMeasuredHeight();
        fromToMessage = MessageDao.getInstance().getOneMessage("System", 1);
        descFromToMessage.clear();
        for (int i = fromToMessage.size() - 1; i >= 0; i--) {
            descFromToMessage.add(fromToMessage.get(i));
        }
        if (MessageDao.getInstance().isReachEndMessage(
                descFromToMessage.size(), "System")) {
            mChatList.dismiss();
        }
        list.addAll(descFromToMessage);
        adapter = new SystemListAdapter(SystemMsgActivity.this, list);

        mChatList.setAdapter(adapter);



        NewMessageDao.getInstance().updateUnReadCount("System");
        RxBus.getInstance().send(new MsgRead());

    }

    // 分页加载更多
    public void JZMoreMessage() {
        fromToMessage = MessageDao.getInstance().getOneMessage("System", i);
        descFromToMessage.clear();
        for (int i = fromToMessage.size() - 1; i >= 0; i--) {
            descFromToMessage.add(fromToMessage.get(i));
        }

        list = new ArrayList<FromToMessage>();
        list.clear();
        list.addAll(descFromToMessage);
        mChatList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (mChatList.getHeaderViewsCount() > 0) {
            mChatList.removeHeaderView(header);
        }

        // 是否有数据
        if (MessageDao.getInstance().isReachEndMessage(
                descFromToMessage.size(), "System")) {
            mChatList.setSelectionFromTop(fromToMessage.size() - (i - 1) * 15,
                    height);
            mChatList.dismiss();
        } else {
            mChatList.setSelectionFromTop(fromToMessage.size() - (i - 1) * 15
                    + 1, height);
        }

        mChatList.onRefreshFinished();
        JZflag = true;
        i++;

    }

    @Override
    public void toRefresh() {
        // TODO Auto-generated method stub
        if (JZflag == true) {
            JZflag = false;
            new Thread() {
                public void run() {
                    try {
                        sleep(800);
                        Message msg = new Message();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                };
            }.start();
        }
    }
}
