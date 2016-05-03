package com.moor.im.options.group.adapter;

import android.text.TextUtils;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.model.Group;
import com.moor.im.common.views.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;

/**
 * Created by longwei on 2016/4/25.
 */
public class GroupAdapter extends EasyRecyclerViewAdapter{

    @Override
    public int[] getItemLayouts() {
        return new int[]{R.layout.group_list_item};
    }

    @Override
    public void onBindRecycleViewHolder(EasyRecyclerViewHolder viewHolder, int position) {
        Group group = getItem(position);
        TextView group_item_tv_name = viewHolder.findViewById(R.id.group_item_tv_name);

        if(!TextUtils.isEmpty(group.title)) {
            group_item_tv_name.setText(group.title);
        }
    }

    @Override
    public int getRecycleViewItemType(int position) {
        return 0;
    }
}
