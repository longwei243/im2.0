package com.moor.im.options.mobileassistant.report;

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
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.GlideUtils;
import com.moor.im.common.views.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.moor.im.common.views.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.moor.im.options.mobileassistant.model.MAAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 16/8/16.
 */
public class AllAgentAdapter extends EasyRecyclerViewAdapter {
    private List<Integer> checkPositionList = new ArrayList<>();

    @Override
    public int[] getItemLayouts() {
        return new int[]{R.layout.item_report_all_agent};
    }

    @Override
    public void onBindRecycleViewHolder(final EasyRecyclerViewHolder viewHolder, final int position) {
        final MAAgent agent = getItem(position);
//        TextView contact_item_tv_name = viewHolder.findViewById(R.id.item_report_cb_all_agent);
        final CheckBox section_cb = viewHolder.findViewById(R.id.item_report_cb_all_agent);
        section_cb.setTag(new Integer(position));

        if(!TextUtils.isEmpty(agent.displayName)) {
            section_cb.setText(agent.displayName+"["+agent.exten+"]");
        }

        section_cb.setChecked(checkPositionList.contains(new Integer(position)));
        for(int i=0; i<checkPositionList.size(); i++) {
            if(checkPositionList.contains(new Integer(position))) {
                section_cb.setBackground(viewHolder.itemView.getResources().getDrawable(R.drawable.bg_cb_checked));
            }else {
                section_cb.setBackground(viewHolder.itemView.getResources().getDrawable(R.drawable.bg_cb));
            }
        }
        section_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    //被选中
                    if(checkPositionList.size() == 4) {
                        if((Integer)section_cb.getTag() == position) {
                            section_cb.setChecked(false);
                        }
                    }else {
                        if(!checkPositionList.contains(section_cb.getTag())) {
                            section_cb.setBackground(viewHolder.itemView.getResources().getDrawable(R.drawable.bg_cb_checked));
                            checkPositionList.add(new Integer(position));
                            RxBus.getInstance().send(new AgentChecked(agent));
                        }
                    }

                }else {
                    if(checkPositionList.contains(section_cb.getTag())) {
                        section_cb.setBackground(viewHolder.itemView.getResources().getDrawable(R.drawable.bg_cb));
                        checkPositionList.remove(new Integer(position));
                        RxBus.getInstance().send(new AgentUnCheck(agent));
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
