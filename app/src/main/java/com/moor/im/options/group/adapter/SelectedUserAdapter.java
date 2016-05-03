package com.moor.im.options.group.adapter;

import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.views.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;

/**
 * Created by longwei on 2016/4/25.
 */
public class SelectedUserAdapter extends EasyRecyclerViewAdapter{

    @Override
    public int[] getItemLayouts() {
        return new int[]{R.layout.item_selected_user};
    }

    @Override
    public void onBindRecycleViewHolder(EasyRecyclerViewHolder viewHolder, int position) {
        Contacts contacts = getItem(position);
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

    }

    @Override
    public int getRecycleViewItemType(int position) {
        return 0;
    }
}
