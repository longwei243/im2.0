package com.moor.im.options.chat.chatrow;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.model.FromToMessage;
import com.moor.im.common.model.User;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.options.chat.activity.ChatActivity;
import com.moor.im.options.chat.holder.BaseHolder;
import com.moor.im.options.chat.holder.TextViewHolder;
import com.moor.im.options.chat.utils.AnimatedGifDrawable;
import com.moor.im.options.chat.utils.AnimatedImageSpan;
import com.moor.im.options.chat.utils.FaceConversionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by longwei on 2016/3/9.
 */
public class TextTxChatRow extends BaseChatRow{

    private Context context;

    public TextTxChatRow(int type) {
        super(type);
    }

    @Override
    public boolean onCreateRowContextMenu(ContextMenu contextMenu, View targetView, FromToMessage detail) {
        return false;
    }

    @Override
    protected void buildChattingData(Context context, BaseHolder baseHolder, FromToMessage detail, int position) {
        this.context = context;
        TextViewHolder holder = (TextViewHolder) baseHolder;
        FromToMessage message = detail;
        if(message != null) {

            SpannableStringBuilder content = handler(holder.getDescTextView(),
                    message.message);
            SpannableString spannableString = FaceConversionUtil.getInstace()
                    .getExpressionString(context, content + "", holder.getDescTextView());
            holder.getDescTextView().setText(spannableString);
//            String im_icon = user.im_icon;
//            if(im_icon != null && !"".equals(im_icon)) {
//                GlideUtils.displayNet(holder.getChatting_avatar_iv(), im_icon);
//            }

            View.OnClickListener listener = ((ChatActivity)context).getChatAdapter().getOnClickListener();
            getMsgStateResId(position, holder, message, listener);
        }
    }

    @Override
    public View buildChatView(LayoutInflater inflater, View convertView) {

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.chat_row_text_tx, null);
            TextViewHolder holder = new TextViewHolder(mRowType);
            convertView.setTag(holder.initBaseHolder(convertView, false));
        }

        return convertView;
    }

    @Override
    public int getChatViewType() {
        return ChatRowType.TEXT_ROW_TRANSMIT.ordinal();
    }

    private SpannableStringBuilder handler(final TextView gifTextView,
                                           String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String tempText = m.group();
            try {
                String num = tempText.substring(
                        "#[face/png/f_static_".length(), tempText.length()
                                - ".png]#".length());
                String gif = "face/gif/f" + num + ".gif";
                /**
                 * 如果open这里不抛异常说明存在gif，则显示对应的gif 否则说明gif找不到，则显示png
                 * */
                InputStream is = context.getAssets().open(gif);
                sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is,
                                new AnimatedGifDrawable.UpdateListener() {
                                    @Override
                                    public void update() {
                                        gifTextView.postInvalidate();
                                    }
                                })), m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                is.close();
            } catch (Exception e) {
                String png = tempText.substring("#[".length(),
                        tempText.length() - "]#".length());
                try {
                    sb.setSpan(
                            new ImageSpan(context,
                                    BitmapFactory.decodeStream(context
                                            .getAssets().open(png))),
                            m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return sb;
    }
}
