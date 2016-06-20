package com.moor.im.options.mobileassistant.report.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.moor.im.R;
import com.moor.im.options.mobileassistant.report.model.ReportData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2016/6/16.
 */
public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener{

    private Context context;
    private List<ReportData> datas;

    private int[] mColors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2]
    };

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
            return new CallOutViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item_callout, null));
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

            ((CallInViewHolder)holder).report_item_callin_linechart.setDrawGridBackground(false);
            ((CallInViewHolder)holder).report_item_callin_linechart.setDescription("");
            ((CallInViewHolder)holder).report_item_callin_linechart.setDrawBorders(false);

            ((CallInViewHolder)holder).report_item_callin_linechart.getAxisLeft().setEnabled(false);
            ((CallInViewHolder)holder).report_item_callin_linechart.getAxisRight().setDrawAxisLine(false);
            ((CallInViewHolder)holder).report_item_callin_linechart.getAxisRight().setDrawGridLines(false);
            ((CallInViewHolder)holder).report_item_callin_linechart.getXAxis().setDrawAxisLine(false);
            ((CallInViewHolder)holder).report_item_callin_linechart.getXAxis().setDrawGridLines(false);

            // enable touch gestures
            ((CallInViewHolder)holder).report_item_callin_linechart.setTouchEnabled(true);

            // enable scaling and dragging
            ((CallInViewHolder)holder).report_item_callin_linechart.setDragEnabled(true);
            ((CallInViewHolder)holder).report_item_callin_linechart.setScaleEnabled(false);

            // if disabled, scaling can be done on x- and y-axis separately
            ((CallInViewHolder)holder).report_item_callin_linechart.setPinchZoom(false);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < 8; i++) {
                xVals.add((i) + "");
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            for (int z = 0; z < 2; z++) {

                ArrayList<Entry> values = new ArrayList<Entry>();

                for (int i = 0; i < 8; i++) {
                    double val = (Math.random() * 8) + 3;
                    values.add(new Entry((float) val, i));
                }

                LineDataSet d = new LineDataSet(values, "DataSet " + (z + 1));
                d.setLineWidth(2.5f);
                d.setCircleRadius(4f);

                int color = mColors[z % mColors.length];
                d.setColor(color);
                d.setCircleColor(color);
                dataSets.add(d);
            }

            LineData data = new LineData(xVals, dataSets);
            ((CallInViewHolder)holder).report_item_callin_linechart.setData(data);
            ((CallInViewHolder)holder).report_item_callin_linechart.invalidate();






        }else if(datas.get(position).type == ReportData.TYPE_CALL_OUT) {
            ((CallOutViewHolder)holder).tv.setText(reportData.name);


            ((CallOutViewHolder)holder).report_item_callout_linechart.setDrawGridBackground(false);
            ((CallOutViewHolder)holder).report_item_callout_linechart.setDescription("");
            ((CallOutViewHolder)holder).report_item_callout_linechart.setDrawBorders(false);

            ((CallOutViewHolder)holder).report_item_callout_linechart.getAxisLeft().setEnabled(false);
            ((CallOutViewHolder)holder).report_item_callout_linechart.getAxisRight().setDrawAxisLine(false);
            ((CallOutViewHolder)holder).report_item_callout_linechart.getAxisRight().setDrawGridLines(false);
            ((CallOutViewHolder)holder).report_item_callout_linechart.getXAxis().setDrawAxisLine(false);
            ((CallOutViewHolder)holder).report_item_callout_linechart.getXAxis().setDrawGridLines(false);

            // enable touch gestures
            ((CallOutViewHolder)holder).report_item_callout_linechart.setTouchEnabled(true);

            // enable scaling and dragging
            ((CallOutViewHolder)holder).report_item_callout_linechart.setDragEnabled(true);
            ((CallOutViewHolder)holder).report_item_callout_linechart.setScaleEnabled(true);

            // if disabled, scaling can be done on x- and y-axis separately
            ((CallOutViewHolder)holder).report_item_callout_linechart.setPinchZoom(false);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < 8; i++) {
                xVals.add((i) + "");
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            for (int z = 0; z < 2; z++) {

                ArrayList<Entry> values = new ArrayList<Entry>();

                for (int i = 0; i < 8; i++) {
                    double val = (Math.random() * 8) + 3;
                    values.add(new Entry((float) val, i));
                }

                LineDataSet d = new LineDataSet(values, "DataSet " + (z + 1));
                d.setLineWidth(2.5f);
                d.setCircleRadius(4f);

                int color = mColors[z % mColors.length];
                d.setColor(color);
                d.setCircleColor(color);
                dataSets.add(d);
            }

            LineData data = new LineData(xVals, dataSets);
            ((CallOutViewHolder)holder).report_item_callout_linechart.setData(data);
            ((CallOutViewHolder)holder).report_item_callout_linechart.invalidate();

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
        LineChart report_item_callout_linechart;

        public CallOutViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_callout_linechart = (LineChart) itemView.findViewById(R.id.report_item_callout_linechart);

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
