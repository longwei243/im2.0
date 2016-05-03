package com.moor.im.options.chat.listener;

import android.view.View;

import com.moor.im.common.model.FromToMessage;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.chat.adapter.ChatAdapter;
import com.moor.im.options.chat.holder.ViewHolderTag;
import com.moor.im.options.chat.utils.MediaPlayTools;

/**
 * Created by longwei on 2016/3/10.
 */
public class ChatListClickListener implements View.OnClickListener{

    /**聊天界面*/
    private ChatActivity mContext;

    public ChatListClickListener(ChatActivity activity , String userName) {
        mContext = activity;
    }
    @Override
    public void onClick(View v) {
        ViewHolderTag holder = (ViewHolderTag) v.getTag();
        FromToMessage iMessage = holder.detail;

        switch (holder.type) {
            case ViewHolderTag.TagType.TAG_RESEND_MSG:
                mContext.resendMsg(iMessage, holder.position);
                break;
            case ViewHolderTag.TagType.TAG_VOICE:
                if(iMessage == null) {
                    return ;
                }
                MediaPlayTools instance = MediaPlayTools.getInstance();
                final ChatAdapter adapterForce = mContext.getChatAdapter();
                if(instance.isPlaying()) {
                    instance.stop();
                }
                if(adapterForce.mVoicePosition == holder.position) {
                    adapterForce.mVoicePosition = -1;
                    adapterForce.notifyDataSetChanged();
                    return ;
                }

                instance.setOnVoicePlayCompletionListener(new MediaPlayTools.OnVoicePlayCompletionListener() {

                    @Override
                    public void OnVoicePlayCompletion() {
                        adapterForce.mVoicePosition = -1;
                        adapterForce.notifyDataSetChanged();
                    }
                });
                String fileLocalPath = holder.detail.filePath;
                instance.playVoice(fileLocalPath, false);
                adapterForce.setVoicePosition(holder.position);
                adapterForce.notifyDataSetChanged();

                break;
        }
    }
}
