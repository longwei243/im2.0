package com.moor.im.options.group.adapter;

import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.event.UserChecked;
import com.moor.im.common.event.UserUnCheck;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.model.Group;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.views.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2016/4/25.
 */
public class AllUserAdapter extends EasyRecyclerViewAdapter{

    private List<Integer> checkPositionList = new ArrayList<>();

    @Override
    public int[] getItemLayouts() {
        return new int[]{R.layout.item_all_user};
    }

    @Override
    public void onBindRecycleViewHolder(EasyRecyclerViewHolder viewHolder, final int position) {
        final Contacts contacts = getItem(position);
        TextView contact_item_tv_name = viewHolder.findViewById(R.id.section_name_tv);

        if(!TextUtils.isEmpty(contacts.displayName)) {
            contact_item_tv_name.setText(contacts.displayName);
        }
        ImageView sectionIv = viewHolder.findViewById(R.id.section_iv);
        if (contacts.im_icon != null && !"".equals(contacts.im_icon)) {
            GlideUtils.displayNet(sectionIv, contacts.im_icon + M7Constant.QINIU_IMG_ICON);
        } else {
            GlideUtils.displayNative(sectionIv, R.drawable.img_default_head);
        }

        final CheckBox section_cb = viewHolder.findViewById(R.id.section_cb);
        section_cb.setTag(new Integer(position));

        section_cb.setChecked(checkPositionList.contains(new Integer(position)));
        section_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    //被选中
                    if(!checkPositionList.contains(section_cb.getTag())) {
                        checkPositionList.add(new Integer(position));
                        RxBus.getInstance().send(new UserChecked(contacts));
                    }

                }else {
                    if(checkPositionList.contains(section_cb.getTag())) {
                        checkPositionList.remove(new Integer(position));
                        RxBus.getInstance().send(new UserUnCheck(contacts));
                    }

                }
            }
        });
    }

    @Override
    public int getRecycleViewItemType(int position) {
        return 0;
    }
}
