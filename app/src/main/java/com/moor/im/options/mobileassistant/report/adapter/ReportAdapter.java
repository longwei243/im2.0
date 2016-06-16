package com.moor.im.options.mobileassistant.report.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moor.im.R;

import java.util.List;

/**
 * Created by longwei on 2016/6/16.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.DragViewHolder> implements OnItemMoveListener{

    private Context context;
    private List<String> datas;

    public ReportAdapter(Context context, List<String> datas) {
        this.context = context;
        this.datas = datas;
    }


    @Override
    public DragViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DragViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item, null));
    }

    @Override
    public void onBindViewHolder(DragViewHolder holder, int position) {
//        holder.tv.setText(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        String item = datas.get(fromPosition);
        datas.remove(fromPosition);
        datas.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    class DragViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;

        public DragViewHolder(View itemView) {
            super(itemView);
//            tv = (TextView) itemView.findViewById(R.id.report_tv_test);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemFinish() {
            itemView.setBackgroundResource(R.drawable.workbanch_item_bg);
        }
    }
}
