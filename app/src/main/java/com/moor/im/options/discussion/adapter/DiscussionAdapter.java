package com.moor.im.options.discussion.adapter;

import android.text.TextUtils;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.model.Discussion;
import com.moor.im.common.model.Group;
import com.moor.im.common.views.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;

/**
 * Created by longwei on 2016/4/25.
 */
public class DiscussionAdapter extends EasyRecyclerViewAdapter{

    @Override
    public int[] getItemLayouts() {
        return new int[]{R.layout.discussion_list_item};
    }

    @Override
    public void onBindRecycleViewHolder(EasyRecyclerViewHolder viewHolder, int position) {
        Discussion discussion = getItem(position);
        TextView discussion_item_tv_name = viewHolder.findViewById(R.id.discussion_item_tv_name);

        if(!TextUtils.isEmpty(discussion.title)) {
            discussion_item_tv_name.setText(discussion.title);
        }
    }

    @Override
    public int getRecycleViewItemType(int position) {
        return 0;
    }
}
