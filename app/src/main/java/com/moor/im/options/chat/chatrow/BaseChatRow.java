package com.moor.im.options.chat.chatrow;

import android.content.Context;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.options.chat.holder.BaseHolder;
import com.moor.im.options.chat.holder.ViewHolderTag;


/**
 * Created by longwei on 2016/3/9.
 * 处理基本的姓名和头像显示，消息发送状态，这些都是相同的
 */
public abstract class BaseChatRow implements IChatRow{

    int mRowType;
    User user = UserDao.getInstance().getUser();
    String im_icon;

    public BaseChatRow(int type) {
        mRowType = type;
    }

    /**
     * 处理消息的发送状态设置
     * @param position 消息的列表所在位置
     * @param holder 消息ViewHolder
     * @param l
     */
    protected static void getMsgStateResId(int position , BaseHolder holder , FromToMessage msg , View.OnClickListener l){
        if(msg != null && msg.userType.equals("0")) {
            String msgStatus = msg.sendState;
            if(msgStatus.equals("false")) {
                holder.getUploadState().setImageResource(R.drawable.chat_failure_msgs);
                holder.getUploadState().setVisibility(View.VISIBLE);
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
            } else  if (msgStatus.equals("true")) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }

            } else  if (msgStatus.equals("sending")) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.VISIBLE);
                }

            } else {
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
            }

            ViewHolderTag holderTag = ViewHolderTag.createTag(msg, ViewHolderTag.TagType.TAG_RESEND_MSG , position);
            holder.getUploadState().setTag(holderTag);
            holder.getUploadState().setOnClickListener(l);
        }
    }

    /**
     *
     * @param contextMenu
     * @param targetView
     * @param detail
     * @return
     */
    public abstract boolean onCreateRowContextMenu(ContextMenu contextMenu , View targetView , FromToMessage detail);

    /**
     * 填充数据
     * @param context
     * @param baseHolder
     * @param detail
     * @param position
     */
    protected abstract void buildChattingData(Context context , BaseHolder baseHolder , FromToMessage detail , int position);

    @Override
    public void buildChattingBaseData(Context context, BaseHolder baseHolder, FromToMessage detail, int position) {

        // 处理其他逻辑
        buildChattingData(context, baseHolder, detail, position);
        //设置姓名和头像
        setPhotoAndName(baseHolder, detail);
    }
    private void setPhotoAndName(BaseHolder baseHolder, FromToMessage message) {
        if(baseHolder.getChattingAvatar() != null) {
            if("0".equals(message.userType)) {
                if(user.im_icon != null && !"".equals(user.im_icon)) {
                    GlideUtils.displayNet(baseHolder.getChattingAvatar(), user.im_icon+ M7Constant.QINIU_IMG_ICON);
                }
            }else {
                if("User".equals(message.type)) {
                    if(!"".equals(im_icon)) {
                        GlideUtils.displayNet(baseHolder.getChattingAvatar(), im_icon+ M7Constant.QINIU_IMG_ICON);
                    }
                }else if("Group".equals(message.type)) {
                    if("System".equals(message.from)) {

                    }else {
                        if(message.from != null && !"".equals(message.from)) {
                            baseHolder.getChattingUser().setVisibility(View.VISIBLE);
                            baseHolder.getChattingUser().setText(ContactsDao.getInstance().getContactsName(message.from));
                            String imicon = ContactsDao.getInstance().getContactsIcon(message.from);
                            GlideUtils.displayNet(baseHolder.getChattingAvatar(), imicon+ M7Constant.QINIU_IMG_ICON);
                        }

                    }
                }else if("Discussion".equals(message.type)) {
                    if("System".equals(message.from)) {

                    }else {
                        if(message.from != null && !"".equals(message.from)) {
                            baseHolder.getChattingUser().setVisibility(View.VISIBLE);
                            baseHolder.getChattingUser().setText(ContactsDao.getInstance().getContactsName(message.from));
                            String imicon = ContactsDao.getInstance().getContactsIcon(message.from);
                            GlideUtils.displayNet(baseHolder.getChattingAvatar(), imicon+ M7Constant.QINIU_IMG_ICON);
                        }

                    }
                }

            }
        }
    }

    public void setImIcon(String imicon) {
        this.im_icon = imicon;
    }

}
