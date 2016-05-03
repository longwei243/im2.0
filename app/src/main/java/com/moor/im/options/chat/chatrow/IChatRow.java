package com.moor.im.options.chat.chatrow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.moor.im.common.model.FromToMessage;
import com.moor.im.options.chat.holder.BaseHolder;

/**
 * Created by longwei on 2016/3/9.
 */
public interface IChatRow {


    View buildChatView(LayoutInflater inflater, View convertView);


    void buildChattingBaseData(Context context, BaseHolder baseHolder, FromToMessage detail, int position);


    int getChatViewType();
}
