package com.moor.im.options.mobileassistant.report;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.constant.M7Constant;
import com.moor.im.common.model.Contacts;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.views.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.moor.im.options.mobileassistant.model.MAAgent;

/**
 * Created by longwei on 16/8/16.
 */
public class AgentSelectedAdapter extends EasyRecyclerViewAdapter {

    @Override
    public int[] getItemLayouts() {
        return new int[]{R.layout.item_report_selected_agent};
    }

    @Override
    public void onBindRecycleViewHolder(EasyRecyclerViewHolder viewHolder, int position) {
        MAAgent agent = getItem(position);
        TextView contact_item_tv_name = viewHolder.findViewById(R.id.section_name_tv);

        if(!TextUtils.isEmpty(agent.displayName)) {
            contact_item_tv_name.setText(agent.displayName);
        }

    }

    @Override
    public int getRecycleViewItemType(int position) {
        return 0;
    }
}

