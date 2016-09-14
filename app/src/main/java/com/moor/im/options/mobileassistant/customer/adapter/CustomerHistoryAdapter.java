package com.moor.im.options.mobileassistant.customer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.views.roundimage.RoundImageView;
import com.moor.im.options.mobileassistant.customer.model.CustomerHistory;
import com.moor.im.options.mobileassistant.customer.model.CustomerHistoryData;

import java.util.List;

/**
 * Created by longwei on 16/8/25.
 */
public class CustomerHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<CustomerHistory> datas;

    public CustomerHistoryAdapter(Context context, List<CustomerHistory> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CallInViewHolder(LayoutInflater.from(context).inflate(R.layout.customer_history_item_callin, null));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CustomerHistory chd = datas.get(position);

        ((CallInViewHolder)holder).customer_detail_tv_action.setText(chd.action);
        ((CallInViewHolder)holder).customer_detail_tv_time.setText(chd.time);
        ((CallInViewHolder)holder).customer_detail_tv_status.setText(chd.status);

        ((CallInViewHolder)holder).customer_history_item_tv_date.setText(chd.date);

        if(chd.comment != null && !"".equals(chd.comment)) {
            ((CallInViewHolder)holder).customer_history_ll_comment.setVisibility(View.VISIBLE);
            ((CallInViewHolder)holder).customer_detail_tv_comment.setText(chd.comment);
        }else {
            ((CallInViewHolder)holder).customer_history_ll_comment.setVisibility(View.GONE);
        }


        if(chd.typeCode == CustomerHistoryData.TYPE_NOTE) {
            ((CallInViewHolder)holder).customer_history_item_iv_type.setImageResource(R.drawable.customer_history_note);
        }else if(chd.typeCode == CustomerHistoryData.TYPE_BUSSINESS) {
            ((CallInViewHolder)holder).customer_history_item_iv_type.setImageResource(R.drawable.customer_history_erp);
        }else if(chd.typeCode == CustomerHistoryData.TYPE_CALL_IN) {
            ((CallInViewHolder)holder).customer_history_item_iv_type.setImageResource(R.drawable.customer_history_callin);
        }else if(chd.typeCode == CustomerHistoryData.TYPE_CALL_OUT) {
            ((CallInViewHolder)holder).customer_history_item_iv_type.setImageResource(R.drawable.customer_history_callout);
        }else if(chd.typeCode == CustomerHistoryData.TYPE_CHAT) {
            ((CallInViewHolder)holder).customer_history_item_iv_type.setImageResource(R.drawable.customer_history_weixin);
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    private class CallInViewHolder extends RecyclerView.ViewHolder {

        TextView customer_detail_tv_action, customer_detail_tv_time,
                customer_detail_tv_status, customer_history_item_tv_date,
                customer_detail_tv_comment;

        RoundImageView customer_history_item_iv_type;
        LinearLayout customer_history_ll_comment;
        public CallInViewHolder(View itemView) {
            super(itemView);
            customer_detail_tv_action = (TextView) itemView.findViewById(R.id.customer_detail_tv_action);
            customer_detail_tv_time = (TextView) itemView.findViewById(R.id.customer_detail_tv_time);
            customer_detail_tv_status = (TextView) itemView.findViewById(R.id.customer_detail_tv_status);
            customer_history_item_tv_date = (TextView) itemView.findViewById(R.id.customer_history_item_tv_date);
            customer_detail_tv_comment = (TextView) itemView.findViewById(R.id.customer_detail_tv_comment);

            customer_history_item_iv_type = (RoundImageView) itemView.findViewById(R.id.customer_history_item_iv_type);
            customer_history_ll_comment = (LinearLayout) itemView.findViewById(R.id.customer_history_ll_comment);
        }
    }
}
