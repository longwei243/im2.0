package com.moor.im.options.chat.chatrow;

import android.content.Context;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.MessageDao;
import com.moor.im.common.http.FileDownLoadListener;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.chat.holder.BaseHolder;
import com.moor.im.options.chat.holder.VoiceViewHolder;

import java.io.File;
import java.util.UUID;


/**
 * Created by longwei on 2016/3/9.
 */
public class VoiceRxChatRow extends BaseChatRow{

    public VoiceRxChatRow(int type) {
        super(type);
    }

    @Override
    public boolean onCreateRowContextMenu(ContextMenu contextMenu, View targetView, FromToMessage detail) {
        return false;
    }

    @Override
    protected void buildChattingData(final Context context, BaseHolder baseHolder, final FromToMessage detail, final int position) {
        final VoiceViewHolder holder = (VoiceViewHolder) baseHolder;
        final FromToMessage message = detail;
        if(message != null) {
            if(message.filePath == null || "".equals(message.filePath)) {
                //下载录音
                final String dirStr = Environment.getExternalStorageDirectory() + File.separator + M7Constant.IM_RECORD_FILE_DIR;
                File dir = new File(dirStr);
                if(!dir.exists()) {
                    dir.mkdirs();

                }
                File file = new File(dir, "7moor_record_"+ UUID.randomUUID()+".amr");
                if(file.exists()) {
                    file.delete();
                }
                HttpManager.getInstance().downloadFile(message.message, file, new FileDownLoadListener() {
                    @Override
                    public void onSuccess(File file) {
                        String filePath = file.getAbsolutePath();
                        message.filePath = filePath;
                        MessageDao.getInstance().updateMsgToDao(message);
                        VoiceViewHolder.initVoiceRow(holder, detail, position, (ChatActivity) context, true);
                    }

                    @Override
                    public void onFailed() {

                    }

                    @Override
                    public void onProgress(int progress) {

                    }
                });
            }else {
                VoiceViewHolder.initVoiceRow(holder, detail, position, (ChatActivity) context, true);
            }

        }
    }

    @Override
    public View buildChatView(LayoutInflater inflater, View convertView) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.chat_row_voice_rx, null);
            VoiceViewHolder holder = new VoiceViewHolder(mRowType);
            convertView.setTag(holder.initBaseHolder(convertView, true));
        }
        return convertView;
    }

    @Override
    public int getChatViewType() {
        return ChatRowType.VOICE_ROW_RECEIVED.ordinal();
    }
}
