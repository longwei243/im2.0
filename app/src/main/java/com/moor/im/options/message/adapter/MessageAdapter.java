package com.moor.im.options.message.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.db.dao.ContactsDao;
import com.moor.im.common.model.NewMessage;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.common.utils.Utils;
import com.moor.im.options.discussion.parser.DiscussionParser;
import com.moor.im.options.group.parser.GroupParser;
import com.moor.im.options.message.fragment.MessageFragment;
import com.moor.im.options.message.listener.OnItemClickListener;
import com.moor.im.options.message.listener.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2016/4/19.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    private List<NewMessage> datas;
    private MessageFragment context;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MessageAdapter(MessageFragment context) {
        datas = new ArrayList<>();
        this.context = context;
    }

    public void setDatas(List<NewMessage> datas) {
        if(datas == null) {
            return;
        }
        this.datas.clear();
        this.datas.addAll(datas);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_message_list_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NewMessage message = datas.get(position);

        if("User".equals(message.type)) {
            String im_icon = ContactsDao.getInstance().getContactsIcon(message.from);
            if(!"".equals(im_icon) && im_icon != null) {
                Glide.with(context).load(im_icon + M7Constant.QINIU_IMG_ICON).asBitmap().placeholder(R.drawable.img_default_head).into(holder.img);
            }else {
                Glide.with(context).load(R.drawable.img_default_head).asBitmap().into(holder.img);
            }
        }else if("Group".equals(message.type)) {
            Glide.with(context).load(R.drawable.ic_addfriend_group).asBitmap().into(holder.img);
        }else if("Discussion".equals(message.type)) {
            Glide.with(context).load(R.drawable.ic_addfriend_discuss).asBitmap().into(holder.img);
        }else if("System".equals(message.type)) {
            Glide.with(context).load(R.drawable.ic_launcher).asBitmap().into(holder.img);
        }else if("MA".equals(message.type)) {
            Glide.with(context).load(R.drawable.ic_launcher).asBitmap().into(holder.img);
        }

        // 半角符转全角符
        String content = Utils.ToDBC(message.message);

        if("User".equals(message.type)) {
            if(message.fromName != null && !"".equals(message.fromName)) {
                holder.name.setText(message.fromName);
            }
            if("0".equals(message.msgType)) {
                holder.content.setText(content);
            }else if("2".equals(message.msgType)) {
                holder.content.setText("[语音]");
            }else if("1".equals(message.msgType)) {
                holder.content.setText("[图片]");
            }
        }else if("Group".equals(message.type)) {
            if(message.fromName != null && !"".equals(message.fromName)) {
                holder.name.setText(message.fromName);
            }else {
                if(message.sessionId != null && !"".equals(message.sessionId)) {
                    holder.name.setText(GroupParser.getInstance().getNameById(message.sessionId));
                }

            }
            String name = "";
            if(ContactsDao.getInstance().getContactsName(message.from) != null) {
                name = ContactsDao.getInstance().getContactsName(message.from);
            }
            if("0".equals(message.msgType)) {
                if(!"".equals(name)) {
                    holder.content.setText(name+":"+content);
                }else {
                    holder.content.setText(content);
                }

            }else if("2".equals(message.msgType)) {
                if(!"".equals(name)) {
                    holder.content.setText(name+":"+"[语音]");
                }else {
                    holder.content.setText("[语音]");
                }
            }else if("1".equals(message.msgType)) {
                if(!"".equals(name)) {
                    holder.content.setText(name+":"+"[图片]");
                }else {
                    holder.content.setText("[图片]");
                }
            }
        }else if("Discussion".equals(message.type)) {
            if(message.fromName != null && !"".equals(message.fromName)) {
                holder.name.setText(message.fromName);
            }else {
                if(message.sessionId != null && !"".equals(message.sessionId)) {
                    holder.name.setText(DiscussionParser.getInstance().getNameById(message.sessionId));
                }

            }
            String name = "";
            if(ContactsDao.getInstance().getContactsName(message.from) != null) {
                name = ContactsDao.getInstance().getContactsName(message.from);
            }
            if("0".equals(message.msgType)) {
                if(!"".equals(name)) {
                    holder.content.setText(name+":"+content);
                }else {
                    holder.content.setText(content);
                }

            }else if("2".equals(message.msgType)) {
                if(!"".equals(name)) {
                    holder.content.setText(name+":"+"[语音]");
                }else {
                    holder.content.setText("[语音]");
                }
            }else if("1".equals(message.msgType)) {
                if(!"".equals(name)) {
                    holder.content.setText(name+":"+"[图片]");
                }else {
                    holder.content.setText("[图片]");
                }
            }
        }else if("System".equals(message.type)) {
            holder.name.setText("系统通知");
            holder.content.setText(content);
        }else if("MA".equals(message.type)) {
            holder.name.setText("客服助手");
            holder.content.setText(content);
        }

        holder.time.setText(TimeUtil.convertTimeToFriendly(message.time));

        if (message.unReadCount == 0) {
            holder.unreadcount.setVisibility(View.GONE);
        } else if (message.unReadCount >= 99) {
            holder.unreadcount.setVisibility(View.VISIBLE);
            holder.unreadcount.setText(99 + "");
        } else {
            holder.unreadcount.setVisibility(View.VISIBLE);
            holder.unreadcount.setText(message.unReadCount + "");
        }
        if(message.isTop == 1) {
            holder.message_ll.setBackgroundResource(R.drawable.bg_top_section_item);
        }else {
            holder.message_ll.setBackgroundResource(R.drawable.bg_image_section_item);
        }
        holder.setOnItemClickListener(this.onItemClickListener, position);
        holder.setOnItemLongClickListener(this.onItemLongClickListener, position);

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout message_ll;
        public ImageView img;
        public TextView name;
        public TextView time;
        public TextView content;
        public TextView unreadcount;

        public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener, final int position) {
            if(onItemLongClickListener != null) {
                this.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.onItemClick(view, position);
                    }
                });
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener, final int position) {
            if(onItemClickListener != null) {
                this.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return onItemLongClickListener.onItemLongClick(view, position);
                    }
                });
            }
        }

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.message_icon);
            name = (TextView) itemView.findViewById(R.id.message_people_name);
            time = (TextView) itemView.findViewById(R.id.message_time);
            content = (TextView) itemView.findViewById(R.id.message_content);
            unreadcount = (TextView) itemView.findViewById(R.id.message_unreadcount);
            message_ll = (LinearLayout) itemView.findViewById(R.id.message_ll);
        }
    }

}
