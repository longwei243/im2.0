package com.moor.im.options.mobileassistant.report.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.moor.im.R;
import com.moor.im.options.mobileassistant.report.model.ReportData;

import java.util.List;

/**
 * Created by longwei on 2016/6/16.
 */
public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener{

    private Context context;
    private List<ReportData> datas;

    public ReportAdapter(Context context, List<ReportData> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int getItemViewType(int position) {
        return datas.get(position).type;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ReportData.TYPE_CALL_IN) {
            return new CallInViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item_callin, null));
        }else if(viewType == ReportData.TYPE_CALL_OUT) {
            return new CallOutViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item, null));
        }else if(viewType == ReportData.TYPE_QUEUE) {
            return new QueueViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item, null));
        }else if(viewType == ReportData.TYPE_IM) {
            return new IMViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item, null));
        }else if(viewType == ReportData.TYPE_SESSION) {
            return new SessionViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item, null));
        }else if(viewType == ReportData.TYPE_CUSTOMER) {
            return new CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item, null));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ReportData reportData = datas.get(position);
        if(datas.get(position).type == ReportData.TYPE_CALL_IN) {
            ((CallInViewHolder)holder).tv.setText(reportData.name);
//            ((CallInViewHolder)holder).report_item_callin_linechart.setData();
        }else if(datas.get(position).type == ReportData.TYPE_CALL_OUT) {
            ((CallOutViewHolder)holder).tv.setText(reportData.name);
        }else if(datas.get(position).type == ReportData.TYPE_QUEUE) {
            ((QueueViewHolder)holder).tv.setText(reportData.name);
        }else if(datas.get(position).type == ReportData.TYPE_IM) {
            ((IMViewHolder)holder).tv.setText(reportData.name);
        }else if(datas.get(position).type == ReportData.TYPE_SESSION) {
            ((SessionViewHolder)holder).tv.setText(reportData.name);
        }else if(datas.get(position).type == ReportData.TYPE_CUSTOMER) {
            ((CustomerViewHolder)holder).tv.setText(reportData.name);
        }

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        ReportData item = datas.get(fromPosition);
        datas.remove(fromPosition);
        datas.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }




    class CallInViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;
        LineChart report_item_callin_linechart;

        public CallInViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_callin_linechart = (LineChart) itemView.findViewById(R.id.report_item_callin_linechart);
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
    class CallOutViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;

        public CallOutViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
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
    class QueueViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;

        public QueueViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
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
    class IMViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;

        public IMViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
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
    class SessionViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;

        public SessionViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
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
    class CustomerViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
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
