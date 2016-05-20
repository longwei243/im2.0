package com.moor.im.options.chat.listener;

import android.view.View;

import com.moor.im.common.model.FromToMessage;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.chat.holder.ViewHolderTag;

/**
 * Created by longwei on 2016/5/19.
 */
public class ChatListLongClickListener implements View.OnLongClickListener{
    /**聊天界面*/
    private ChatActivity mContext;
    public ChatListLongClickListener(ChatActivity activity) {
        mContext = activity;
    }
    @Override
    public boolean onLongClick(View view) {
        ViewHolderTag holder = (ViewHolderTag) view.getTag();
        FromToMessage iMessage = holder.detail;
        mContext.itemLongClick(iMessage);
        return false;
    }
}
