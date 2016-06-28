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
import com.moor.im.common.utils.NullUtil;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.mobileassistant.report.MyValueFormatter;
import com.moor.im.options.mobileassistant.report.model.CallInData;
import com.moor.im.options.mobileassistant.report.model.CallOutData;
import com.moor.im.options.mobileassistant.report.model.CustData;
import com.moor.im.options.mobileassistant.report.model.CustSrc;
import com.moor.im.options.mobileassistant.report.model.IMData;
import com.moor.im.options.mobileassistant.report.model.QueueData;
import com.moor.im.options.mobileassistant.report.model.ReportData;
import com.moor.im.options.mobileassistant.report.model.SessionData;

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
    Gson gson = new Gson();
    private Context context;
    private List<ReportData> datas;

    private int[] mColors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[1],
            ColorTemplate.VORDIPLOM_COLORS[2],
            ColorTemplate.VORDIPLOM_COLORS[3]
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
            return new IMViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item_im, null));
        }else if(viewType == ReportData.TYPE_SESSION) {
            return new SessionViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item_session, null));
        }else if(viewType == ReportData.TYPE_CUSTOMER) {
            return new CustomerViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item_cust, null));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ReportData reportData = datas.get(position);
        if(datas.get(position).type == ReportData.TYPE_CALL_IN) {
            ((CallInViewHolder)holder).tv.setText(reportData.name);
            ((CallInViewHolder)holder).report_item_callin_time_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CallInViewHolder)holder).report_item_callin_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_checked);
                    ((CallInViewHolder)holder).report_item_callin_time_day.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((CallInViewHolder)holder).report_item_callin_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_normal);
                    ((CallInViewHolder)holder).report_item_callin_time_week.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((CallInViewHolder)holder).report_item_callin_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_normal);
                    ((CallInViewHolder)holder).report_item_callin_time_month.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));

                    refreshData("callin", "day", reportData);

                }
            });
            ((CallInViewHolder)holder).report_item_callin_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CallInViewHolder)holder).report_item_callin_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_normal);
                    ((CallInViewHolder)holder).report_item_callin_time_day.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((CallInViewHolder)holder).report_item_callin_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_checked);
                    ((CallInViewHolder)holder).report_item_callin_time_week.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((CallInViewHolder)holder).report_item_callin_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_normal);
                    ((CallInViewHolder)holder).report_item_callin_time_month.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));

                    refreshData("callin", "week", reportData);

                }
            });
            ((CallInViewHolder)holder).report_item_callin_time_month.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CallInViewHolder)holder).report_item_callin_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_normal);
                    ((CallInViewHolder)holder).report_item_callin_time_day.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((CallInViewHolder)holder).report_item_callin_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_normal);
                    ((CallInViewHolder)holder).report_item_callin_time_week.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((CallInViewHolder)holder).report_item_callin_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_checked);
                    ((CallInViewHolder)holder).report_item_callin_time_month.setTextColor(context.getResources().getColor(R.color.all_white));

                    refreshData("callin", "month", reportData);

                }
            });

            setLineChart(((CallInViewHolder)holder).report_item_callin_linechart);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.callInDatas.size(); i++) {
                xVals.add(reportData.callInDatas.get(i).X_axis);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            ArrayList<Entry> values1 = new ArrayList<Entry>();

            int dealingCountSum = 0;
            int accessCountSum = 0;

            for (int i = 0; i < reportData.callInDatas.size(); i++) {
                int val = reportData.callInDatas.get(i).DealingCount;
                values1.add(new Entry( val, i));
                dealingCountSum += val;
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
                int val = reportData.callInDatas.get(i).AccessCount;
                values2.add(new Entry( val, i));
                accessCountSum += val;
            }

            LineDataSet d2 = new LineDataSet(values2, "总数");
            d2.setLineWidth(2.5f);
            d2.setCircleRadius(4f);

            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

            LineData data = new LineData(xVals, dataSets);
            data.setValueFormatter(new MyValueFormatter());
            ((CallInViewHolder)holder).report_item_callin_linechart.setData(data);
            ((CallInViewHolder)holder).report_item_callin_linechart.invalidate();


            if(accessCountSum != 0) {
                int progress = dealingCountSum * 100 /accessCountSum;
                //界面展示
                ((CallInViewHolder)holder).report_item_callin_table_tv_time.setText("xxx");
                ((CallInViewHolder)holder).report_item_callin_table_tv_access.setText(accessCountSum+"");
                ((CallInViewHolder)holder).report_item_callin_table_tv_deal.setText(dealingCountSum+"");
                ((CallInViewHolder)holder).report_item_callin_table_tv_progress.setText(progress+"%");
            }else {
                ((CallInViewHolder)holder).report_item_callin_table_tv_time.setText("xxx");
                ((CallInViewHolder)holder).report_item_callin_table_tv_access.setText("0");
                ((CallInViewHolder)holder).report_item_callin_table_tv_deal.setText("0");
                ((CallInViewHolder)holder).report_item_callin_table_tv_progress.setText("0%");
            }


        }else if(datas.get(position).type == ReportData.TYPE_CALL_OUT) {
            ((CallOutViewHolder)holder).tv.setText(reportData.name);
            ((CallOutViewHolder)holder).report_item_callout_time_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CallOutViewHolder)holder).report_item_callout_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_checked);
                    ((CallOutViewHolder)holder).report_item_callout_time_day.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((CallOutViewHolder)holder).report_item_callout_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((CallOutViewHolder)holder).report_item_callout_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CallOutViewHolder)holder).report_item_callout_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((CallOutViewHolder)holder).report_item_callout_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    refreshData("callout", "day", reportData);

                }
            });
            ((CallOutViewHolder)holder).report_item_callout_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CallOutViewHolder)holder).report_item_callout_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((CallOutViewHolder)holder).report_item_callout_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CallOutViewHolder)holder).report_item_callout_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_checked);
                    ((CallOutViewHolder)holder).report_item_callout_time_week.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((CallOutViewHolder)holder).report_item_callout_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((CallOutViewHolder)holder).report_item_callout_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    refreshData("callout", "week", reportData);

                }
            });
            ((CallOutViewHolder)holder).report_item_callout_time_month.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CallOutViewHolder)holder).report_item_callout_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((CallOutViewHolder)holder).report_item_callout_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CallOutViewHolder)holder).report_item_callout_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((CallOutViewHolder)holder).report_item_callout_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CallOutViewHolder)holder).report_item_callout_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_checked);
                    ((CallOutViewHolder)holder).report_item_callout_time_month.setTextColor(context.getResources().getColor(R.color.all_white));

                    refreshData("callout", "month", reportData);

                }
            });


            setLineChart(((CallOutViewHolder)holder).report_item_callout_linechart);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.callOutDatas.size(); i++) {
                xVals.add(reportData.callOutDatas.get(i).X_axis);
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
            d2.setLineWidth(2.5f);
            d2.setCircleRadius(4f);

            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

            LineData data = new LineData(xVals, dataSets);
            data.setValueFormatter(new MyValueFormatter());
            ((CallOutViewHolder)holder).report_item_callout_linechart.setData(data);
            ((CallOutViewHolder)holder).report_item_callout_linechart.invalidate();

        }else if(datas.get(position).type == ReportData.TYPE_QUEUE) {
            ((QueueViewHolder)holder).tv.setText(reportData.name);
            ((QueueViewHolder)holder).report_item_queue_tv_time_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((QueueViewHolder)holder).report_item_queue_tv_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_checked);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_day.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((QueueViewHolder)holder).report_item_queue_tv_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_normal);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_week.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((QueueViewHolder)holder).report_item_queue_tv_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_normal);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_month.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));

                    refreshData("skillgroup", "day", reportData);
                }
            });
            ((QueueViewHolder)holder).report_item_queue_tv_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((QueueViewHolder)holder).report_item_queue_tv_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_normal);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_day.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((QueueViewHolder)holder).report_item_queue_tv_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_checked);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_week.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((QueueViewHolder)holder).report_item_queue_tv_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_normal);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_month.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));

                    refreshData("skillgroup", "week", reportData);
                }
            });
            ((QueueViewHolder)holder).report_item_queue_tv_time_month.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((QueueViewHolder)holder).report_item_queue_tv_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_normal);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_day.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((QueueViewHolder)holder).report_item_queue_tv_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_normal);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_week.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((QueueViewHolder)holder).report_item_queue_tv_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_checked);
                    ((QueueViewHolder)holder).report_item_queue_tv_time_month.setTextColor(context.getResources().getColor(R.color.all_white));

                    refreshData("skillgroup", "month", reportData);
                }
            });

            ((QueueViewHolder)holder).report_item_queue_barchart.setDescription("");
            ((QueueViewHolder)holder).report_item_queue_barchart.setPinchZoom(true);

            ((QueueViewHolder)holder).report_item_queue_barchart.setDrawBarShadow(false);

            ((QueueViewHolder)holder).report_item_queue_barchart.setDrawGridBackground(false);

            YAxis leftAxis = ((QueueViewHolder)holder).report_item_queue_barchart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setSpaceTop(30f);
            leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

            ((QueueViewHolder)holder).report_item_queue_barchart.getAxisRight().setEnabled(false);
            ((QueueViewHolder)holder).report_item_queue_barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

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

            // create 2 datasets with different types
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
            data.setValueFormatter(new MyValueFormatter());
            ((QueueViewHolder)holder).report_item_queue_barchart.setData(data);
            ((QueueViewHolder)holder).report_item_queue_barchart.invalidate();



        }else if(datas.get(position).type == ReportData.TYPE_IM) {
            ((IMViewHolder)holder).tv.setText(reportData.name);
            ((IMViewHolder)holder).report_item_im_tv_time_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IMViewHolder)holder).report_item_im_tv_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_checked);
                    ((IMViewHolder)holder).report_item_im_tv_time_day.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((IMViewHolder)holder).report_item_im_tv_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((IMViewHolder)holder).report_item_im_tv_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((IMViewHolder)holder).report_item_im_tv_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((IMViewHolder)holder).report_item_im_tv_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    refreshData("immessage", "day", reportData);
                }
            });
            ((IMViewHolder)holder).report_item_im_tv_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IMViewHolder)holder).report_item_im_tv_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((IMViewHolder)holder).report_item_im_tv_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((IMViewHolder)holder).report_item_im_tv_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_checked);
                    ((IMViewHolder)holder).report_item_im_tv_time_week.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((IMViewHolder)holder).report_item_im_tv_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((IMViewHolder)holder).report_item_im_tv_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    refreshData("immessage", "week", reportData);
                }
            });
            ((IMViewHolder)holder).report_item_im_tv_time_month.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IMViewHolder)holder).report_item_im_tv_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((IMViewHolder)holder).report_item_im_tv_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((IMViewHolder)holder).report_item_im_tv_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((IMViewHolder)holder).report_item_im_tv_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((IMViewHolder)holder).report_item_im_tv_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_checked);
                    ((IMViewHolder)holder).report_item_im_tv_time_month.setTextColor(context.getResources().getColor(R.color.all_white));

                    refreshData("immessage", "month", reportData);
                }
            });

            ((IMViewHolder)holder).report_item_im_barchart.setDescription("");
            ((IMViewHolder)holder).report_item_im_barchart.setPinchZoom(true);

            ((IMViewHolder)holder).report_item_im_barchart.setDrawBarShadow(false);

            ((IMViewHolder)holder).report_item_im_barchart.setDrawGridBackground(false);

            YAxis leftAxis = ((IMViewHolder)holder).report_item_im_barchart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setSpaceTop(30f);
            leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

            ((IMViewHolder)holder).report_item_im_barchart.getAxisRight().setEnabled(false);
            ((IMViewHolder)holder).report_item_im_barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);


            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.imDatas.size(); i++) {
                xVals.add(NullUtil.checkNull(reportData.imDatas.get(i).platform));
            }

            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals3 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals4 = new ArrayList<BarEntry>();

            for (int i = 0; i < reportData.imDatas.size(); i++) {
                float val = reportData.imDatas.get(i).sessionCount;
                yVals1.add(new BarEntry(val, i));
            }

            for (int i = 0; i < reportData.imDatas.size(); i++) {
                float val = reportData.imDatas.get(i).visitorCount;
                yVals2.add(new BarEntry(val, i));
            }
            for (int i = 0; i < reportData.imDatas.size(); i++) {
                float val = reportData.imDatas.get(i).messageCount;
                yVals3.add(new BarEntry(val, i));
            }
            for (int i = 0; i < reportData.imDatas.size(); i++) {
                float val = reportData.imDatas.get(i).robotMessageCount;
                yVals4.add(new BarEntry(val, i));
            }



            BarDataSet set1, set2, set3, set4;

            // create 2 datasets with different types
            set1 = new BarDataSet(yVals1, "对话数");
            set1.setColor(Color.rgb(104, 241, 175));
            set2 = new BarDataSet(yVals2, "访客数");
            set2.setColor(Color.rgb(164, 228, 251));
            set3 = new BarDataSet(yVals3, "消息数");
            set3.setColor(Color.rgb(111, 111, 251));
            set4 = new BarDataSet(yVals4, "机器人消息数");
            set4.setColor(Color.rgb(222, 228, 251));

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);
            dataSets.add(set3);
            dataSets.add(set4);

            BarData data = new BarData(xVals, dataSets);

            // add space between the dataset groups in percent of bar-width
            data.setGroupSpace(80f);
            data.setValueFormatter(new MyValueFormatter());
            ((IMViewHolder)holder).report_item_im_barchart.setData(data);
            ((IMViewHolder)holder).report_item_im_barchart.invalidate();


        }else if(datas.get(position).type == ReportData.TYPE_SESSION) {
            ((SessionViewHolder)holder).tv.setText(reportData.name);
            ((SessionViewHolder)holder).report_item_session_time_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SessionViewHolder)holder).report_item_session_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_checked);
                    ((SessionViewHolder)holder).report_item_session_time_day.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((SessionViewHolder)holder).report_item_session_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_normal);
                    ((SessionViewHolder)holder).report_item_session_time_week.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((SessionViewHolder)holder).report_item_session_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_normal);
                    ((SessionViewHolder)holder).report_item_session_time_month.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));

                    refreshData("imsession", "day", reportData);

                }
            });
            ((SessionViewHolder)holder).report_item_session_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ((SessionViewHolder)holder).report_item_session_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_normal);
                    ((SessionViewHolder)holder).report_item_session_time_day.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((SessionViewHolder)holder).report_item_session_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_checked);
                    ((SessionViewHolder)holder).report_item_session_time_week.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((SessionViewHolder)holder).report_item_session_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_normal);
                    ((SessionViewHolder)holder).report_item_session_time_month.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));

                    refreshData("imsession", "week", reportData);

                }
            });
            ((SessionViewHolder)holder).report_item_session_time_month.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SessionViewHolder)holder).report_item_session_time_day.setBackgroundResource(R.drawable.bg_report_callin_day_normal);
                    ((SessionViewHolder)holder).report_item_session_time_day.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((SessionViewHolder)holder).report_item_session_time_week.setBackgroundResource(R.drawable.bg_report_callin_week_normal);
                    ((SessionViewHolder)holder).report_item_session_time_week.setTextColor(context.getResources().getColor(R.color.report_callin_time_tv));
                    ((SessionViewHolder)holder).report_item_session_time_month.setBackgroundResource(R.drawable.bg_report_callin_month_checked);
                    ((SessionViewHolder)holder).report_item_session_time_month.setTextColor(context.getResources().getColor(R.color.all_white));

                    refreshData("imsession", "month", reportData);

                }
            });

            setLineChart(((SessionViewHolder)holder).report_item_session_linechart);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                xVals.add(reportData.sessionDatas.get(i).X_axis);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            ArrayList<Entry> values1 = new ArrayList<Entry>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                double val = reportData.sessionDatas.get(i).manualSessionCount;
                values1.add(new Entry((float) val, i));
            }
            LineDataSet d1 = new LineDataSet(values1, "人工对话数");
            d1.setLineWidth(2.5f);
            d1.setCircleRadius(4f);
            int color = mColors[0];
            d1.setColor(color);
            d1.setCircleColor(color);
            dataSets.add(d1);

            ArrayList<Entry> values2 = new ArrayList<Entry>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                double val = reportData.sessionDatas.get(i).robotSessionCount;
                values2.add(new Entry((float) val, i));
            }
            LineDataSet d2 = new LineDataSet(values2, "机器人对话数");
            d2.setLineWidth(2.5f);
            d2.setCircleRadius(4f);
            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

            ArrayList<Entry> values3 = new ArrayList<Entry>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                double val = reportData.sessionDatas.get(i).convertManualCount;
                values3.add(new Entry((float) val, i));
            }
            LineDataSet d3 = new LineDataSet(values3, "转人工");
            d3.setLineWidth(2.5f);
            d3.setCircleRadius(4f);
            int color3 = mColors[2];
            d3.setColor(color3);
            d3.setCircleColor(color3);
            dataSets.add(d3);

            LineData data = new LineData(xVals, dataSets);
            data.setValueFormatter(new MyValueFormatter());
            ((SessionViewHolder)holder).report_item_session_linechart.setData(data);
            ((SessionViewHolder)holder).report_item_session_linechart.invalidate();

        }else if(datas.get(position).type == ReportData.TYPE_CUSTOMER) {
            ((CustomerViewHolder)holder).tv.setText(reportData.name);

            ((CustomerViewHolder)holder).report_item_cust_time_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CustomerViewHolder)holder).report_item_cust_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_checked);
                    ((CustomerViewHolder)holder).report_item_cust_time_day.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((CustomerViewHolder)holder).report_item_cust_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((CustomerViewHolder)holder).report_item_cust_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CustomerViewHolder)holder).report_item_cust_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((CustomerViewHolder)holder).report_item_cust_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    refreshData("customerinc", "day", reportData);

                }
            });
            ((CustomerViewHolder)holder).report_item_cust_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CustomerViewHolder)holder).report_item_cust_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((CustomerViewHolder)holder).report_item_cust_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CustomerViewHolder)holder).report_item_cust_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_checked);
                    ((CustomerViewHolder)holder).report_item_cust_time_week.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((CustomerViewHolder)holder).report_item_cust_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((CustomerViewHolder)holder).report_item_cust_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    refreshData("customerinc", "week", reportData);

                }
            });
            ((CustomerViewHolder)holder).report_item_cust_time_month.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CustomerViewHolder)holder).report_item_cust_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((CustomerViewHolder)holder).report_item_cust_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CustomerViewHolder)holder).report_item_cust_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((CustomerViewHolder)holder).report_item_cust_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((CustomerViewHolder)holder).report_item_cust_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_checked);
                    ((CustomerViewHolder)holder).report_item_cust_time_month.setTextColor(context.getResources().getColor(R.color.all_white));

                    refreshData("customerinc", "month", reportData);

                }
            });

            setLineChart(((CustomerViewHolder)holder).report_item_cust_linechart);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.custDatas.size(); i++) {
                xVals.add(reportData.custDatas.get(i).day);
            }

            if(reportData.custDatas.size() != 0 && reportData.custDatas.get(0) != null) {
                int count = reportData.custDatas.get(0).counts.size();
                for(int i=0; i<count; i++) {
                    ArrayList<Entry> values2 = new ArrayList<Entry>();
                    for (int j = 0; j < reportData.custDatas.size(); j++) {
                        double val = reportData.custDatas.get(j).counts.get(i).srcCount;
                        values2.add(new Entry((float) val, j));
                    }
                    LineDataSet d2 = new LineDataSet(values2, reportData.custDatas.get(0).counts.get(i).srcName);
                    d2.setLineWidth(2.5f);
                    d2.setCircleRadius(4f);
                    int color2 = mColors[i%4];
                    d2.setColor(color2);
                    d2.setCircleColor(color2);
                    dataSets.add(d2);
                }
                LineData data = new LineData(xVals, dataSets);
                data.setValueFormatter(new MyValueFormatter());
                ((CustomerViewHolder)holder).report_item_cust_linechart.setData(data);
                ((CustomerViewHolder)holder).report_item_cust_linechart.invalidate();

            }


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

    private void refreshData(final String type, String time, final ReportData reportData) {
        HttpManager.getInstance().doReport(UserDao.getInstance().getUser()._id, type, time)
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

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(s);
                            if(jsonObject.getBoolean("Succeed")) {
                                if("callin".equals(type)) {
                                    LogUtil.d("报表呼入返回数据:"+s);
                                    JSONObject callin = jsonObject.getJSONObject("callin");
                                    if(callin.getBoolean("success")) {
                                        reportData.callInDatas = gson.fromJson(callin.getJSONArray("data").toString(),
                                                new TypeToken<List<CallInData>>() {
                                                }.getType());
                                        notifyDataSetChanged();
                                    }
                                }else if("callout".equals(type)) {
                                    LogUtil.d("报表呼出返回数据:"+s);
                                    JSONObject callout = jsonObject.getJSONObject("callout");
                                    if(callout.getBoolean("success")) {
                                        reportData.callOutDatas = gson.fromJson(callout.getJSONArray("data").toString(),
                                                new TypeToken<List<CallOutData>>() {
                                                }.getType());
                                        notifyDataSetChanged();
                                    }
                                }else if("skillgroup".equals(type)) {
                                    LogUtil.d("报表技能组返回数据:"+s);
                                    JSONObject skillgroup = jsonObject.getJSONObject("skillgroup");
                                    if(skillgroup.getBoolean("success")) {
                                        reportData.queueDatas = gson.fromJson(skillgroup.getJSONArray("data").toString(),
                                                new TypeToken<List<QueueData>>() {
                                                }.getType());
                                        notifyDataSetChanged();
                                    }
                                }else if("immessage".equals(type)) {
                                    LogUtil.d("报表客服返回数据:"+s);
                                    JSONObject immessage = jsonObject.getJSONObject("immessage");
                                    if(immessage.getBoolean("success")) {
                                        reportData.imDatas = gson.fromJson(immessage.getJSONArray("data").toString(),
                                                new TypeToken<List<IMData>>() {
                                                }.getType());
                                        notifyDataSetChanged();
                                    }
                                }else if("imsession".equals(type)) {
                                    LogUtil.d("报表会话返回数据:"+s);
                                    JSONObject imsession = jsonObject.getJSONObject("imsession");
                                    if(imsession.getBoolean("success")) {
                                        reportData.sessionDatas = gson.fromJson(imsession.getJSONArray("data").toString(),
                                                new TypeToken<List<SessionData>>() {
                                                }.getType());
                                        notifyDataSetChanged();
                                    }
                                }else if("customerinc".equals(type)) {
                                    LogUtil.d("报表客户来源返回数据:"+s);
                                    JSONObject customerinc = jsonObject.getJSONObject("customerinc");
                                    if(customerinc.getBoolean("success")) {
                                        reportData.custDatas = gson.fromJson(customerinc.getJSONArray("data").toString(),
                                                new TypeToken<List<CustData>>() {
                                                }.getType());
                                        notifyDataSetChanged();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }


    private void setLineChart(LineChart lineChart) {
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription("");
        lineChart.setDrawBorders(false);

        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setDrawAxisLine(true);
        lineChart.getXAxis().setDrawGridLines(false);

        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setHighlightPerDragEnabled(false);

    }


    class CallInViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;
        LineChart report_item_callin_linechart;
        TextView report_item_callin_time_day, report_item_callin_time_week, report_item_callin_time_month;
        TextView report_item_callin_table_tv_time,report_item_callin_table_tv_access,report_item_callin_table_tv_deal,report_item_callin_table_tv_progress;

        public CallInViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_callin_linechart = (LineChart) itemView.findViewById(R.id.report_item_callin_linechart);
            report_item_callin_time_day = (TextView) itemView.findViewById(R.id.report_item_callin_time_day);
            report_item_callin_time_week = (TextView) itemView.findViewById(R.id.report_item_callin_time_week);
            report_item_callin_time_month = (TextView) itemView.findViewById(R.id.report_item_callin_time_month);

            report_item_callin_table_tv_time = (TextView) itemView.findViewById(R.id.report_item_callin_table_tv_time);
            report_item_callin_table_tv_access = (TextView) itemView.findViewById(R.id.report_item_callin_table_tv_access);
            report_item_callin_table_tv_deal = (TextView) itemView.findViewById(R.id.report_item_callin_table_tv_deal);
            report_item_callin_table_tv_progress = (TextView) itemView.findViewById(R.id.report_item_callin_table_tv_progress);
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
        TextView report_item_callout_time_day,report_item_callout_time_week,report_item_callout_time_month;

        public CallOutViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_callout_linechart = (LineChart) itemView.findViewById(R.id.report_item_callout_linechart);
            report_item_callout_time_day = (TextView) itemView.findViewById(R.id.report_item_callout_time_day);
            report_item_callout_time_week = (TextView) itemView.findViewById(R.id.report_item_callout_time_week);
            report_item_callout_time_month = (TextView) itemView.findViewById(R.id.report_item_callout_time_month);

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
        TextView report_item_queue_tv_time_day, report_item_queue_tv_time_week, report_item_queue_tv_time_month;

        public QueueViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_queue_barchart = (BarChart) itemView.findViewById(R.id.report_item_queue_barchart);
            report_item_queue_tv_time_day = (TextView) itemView.findViewById(R.id.report_item_queue_tv_time_day);
            report_item_queue_tv_time_week = (TextView) itemView.findViewById(R.id.report_item_queue_tv_time_week);
            report_item_queue_tv_time_month = (TextView) itemView.findViewById(R.id.report_item_queue_tv_time_month);

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
        BarChart report_item_im_barchart;
        TextView report_item_im_tv_time_day, report_item_im_tv_time_week, report_item_im_tv_time_month;

        public IMViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_im_barchart = (BarChart) itemView.findViewById(R.id.report_item_im_barchart);
            report_item_im_tv_time_day = (TextView) itemView.findViewById(R.id.report_item_im_tv_time_day);
            report_item_im_tv_time_week = (TextView) itemView.findViewById(R.id.report_item_im_tv_time_week);
            report_item_im_tv_time_month = (TextView) itemView.findViewById(R.id.report_item_im_tv_time_month);

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
        LineChart report_item_session_linechart;
        TextView report_item_session_time_day, report_item_session_time_week, report_item_session_time_month;
        public SessionViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_session_linechart = (LineChart) itemView.findViewById(R.id.report_item_session_linechart);
            report_item_session_time_day = (TextView) itemView.findViewById(R.id.report_item_session_time_day);
            report_item_session_time_week = (TextView) itemView.findViewById(R.id.report_item_session_time_week);
            report_item_session_time_month = (TextView) itemView.findViewById(R.id.report_item_session_time_month);
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
        LineChart report_item_cust_linechart;
        TextView report_item_cust_time_day, report_item_cust_time_week, report_item_cust_time_month;
        public CustomerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_cust_linechart = (LineChart) itemView.findViewById(R.id.report_item_cust_linechart);
            report_item_cust_time_day = (TextView) itemView.findViewById(R.id.report_item_cust_time_day);
            report_item_cust_time_week = (TextView) itemView.findViewById(R.id.report_item_cust_time_week);
            report_item_cust_time_month = (TextView) itemView.findViewById(R.id.report_item_cust_time_month);

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
