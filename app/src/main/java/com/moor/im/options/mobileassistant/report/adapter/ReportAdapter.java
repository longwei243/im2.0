package com.moor.im.options.mobileassistant.report.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.mobileassistant.report.model.CallInData;
import com.moor.im.options.mobileassistant.report.model.ReportData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
            return new QueueViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item_queue, null));
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ReportData reportData = datas.get(position);
        if(datas.get(position).type == ReportData.TYPE_CALL_IN) {
            ((CallInViewHolder)holder).tv.setText(reportData.name);
            ((CallInViewHolder)holder).report_item_callin_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HttpManager.getInstance().doReport(UserDao.getInstance().getUser()._id, "callin", "week")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<String>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    LogUtil.d("获取报表数据失败");
                                }

                                @Override
                                public void onNext(String s) {
                                    LogUtil.d("报表呼入返回数据:"+s);
                                    JSONObject jsonObject = null;
                                    Gson gson = new Gson();
                                    try {
                                        jsonObject = new JSONObject(s);
                                        if(jsonObject.getBoolean("Succeed")) {
                                            JSONObject callin = jsonObject.getJSONObject("callin");
                                            if(callin.getBoolean("success")) {
                                                reportData.callInDatas = gson.fromJson(callin.getJSONArray("data").toString(),
                                                        new TypeToken<List<CallInData>>() {
                                                        }.getType());
                                                notifyDataSetChanged();
                                            }

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                }
            });

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
            for (int i = 0; i < reportData.callInDatas.size(); i++) {
                xVals.add(reportData.callInDatas.get(i).DayID);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            ArrayList<Entry> values1 = new ArrayList<Entry>();

            for (int i = 0; i < reportData.callInDatas.size(); i++) {
                double val = reportData.callInDatas.get(i).DealingCount;
                values1.add(new Entry((float) val, i));
            }

            LineDataSet d1 = new LineDataSet(values1, "接听数");
            d1.setLineWidth(2.5f);
            d1.setCircleRadius(4f);

            int color = mColors[0];
            d1.setColor(color);
            d1.setCircleColor(color);
            dataSets.add(d1);

            ArrayList<Entry> values2 = new ArrayList<Entry>();

            for (int i = 0; i < reportData.callInDatas.size(); i++) {
                double val = reportData.callInDatas.get(i).AccessCount;
                values2.add(new Entry((float) val, i));
            }

            LineDataSet d2 = new LineDataSet(values2, "总数");
            d1.setLineWidth(2.5f);
            d1.setCircleRadius(4f);

            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

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
            for (int i = 0; i < reportData.callOutDatas.size(); i++) {
                xVals.add(reportData.callOutDatas.get(i).ReportTime);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            ArrayList<Entry> values1 = new ArrayList<Entry>();

            for (int i = 0; i < reportData.callOutDatas.size(); i++) {
                double val = reportData.callOutDatas.get(i).DealingCount;
                values1.add(new Entry((float) val, i));
            }
            LineDataSet d1 = new LineDataSet(values1, "接听数");
            d1.setLineWidth(2.5f);
            d1.setCircleRadius(4f);

            int color = mColors[0];
            d1.setColor(color);
            d1.setCircleColor(color);
            dataSets.add(d1);

            ArrayList<Entry> values2 = new ArrayList<Entry>();

            for (int i = 0; i < reportData.callOutDatas.size(); i++) {
                double val = reportData.callOutDatas.get(i).AccessCount;
                values2.add(new Entry((float) val, i));
            }

            LineDataSet d2 = new LineDataSet(values2, "总数");
            d1.setLineWidth(2.5f);
            d1.setCircleRadius(4f);

            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

            LineData data = new LineData(xVals, dataSets);
            ((CallOutViewHolder)holder).report_item_callout_linechart.setData(data);
            ((CallOutViewHolder)holder).report_item_callout_linechart.invalidate();

        }else if(datas.get(position).type == ReportData.TYPE_QUEUE) {
            ((QueueViewHolder)holder).tv.setText(reportData.name);

            ((QueueViewHolder)holder).report_item_queue_barchart.setPinchZoom(false);

            ((QueueViewHolder)holder).report_item_queue_barchart.setDrawBarShadow(false);

            ((QueueViewHolder)holder).report_item_queue_barchart.setDrawGridBackground(false);

            YAxis leftAxis = ((QueueViewHolder)holder).report_item_queue_barchart.getAxisLeft();
            leftAxis.setValueFormatter(new LargeValueFormatter());
            leftAxis.setDrawGridLines(false);
            leftAxis.setSpaceTop(30f);
            leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

            ((QueueViewHolder)holder).report_item_queue_barchart.getAxisRight().setEnabled(false);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.queueDatas.size(); i++) {
                xVals.add(reportData.queueDatas.get(i).QueueName);
            }

            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();

            for (int i = 0; i < reportData.queueDatas.size(); i++) {
                float val = reportData.queueDatas.get(i).AccessCount;
                yVals1.add(new BarEntry(val, i));
            }

            for (int i = 0; i < reportData.queueDatas.size(); i++) {
                float val = reportData.queueDatas.get(i).AcceptCount;
                yVals2.add(new BarEntry(val, i));
            }

            BarDataSet set1, set2;

            // create 3 datasets with different types
            set1 = new BarDataSet(yVals1, "排队总数");
            set1.setColor(Color.rgb(104, 241, 175));
            set2 = new BarDataSet(yVals2, "接通数");
            set2.setColor(Color.rgb(164, 228, 251));

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);

            BarData data = new BarData(xVals, dataSets);

            // add space between the dataset groups in percent of bar-width
            data.setGroupSpace(80f);

            ((QueueViewHolder)holder).report_item_queue_barchart.setData(data);
            ((QueueViewHolder)holder).report_item_queue_barchart.invalidate();



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
        TextView report_item_callin_time_day, report_item_callin_time_week, report_item_callin_time_month;

        public CallInViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_callin_linechart = (LineChart) itemView.findViewById(R.id.report_item_callin_linechart);
            report_item_callin_time_day = (TextView) itemView.findViewById(R.id.report_item_callin_time_day);
            report_item_callin_time_week = (TextView) itemView.findViewById(R.id.report_item_callin_time_week);
            report_item_callin_time_month = (TextView) itemView.findViewById(R.id.report_item_callin_time_month);
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
        BarChart report_item_queue_barchart;

        public QueueViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_queue_barchart = (BarChart) itemView.findViewById(R.id.report_item_queue_barchart);
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
