package com.moor.im.options.mobileassistant.report.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.common.utils.WindowUtils;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.MobileAssitantParser;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.report.AgentChooiseActivity;
import com.moor.im.options.mobileassistant.report.AgentChooised;
import com.moor.im.options.mobileassistant.report.ReportActivity;
import com.moor.im.options.mobileassistant.report.model.WorkLoadData;
import com.moor.imkf.gson.Gson;
import com.moor.imkf.gson.reflect.TypeToken;
import com.moor.im.R;
import com.moor.im.common.db.dao.UserDao;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.options.mobileassistant.report.MyValueFormatter;
import com.moor.im.options.mobileassistant.report.model.CallInData;
import com.moor.im.options.mobileassistant.report.model.CallOutData;
import com.moor.im.options.mobileassistant.report.model.CustData;
import com.moor.im.options.mobileassistant.report.model.IMData;
import com.moor.im.options.mobileassistant.report.model.QueueData;
import com.moor.im.options.mobileassistant.report.model.ReportData;
import com.moor.im.options.mobileassistant.report.model.SessionData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by longwei on 2016/6/16.
 */
public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener{
    Gson gson = new Gson();
    private Context context;
    private List<ReportData> datas;

    private int[] mColors = new int[] {
            Color.rgb(237, 114, 76),
            Color.rgb(31, 217, 156),
            Color.rgb(29, 172, 229),
            Color.rgb(125, 137, 238),
            Color.rgb(229, 210, 69)
    };

    private String callInTime="今天", callOutTime="今天", sessionTime="今天", custTime="今天";

    private String agnetWorkTimeStatus = "day";
    private List<MAAgent> agentsList = new ArrayList<>();


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
        }else if(viewType == ReportData.TYPE_AGENT) {
            return new AgentViewHolder(LayoutInflater.from(context).inflate(R.layout.report_item_agent, null));
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

                    callInTime = "今天";
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

                    callInTime = "一周内";
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

                    callInTime = "一月内";
                    refreshData("callin", "month", reportData);

                }
            });

            setLineChart(((CallInViewHolder)holder).report_item_callin_linechart);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.callInDatas.size(); i++) {
                String x = reportData.callInDatas.get(i).X_axis;
                if(x != null && !"".equals(x) && x.length() == 8) {
                    x = x.substring(4);
                }else if(x != null && !"".equals(x) && x.length() == 19) {
                    x = x.substring(11, 16);
                }else {
                    x = "";
                }
                xVals.add(x);
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
            d1.setLineWidth(2f);
            d1.setCircleRadius(2f);

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
            d2.setLineWidth(2f);
            d2.setCircleRadius(2f);

            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

            LineData data = new LineData(xVals, dataSets);
            data.setValueFormatter(new MyValueFormatter());
            ((CallInViewHolder)holder).report_item_callin_linechart.setData(data);
            ((CallInViewHolder)holder).report_item_callin_linechart.invalidate();


            if(accessCountSum != 0) {
                int progress = dealingCountSum * 100 / accessCountSum;
                //界面展示
                ((CallInViewHolder)holder).report_item_callin_table_tv_time.setText(callInTime);
                ((CallInViewHolder)holder).report_item_callin_table_tv_access.setText(accessCountSum+"");
                ((CallInViewHolder)holder).report_item_callin_table_tv_deal.setText(dealingCountSum+"");
                ((CallInViewHolder)holder).report_item_callin_table_tv_progress.setText(progress+"%");
            }else {
                ((CallInViewHolder)holder).report_item_callin_table_tv_time.setText(callInTime);
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

                    callOutTime = "今天";
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

                    callOutTime = "一周内";
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

                    callOutTime = "一月内";
                    refreshData("callout", "month", reportData);

                }
            });


            setLineChart(((CallOutViewHolder)holder).report_item_callout_linechart);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.callOutDatas.size(); i++) {
                String x = reportData.callOutDatas.get(i).X_axis;
                if(x != null && !"".equals(x) && x.length() == 8) {
                    x = x.substring(4);
                }else if(x != null && !"".equals(x) && x.length() == 19) {
                    x = x.substring(11, 16);
                }else {
                    x = "";
                }
                xVals.add(x);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();


            int dealingCountSum = 0;
            int accessCountSum = 0;
            ArrayList<Entry> values1 = new ArrayList<Entry>();

            for (int i = 0; i < reportData.callOutDatas.size(); i++) {
                double val = reportData.callOutDatas.get(i).DealingCount;
                values1.add(new Entry((float) val, i));
                dealingCountSum += val;
            }
            LineDataSet d1 = new LineDataSet(values1, "接听数");
            d1.setLineWidth(2f);
            d1.setCircleRadius(2f);

            int color = mColors[0];
            d1.setColor(color);
            d1.setCircleColor(color);
            dataSets.add(d1);

            ArrayList<Entry> values2 = new ArrayList<Entry>();

            for (int i = 0; i < reportData.callOutDatas.size(); i++) {
                double val = reportData.callOutDatas.get(i).AccessCount;
                values2.add(new Entry((float) val, i));
                accessCountSum += val;
            }

            LineDataSet d2 = new LineDataSet(values2, "总数");
            d2.setLineWidth(2f);
            d2.setCircleRadius(2f);

            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

            LineData data = new LineData(xVals, dataSets);
            data.setValueFormatter(new MyValueFormatter());
            ((CallOutViewHolder)holder).report_item_callout_linechart.setData(data);
            ((CallOutViewHolder)holder).report_item_callout_linechart.invalidate();

            if(accessCountSum != 0) {
                int progress = dealingCountSum * 100 / accessCountSum;
                //界面展示
                ((CallOutViewHolder)holder).report_item_callout_table_tv_time.setText(callOutTime);
                ((CallOutViewHolder)holder).report_item_callout_table_tv_access.setText(accessCountSum+"");
                ((CallOutViewHolder)holder).report_item_callout_table_tv_deal.setText(dealingCountSum+"");
                ((CallOutViewHolder)holder).report_item_callout_table_tv_progress.setText(progress+"%");
            }else {
                ((CallOutViewHolder)holder).report_item_callout_table_tv_time.setText(callOutTime);
                ((CallOutViewHolder)holder).report_item_callout_table_tv_access.setText("0");
                ((CallOutViewHolder)holder).report_item_callout_table_tv_deal.setText("0");
                ((CallOutViewHolder)holder).report_item_callout_table_tv_progress.setText("0%");
            }

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
            ((QueueViewHolder)holder).report_item_queue_barchart.setHighlightPerTapEnabled(false);
            ((QueueViewHolder)holder).report_item_queue_barchart.setHighlightPerDragEnabled(false);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.queueDatas.size(); i++) {
                xVals.add(reportData.queueDatas.get(i).QueueName);
            }

            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();

            for (int i = 0; i < reportData.queueDatas.size(); i++) {
                float val = reportData.queueDatas.get(i).AccessCount;
                yVals1.add(new BarEntry(val, i));
                float val1 = reportData.queueDatas.get(i).AcceptCount;
                yVals2.add(new BarEntry(val1, i));
            }

            BarDataSet set1, set2;

            // create 2 datasets with different types
            set1 = new BarDataSet(yVals1, "排队总数");
            set1.setColor(mColors[0]);
            set2 = new BarDataSet(yVals2, "接通数");
            set2.setColor(mColors[1]);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);

            BarData data = new BarData(xVals, dataSets);

            // add space between the dataset groups in percent of bar-width
            data.setGroupSpace(80f);
            data.setValueFormatter(new MyValueFormatter());
            ((QueueViewHolder)holder).report_item_queue_barchart.setData(data);
            ((QueueViewHolder)holder).report_item_queue_barchart.invalidate();

            ((QueueViewHolder)holder).report_item_queue_tablelayout.removeAllViews();
            if(reportData.queueDatas.size() > 0) {
                TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_queue_table_head, ((QueueViewHolder)holder).report_item_queue_tablelayout, false);
                ((QueueViewHolder)holder).report_item_queue_tablelayout.addView(headTableRow);
                if(reportData.queueDatas.size() == 1) {
                    String name = reportData.queueDatas.get(0).QueueName;
                    String accessCountStr = reportData.queueDatas.get(0).AccessCount + "";
                    String acceptCountStr = reportData.queueDatas.get(0).AcceptCount + "";
                    String progress = reportData.queueDatas.get(0).AcceptRate;

                    TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_queue_table_layout, ((QueueViewHolder)holder).report_item_queue_tablelayout,false);
                    TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_progress);

                    tv_name.setText(name);
                    tv_access.setText(accessCountStr);
                    tv_accept.setText(acceptCountStr);
                    tv_progress.setText(progress);

                    ((QueueViewHolder)holder).report_item_queue_tablelayout.addView(tableRow);
                }else {
                    for(int i = 0; i < reportData.queueDatas.size()-1; i++) {
                        String name = reportData.queueDatas.get(i).QueueName;
                        String accessCountStr = reportData.queueDatas.get(i).AccessCount + "";
                        String acceptCountStr = reportData.queueDatas.get(i).AcceptCount + "";
                        String progress = reportData.queueDatas.get(i).AcceptRate;

                        TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_queue_table_layout_medium, ((QueueViewHolder)holder).report_item_queue_tablelayout,false);
                        TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                        TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                        TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                        TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_progress);

                        tv_name.setText(name);
                        tv_access.setText(accessCountStr);
                        tv_accept.setText(acceptCountStr);
                        tv_progress.setText(progress);
                        ((QueueViewHolder)holder).report_item_queue_tablelayout.addView(tableRow);

                    }
                    String name = reportData.queueDatas.get(reportData.queueDatas.size()-1).QueueName;
                    String accessCountStr = reportData.queueDatas.get(reportData.queueDatas.size()-1).AccessCount + "";
                    String acceptCountStr = reportData.queueDatas.get(reportData.queueDatas.size()-1).AcceptCount + "";
                    String progress = reportData.queueDatas.get(reportData.queueDatas.size()-1).AcceptRate;

                    TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_queue_table_layout, null);
                    TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_progress);

                    tv_name.setText(name);
                    tv_access.setText(accessCountStr);
                    tv_accept.setText(acceptCountStr);
                    tv_progress.setText(progress);
                    ((QueueViewHolder)holder).report_item_queue_tablelayout.addView(tableRow);

                }
            }else {
//                TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_queue_table_head, null);
//                ((QueueViewHolder)holder).report_item_queue_tablelayout.addView(headTableRow);
            }

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
            ((IMViewHolder)holder).report_item_im_barchart.setHighlightPerTapEnabled(false);
            ((IMViewHolder)holder).report_item_im_barchart.setHighlightPerDragEnabled(false);


            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.imDatas.size(); i++) {
                if(reportData.imDatas.get(i).platform != null) {
                    String platform = reportData.imDatas.get(i).platform;
                    String x = "";
                    if("pc".equals(platform)) {
                        x = "网站咨询";
                    }else if("sdk".equals(platform)) {
                        x = "App咨询";
                    }else if("weixin".equals(platform)) {
                        x = "微信咨询";
                    }else if("wap".equals(platform)) {
                        x = "Wap咨询";
                    }
                    xVals.add(x);
                }else {
                    xVals.add("未知");
                }

            }

            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals3 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals4 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals5 = new ArrayList<BarEntry>();

            for (int i = 0; i < reportData.imDatas.size(); i++) {
                float val = reportData.imDatas.get(i).sessionCount;
                yVals1.add(new BarEntry(val, i));
                float val1 = reportData.imDatas.get(i).visitorCount;
                yVals2.add(new BarEntry(val1, i));
                float val2 = reportData.imDatas.get(i).messageCount;
                yVals3.add(new BarEntry(val2, i));
                float val3 = reportData.imDatas.get(i).robotMessageCount;
                yVals4.add(new BarEntry(val3, i));
                float val4 = reportData.imDatas.get(i).robotSessionCount;
                yVals5.add(new BarEntry(val4, i));
            }

            BarDataSet set1, set2, set3, set4, set5;

            set1 = new BarDataSet(yVals1, "对话数");
            set1.setColor(mColors[0]);
            set2 = new BarDataSet(yVals2, "访客数");
            set2.setColor(mColors[1]);
            set3 = new BarDataSet(yVals3, "消息数");
            set3.setColor(mColors[2]);
            set4 = new BarDataSet(yVals4, "机器人消息数");
            set4.setColor(mColors[3]);
            set5 = new BarDataSet(yVals5, "机器人会话数");
            set5.setColor(mColors[4]);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);
            dataSets.add(set3);
            dataSets.add(set4);
            dataSets.add(set5);

            BarData data = new BarData(xVals, dataSets);

            // add space between the dataset groups in percent of bar-width
            data.setGroupSpace(80f);
            data.setValueFormatter(new MyValueFormatter());
            ((IMViewHolder)holder).report_item_im_barchart.setData(data);
            ((IMViewHolder)holder).report_item_im_barchart.invalidate();


            ((IMViewHolder)holder).report_item_im_tablelayout.removeAllViews();
            if(reportData.imDatas.size() > 0) {

                TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_im_table_head, ((IMViewHolder)holder).report_item_im_tablelayout, false);
                ((IMViewHolder) holder).report_item_im_tablelayout.addView(headTableRow);
                if(reportData.imDatas.size() == 1) {
                    String name = "未知";
                    if(reportData.imDatas.get(0).platform != null) {
                        String platform = reportData.imDatas.get(0).platform;
                        if("pc".equals(platform)) {
                            name = "网站咨询";
                        }else if("sdk".equals(platform)) {
                            name = "App咨询";
                        }else if("weixin".equals(platform)) {
                            name = "微信咨询";
                        }else if("wap".equals(platform)) {
                            name = "Wap咨询";
                        }
                    }
                    String accessCountStr = reportData.imDatas.get(0).sessionCount + "";
                    String acceptCountStr = reportData.imDatas.get(0).visitorCount + "";
                    String progress = reportData.imDatas.get(0).messageCount + "";

                    TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_im_table_layout, ((IMViewHolder)holder).report_item_im_tablelayout, false);
                    TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_progress);

                    tv_name.setText(name);
                    tv_access.setText(accessCountStr);
                    tv_accept.setText(acceptCountStr);
                    tv_progress.setText(progress);

                    ((IMViewHolder) holder).report_item_im_tablelayout.addView(tableRow);
                }else {
                    for (int i = 0; i < reportData.imDatas.size()-1; i++) {
                        String name = "未知";
                        if(reportData.imDatas.get(i).platform != null) {
                            String platform = reportData.imDatas.get(i).platform;
                            if("pc".equals(platform)) {
                                name = "网站咨询";
                            }else if("sdk".equals(platform)) {
                                name = "App咨询";
                            }else if("weixin".equals(platform)) {
                                name = "微信咨询";
                            }else if("wap".equals(platform)) {
                                name = "Wap咨询";
                            }
                        }
                        String accessCountStr = reportData.imDatas.get(i).sessionCount + "";
                        String acceptCountStr = reportData.imDatas.get(i).visitorCount + "";
                        String progress = reportData.imDatas.get(i).messageCount + "";

                        TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_im_table_layout_medium, ((IMViewHolder)holder).report_item_im_tablelayout, false);
                        TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                        TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                        TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                        TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_progress);

                        tv_name.setText(name);
                        tv_access.setText(accessCountStr);
                        tv_accept.setText(acceptCountStr);
                        tv_progress.setText(progress);
                        ((IMViewHolder) holder).report_item_im_tablelayout.addView(tableRow);
                    }

                    String name = "未知";
                    if(reportData.imDatas.get(reportData.imDatas.size()-1).platform != null) {
                        String platform = reportData.imDatas.get(reportData.imDatas.size()-1).platform;
                        if("pc".equals(platform)) {
                            name = "网站咨询";
                        }else if("sdk".equals(platform)) {
                            name = "App咨询";
                        }else if("weixin".equals(platform)) {
                            name = "微信咨询";
                        }else if("wap".equals(platform)) {
                            name = "Wap咨询";
                        }
                    }
                    String accessCountStr = reportData.imDatas.get(reportData.imDatas.size()-1).sessionCount + "";
                    String acceptCountStr = reportData.imDatas.get(reportData.imDatas.size()-1).visitorCount + "";
                    String progress = reportData.imDatas.get(reportData.imDatas.size()-1).messageCount + "";

                    TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_im_table_layout, ((IMViewHolder)holder).report_item_im_tablelayout, false);
                    TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_progress);

                    tv_name.setText(name);
                    tv_access.setText(accessCountStr);
                    tv_accept.setText(acceptCountStr);
                    tv_progress.setText(progress);
                    ((IMViewHolder) holder).report_item_im_tablelayout.addView(tableRow);

                }
            }else {
//                TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_im_table_head, null);
//                ((IMViewHolder) holder).report_item_im_tablelayout.addView(headTableRow);
            }

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

                    sessionTime = "今天";
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

                    sessionTime = "一周内";
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

                    sessionTime = "一月内";
                    refreshData("imsession", "month", reportData);

                }
            });

            setLineChart(((SessionViewHolder)holder).report_item_session_linechart);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                String x = reportData.sessionDatas.get(i).X_axis;
                if(x != null && !"".equals(x) && x.length() == 8) {
                    x = x.substring(4);
                }else if(x != null && !"".equals(x) && x.length() == 10) {
                    x = x.substring(8)+":00";
                }else {
                    x = "";
                }
                xVals.add(x);
            }

            int manualCountSum=0, robotCountSum=0, convertCountSum=0;
            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            ArrayList<Entry> values1 = new ArrayList<Entry>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                double val = reportData.sessionDatas.get(i).manualSessionCount;
                values1.add(new Entry((float) val, i));
                manualCountSum += val;
            }
            LineDataSet d1 = new LineDataSet(values1, "人工对话数");
            d1.setLineWidth(2f);
            d1.setCircleRadius(2f);
            int color = mColors[0];
            d1.setColor(color);
            d1.setCircleColor(color);
            dataSets.add(d1);

            ArrayList<Entry> values2 = new ArrayList<Entry>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                double val = reportData.sessionDatas.get(i).robotSessionCount;
                values2.add(new Entry((float) val, i));
                robotCountSum += val;
            }
            LineDataSet d2 = new LineDataSet(values2, "机器人对话数");
            d2.setLineWidth(2f);
            d2.setCircleRadius(2f);
            int color2 = mColors[1];
            d2.setColor(color2);
            d2.setCircleColor(color2);
            dataSets.add(d2);

            ArrayList<Entry> values3 = new ArrayList<Entry>();
            for (int i = 0; i < reportData.sessionDatas.size(); i++) {
                double val = reportData.sessionDatas.get(i).convertManualCount;
                values3.add(new Entry((float) val, i));
                convertCountSum += val;
            }
            LineDataSet d3 = new LineDataSet(values3, "转人工");
            d3.setLineWidth(2f);
            d3.setCircleRadius(2f);
            int color3 = mColors[2];
            d3.setColor(color3);
            d3.setCircleColor(color3);
            dataSets.add(d3);

            LineData data = new LineData(xVals, dataSets);
            data.setValueFormatter(new MyValueFormatter());
            ((SessionViewHolder)holder).report_item_session_linechart.setData(data);
            ((SessionViewHolder)holder).report_item_session_linechart.invalidate();



            //界面展示
            ((SessionViewHolder)holder).report_item_session_table_tv_time.setText(sessionTime);
            ((SessionViewHolder)holder).report_item_session_table_tv_manual.setText(manualCountSum+"");
            ((SessionViewHolder)holder).report_item_session_table_tv_robot.setText(robotCountSum+"");
            ((SessionViewHolder)holder).report_item_session_table_tv_convert.setText(convertCountSum+"");

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

                    custTime = "今天";
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

                    custTime = "一周内";
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

                    custTime = "一月内";
                    refreshData("customerinc", "month", reportData);

                }
            });

            setLineChart(((CustomerViewHolder)holder).report_item_cust_linechart);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < reportData.custDatas.size(); i++) {
                String x = reportData.custDatas.get(i).day;
                if(x != null && !"".equals(x) && x.length() == 8) {
                    x = x.substring(4);
                }else if(x != null && !"".equals(x) && x.length() == 19) {
                    x = x.substring(11, 16);
                }else {
                    x = "";
                }
                xVals.add(x);
            }
            ((CustomerViewHolder)holder).report_item_cust_tablelayout.removeAllViews();
            if(reportData.custDatas.size() != 0 && reportData.custDatas.get(0) != null) {
                int count = reportData.custDatas.get(0).counts.size();
                for(int i=0; i<count; i++) {
                    ArrayList<Entry> values2 = new ArrayList<Entry>();
                    for (int j = 0; j < reportData.custDatas.size(); j++) {
                        double val = reportData.custDatas.get(j).counts.get(i).srcCount;
                        values2.add(new Entry((float) val, j));
                    }
                    LineDataSet d2 = new LineDataSet(values2, reportData.custDatas.get(0).counts.get(i).srcName);
                    d2.setLineWidth(2f);
                    d2.setCircleRadius(2f);
                    int color2 = mColors[i%5];
                    d2.setColor(color2);
                    d2.setCircleColor(color2);
                    dataSets.add(d2);
                }
                LineData data = new LineData(xVals, dataSets);
                data.setValueFormatter(new MyValueFormatter());
                ((CustomerViewHolder)holder).report_item_cust_linechart.setData(data);
                ((CustomerViewHolder)holder).report_item_cust_linechart.invalidate();

                if(reportData.custDatas.size() > 0 && reportData.custDatas.get(0) != null) {
                    TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_cust_table_head, ((CustomerViewHolder) holder).report_item_cust_tablelayout, false);
                    ((CustomerViewHolder) holder).report_item_cust_tablelayout.addView(headTableRow);
                    int c = reportData.custDatas.get(0).counts.size();
                    if(c == 1) {
                        int srcCountSum = 0;
                        String srcName = reportData.custDatas.get(0).counts.get(0).srcName;
                        for(int j=0; j<reportData.custDatas.size(); j++) {
                            srcCountSum += reportData.custDatas.get(j).counts.get(0).srcCount;
                        }

                        TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_cust_table_layout, ((CustomerViewHolder) holder).report_item_cust_tablelayout, false);
                        TextView tv_time = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_time);
                        TextView tv_src = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_src);
                        TextView tv_count = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_count);

                        tv_time.setText(custTime);
                        tv_src.setText(srcName);
                        tv_count.setText(srcCountSum+"");

                        ((CustomerViewHolder) holder).report_item_cust_tablelayout.addView(tableRow);
                    }else {
                        for(int i=0; i<c-1; i++) {
                            int srcCountSum = 0;
                            String srcName = reportData.custDatas.get(0).counts.get(i).srcName;
                            for(int j=0; j<reportData.custDatas.size(); j++) {
                                srcCountSum += reportData.custDatas.get(j).counts.get(i).srcCount;
                            }

                            TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_cust_table_layout_medium, ((CustomerViewHolder) holder).report_item_cust_tablelayout, false);
                            TextView tv_time = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_time);
                            TextView tv_src = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_src);
                            TextView tv_count = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_count);

                            tv_time.setText(custTime);
                            tv_src.setText(srcName);
                            tv_count.setText(srcCountSum+"");

                            ((CustomerViewHolder) holder).report_item_cust_tablelayout.addView(tableRow);
                        }
                        int srcCountSum = 0;
                        String srcName = reportData.custDatas.get(0).counts.get(c-1).srcName;
                        for(int j=0; j<reportData.custDatas.size(); j++) {
                            srcCountSum += reportData.custDatas.get(j).counts.get(c-1).srcCount;
                        }

                        TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_cust_table_layout, ((CustomerViewHolder) holder).report_item_cust_tablelayout, false);
                        TextView tv_time = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_time);
                        TextView tv_src = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_src);
                        TextView tv_count = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_count);

                        tv_time.setText(custTime);
                        tv_src.setText(srcName);
                        tv_count.setText(srcCountSum+"");

                        ((CustomerViewHolder) holder).report_item_cust_tablelayout.addView(tableRow);
                    }

                }else {
                    TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_cust_table_head, ((CustomerViewHolder) holder).report_item_cust_tablelayout, false);
                    ((CustomerViewHolder) holder).report_item_cust_tablelayout.addView(headTableRow);
                }

            }else {
                LineData data = new LineData(xVals, dataSets);
                ((CustomerViewHolder)holder).report_item_cust_linechart.setData(data);
                ((CustomerViewHolder)holder).report_item_cust_linechart.invalidate();
//                TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_cust_table_head, null);
//                ((CustomerViewHolder) holder).report_item_cust_tablelayout.addView(headTableRow);
            }


        }else if(datas.get(position).type == ReportData.TYPE_AGENT) {
            RxBus.getInstance().toObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {
                            if(event instanceof AgentChooised) {
                                agentsList = ((AgentChooised)event).agents;
                                refreshAgentWork(agnetWorkTimeStatus, reportData, agentsList);
                            }
                        }
                    });

            ((AgentViewHolder)holder).tv.setText(reportData.name);

            ((AgentViewHolder)holder).report_item_agent_iv_addagent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(MobileAssitantCache.getInstance().getAgents().size() > 0) {
                        Intent intent = new Intent(context, AgentChooiseActivity.class);
                        context.startActivity(intent);
                        ((ReportActivity)context).overridePendingTransition(R.anim.activity_open,0);
                    }else {
                        Toast.makeText(context,"没有坐席可以选择",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ((AgentViewHolder)holder).report_item_agent_time_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AgentViewHolder)holder).report_item_agent_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_checked);
                    ((AgentViewHolder)holder).report_item_agent_time_day.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((AgentViewHolder)holder).report_item_agent_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((AgentViewHolder)holder).report_item_agent_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((AgentViewHolder)holder).report_item_agent_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((AgentViewHolder)holder).report_item_agent_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    agnetWorkTimeStatus = "day";
                    refreshAgentWork("day", reportData, agentsList);

                }
            });
            ((AgentViewHolder)holder).report_item_agent_time_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AgentViewHolder)holder).report_item_agent_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((AgentViewHolder)holder).report_item_agent_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((AgentViewHolder)holder).report_item_agent_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_checked);
                    ((AgentViewHolder)holder).report_item_agent_time_week.setTextColor(context.getResources().getColor(R.color.all_white));
                    ((AgentViewHolder)holder).report_item_agent_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_normal);
                    ((AgentViewHolder)holder).report_item_agent_time_month.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));

                    agnetWorkTimeStatus = "week";
                    refreshAgentWork("week", reportData, agentsList);

                }
            });
            ((AgentViewHolder)holder).report_item_agent_time_month.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AgentViewHolder)holder).report_item_agent_time_day.setBackgroundResource(R.drawable.bg_report_callout_day_normal);
                    ((AgentViewHolder)holder).report_item_agent_time_day.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((AgentViewHolder)holder).report_item_agent_time_week.setBackgroundResource(R.drawable.bg_report_callout_week_normal);
                    ((AgentViewHolder)holder).report_item_agent_time_week.setTextColor(context.getResources().getColor(R.color.report_callout_time_tv));
                    ((AgentViewHolder)holder).report_item_agent_time_month.setBackgroundResource(R.drawable.bg_report_callout_month_checked);
                    ((AgentViewHolder)holder).report_item_agent_time_month.setTextColor(context.getResources().getColor(R.color.all_white));

                    agnetWorkTimeStatus = "month";
                    refreshAgentWork("month", reportData, agentsList);

                }
            });

            ((AgentViewHolder)holder).report_item_agent_barchart.setDescription("");
            ((AgentViewHolder)holder).report_item_agent_barchart.setPinchZoom(true);

            ((AgentViewHolder)holder).report_item_agent_barchart.setDrawBarShadow(false);

            ((AgentViewHolder)holder).report_item_agent_barchart.setDrawGridBackground(false);

            YAxis leftAxis = ((AgentViewHolder)holder).report_item_agent_barchart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setSpaceTop(30f);
            leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

            ((AgentViewHolder)holder).report_item_agent_barchart.getAxisRight().setEnabled(false);
            ((AgentViewHolder)holder).report_item_agent_barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            ((AgentViewHolder)holder).report_item_agent_barchart.setHighlightPerTapEnabled(false);
            ((AgentViewHolder)holder).report_item_agent_barchart.setHighlightPerDragEnabled(false);


            ((AgentViewHolder)holder).report_item_agent_ll_show.removeAllViews();
            ArrayList<String> xVals = new ArrayList<String>();
            agentsList.clear();
            for (int i = 0; i < reportData.workLoadDatas.size(); i++) {
                if(reportData.workLoadDatas.get(i).AgentID != null) {

                    String agentId = reportData.workLoadDatas.get(i).AgentID;
                    MAAgent agent = MobileAssitantCache.getInstance().getAgentById(agentId);
                    if(agent != null) {
                        agentsList.add(agent);
                        String x = agent.displayName+"["+agent.exten+"]";
                        xVals.add(x);
                    }else {
                        xVals.add("未知");
                    }
                }else {
                    xVals.add("未知");
                }
            }

            for(int i=0; i<xVals.size(); i++) {
                TextView textView = new TextView(context);
                textView.setPadding(WindowUtils.dip2px(16), 0, 0, 0);
                textView.setText(xVals.get(i));
                ((AgentViewHolder)holder).report_item_agent_ll_show.addView(textView);
            }

            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals3 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> yVals4 = new ArrayList<BarEntry>();

            for (int i = 0; i < reportData.workLoadDatas.size(); i++) {
                float val = reportData.workLoadDatas.get(i).CallInTimeLength;
                yVals1.add(new BarEntry(val, i));
                float val1 = reportData.workLoadDatas.get(i).CallInAverageTimeLength;
                yVals2.add(new BarEntry(val1, i));
                float val2 = reportData.workLoadDatas.get(i).CallOutTimeLength;
                yVals3.add(new BarEntry(val2, i));
                float val3 = reportData.workLoadDatas.get(i).CallOutAverageTimeLength;
                yVals4.add(new BarEntry(val3, i));

            }

            BarDataSet set1, set2, set3, set4;

            set1 = new BarDataSet(yVals1, "呼入通话总时长");
            set1.setColor(mColors[0]);
            set2 = new BarDataSet(yVals2, "呼入通话平均时长");
            set2.setColor(mColors[1]);
            set3 = new BarDataSet(yVals3, "外呼通话总时长");
            set3.setColor(mColors[2]);
            set4 = new BarDataSet(yVals4, "外呼通话平均时长");
            set4.setColor(mColors[3]);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);
            dataSets.add(set3);
            dataSets.add(set4);

            BarData data = new BarData(xVals, dataSets);

            data.setGroupSpace(80f);
            data.setValueFormatter(new MyValueFormatter());
            ((AgentViewHolder)holder).report_item_agent_barchart.setData(data);
            ((AgentViewHolder)holder).report_item_agent_barchart.invalidate();


            ((AgentViewHolder)holder).report_item_agent_tablelayout_callin.removeAllViews();
            ((AgentViewHolder)holder).report_item_agent_tablelayout_callout.removeAllViews();
            if(reportData.workLoadDatas.size() > 0) {

                TableRow headTableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_head_callin, ((AgentViewHolder)holder).report_item_agent_tablelayout_callin, false);
                ((AgentViewHolder) holder).report_item_agent_tablelayout_callin.addView(headTableRow);
                TableRow headTableRow_callout = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_head_callout, ((AgentViewHolder)holder).report_item_agent_tablelayout_callout, false);
                ((AgentViewHolder) holder).report_item_agent_tablelayout_callout.addView(headTableRow_callout);
                if(reportData.workLoadDatas.size() == 1) {
                    String name = "";
                    if(reportData.workLoadDatas.get(0).AgentID != null) {
                        MAAgent agent = MobileAssitantCache.getInstance().getAgentById(reportData.workLoadDatas.get(0).AgentID);
                        if(agent != null) {
                            name = agent.displayName +"["+agent.exten+"]";
                        }else {
                            name = "未知";
                        }
                    }
                    String accessCountStr = reportData.workLoadDatas.get(0).CallInAccessCount + "";
                    String acceptCountStr = reportData.workLoadDatas.get(0).CallInAcceptCount + "";
                    String progress = reportData.workLoadDatas.get(0).CallInAcceptRate + "";
                    String time = TimeUtil.getREportTime(reportData.workLoadDatas.get(0).CallInTimeLength);

                    TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_layout, ((AgentViewHolder)holder).report_item_agent_tablelayout_callin, false);
                    TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_acceptrate);
                    TextView tv_time = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_time);

                    tv_name.setText(name);
                    tv_access.setText(accessCountStr);
                    tv_accept.setText(acceptCountStr);
                    tv_progress.setText(progress);
                    tv_time.setText(time);

                    ((AgentViewHolder) holder).report_item_agent_tablelayout_callin.addView(tableRow);

                    String accessCountStr_callout = reportData.workLoadDatas.get(0).CallOutAccessCount + "";
                    String acceptCountStr_callout = reportData.workLoadDatas.get(0).CallOutAcceptCount + "";
                    String progress_callout = reportData.workLoadDatas.get(0).AgentUtilization + "";
                    String time_callout = TimeUtil.getREportTime(reportData.workLoadDatas.get(0).CallOutTimeLength);

                    TableRow tableRow_callout = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_layout, ((AgentViewHolder)holder).report_item_agent_tablelayout_callout, false);
                    TextView tv_name_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_acceptrate);
                    TextView tv_time_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_time);

                    tv_name_callout.setText(name);
                    tv_access_callout.setText(accessCountStr_callout);
                    tv_accept_callout.setText(acceptCountStr_callout);
                    tv_progress_callout.setText(progress_callout);
                    tv_time_callout.setText(time_callout);

                    ((AgentViewHolder) holder).report_item_agent_tablelayout_callout.addView(tableRow_callout);
                }else {
                    for (int i = 0; i < reportData.workLoadDatas.size()-1; i++) {
                        String name = "未知";
                        if(reportData.workLoadDatas.get(i).AgentID != null) {
                            MAAgent agent = MobileAssitantCache.getInstance().getAgentById(reportData.workLoadDatas.get(i).AgentID);
                            if(agent != null) {
                                name = agent.displayName +"["+agent.exten+"]";
                            }else {
                                name = "未知";
                            }
                        }
                        String accessCountStr = reportData.workLoadDatas.get(i).CallInAccessCount + "";
                        String acceptCountStr = reportData.workLoadDatas.get(i).CallInAcceptCount + "";
                        String progress = reportData.workLoadDatas.get(i).CallInAcceptRate + "";
                        String time = TimeUtil.getREportTime(reportData.workLoadDatas.get(i).CallInTimeLength);

                        TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_layout_medium, ((AgentViewHolder)holder).report_item_agent_tablelayout_callin, false);
                        TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                        TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                        TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                        TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_acceptrate);
                        TextView tv_time = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_time);

                        tv_name.setText(name);
                        tv_access.setText(accessCountStr);
                        tv_accept.setText(acceptCountStr);
                        tv_progress.setText(progress);
                        tv_time.setText(time);
                        ((AgentViewHolder) holder).report_item_agent_tablelayout_callin.addView(tableRow);

                        String accessCountStr_callout = reportData.workLoadDatas.get(i).CallOutAccessCount + "";
                        String acceptCountStr_callout = reportData.workLoadDatas.get(i).CallOutAcceptCount + "";
                        String progress_callout = reportData.workLoadDatas.get(i).AgentUtilization + "";
                        String time_callout = TimeUtil.getREportTime(reportData.workLoadDatas.get(i).CallOutTimeLength);

                        TableRow tableRow_callout = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_layout_medium, ((AgentViewHolder)holder).report_item_agent_tablelayout_callout, false);
                        TextView tv_name_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_name);
                        TextView tv_access_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_access);
                        TextView tv_accept_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_accept);
                        TextView tv_progress_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_acceptrate);
                        TextView tv_time_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_time);

                        tv_name_callout.setText(name);
                        tv_access_callout.setText(accessCountStr_callout);
                        tv_accept_callout.setText(acceptCountStr_callout);
                        tv_progress_callout.setText(progress_callout);
                        tv_time_callout.setText(time_callout);

                        ((AgentViewHolder) holder).report_item_agent_tablelayout_callout.addView(tableRow_callout);
                    }

                    String name = "未知";
                    if(reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).AgentID != null) {
                        MAAgent agent = MobileAssitantCache.getInstance().getAgentById(reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).AgentID);
                        if(agent != null) {
                            name = agent.displayName +"["+agent.exten+"]";
                        }else {
                            name = "未知";
                        }
                    }
                    String accessCountStr = reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).CallInAccessCount + "";
                    String acceptCountStr = reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).CallInAcceptCount + "";
                    String progress = reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).CallInAcceptRate + "";
                    String time = TimeUtil.getREportTime(reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).CallInTimeLength);

                    TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_layout, ((AgentViewHolder)holder).report_item_agent_tablelayout_callin, false);
                    TextView tv_name = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_acceptrate);
                    TextView tv_time = (TextView) tableRow.findViewById(R.id.report_item_tablerow_tv_time);

                    tv_name.setText(name);
                    tv_access.setText(accessCountStr);
                    tv_accept.setText(acceptCountStr);
                    tv_progress.setText(progress);
                    tv_time.setText(time);
                    ((AgentViewHolder) holder).report_item_agent_tablelayout_callin.addView(tableRow);

                    String accessCountStr_callout = reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).CallOutAccessCount + "";
                    String acceptCountStr_callout = reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).CallOutAcceptCount + "";
                    String progress_callout = reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).AgentUtilization + "";
                    String time_callout = TimeUtil.getREportTime(reportData.workLoadDatas.get(reportData.workLoadDatas.size()-1).CallOutTimeLength);

                    TableRow tableRow_callout = (TableRow) LayoutInflater.from(context).inflate(R.layout.report_item_agent_table_layout, ((AgentViewHolder)holder).report_item_agent_tablelayout_callout, false);
                    TextView tv_name_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_name);
                    TextView tv_access_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_access);
                    TextView tv_accept_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_accept);
                    TextView tv_progress_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_acceptrate);
                    TextView tv_time_callout = (TextView) tableRow_callout.findViewById(R.id.report_item_tablerow_tv_time);

                    tv_name_callout.setText(name);
                    tv_access_callout.setText(accessCountStr_callout);
                    tv_accept_callout.setText(acceptCountStr_callout);
                    tv_progress_callout.setText(progress_callout);
                    tv_time_callout.setText(time_callout);

                    ((AgentViewHolder) holder).report_item_agent_tablelayout_callout.addView(tableRow_callout);
                }
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


    private void refreshAgentWork(String time, final ReportData reportData, List<MAAgent> agentsList) {

        System.out.println("刷新了坐席工作量报表");
        if(agentsList.size() > 0) {
            List<String> agentIds = new ArrayList<>();
            for (int i=0; i<agentsList.size(); i++) {
                agentIds.add(agentsList.get(i)._id);
            }
            HttpManager.getInstance().refreshAgentWorkReport(UserDao.getInstance().getUser()._id, time, agentIds)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(String s) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(s);
                                if (jsonObject.getBoolean("Succeed")) {
                                    System.out.println("工作量报表刷新数据返回结果:"+s);
                                    JSONObject agentwork = jsonObject.getJSONObject("agentwork");
                                    if(agentwork.getBoolean("success")) {
                                        JSONArray workloadArray = agentwork.getJSONObject("data").getJSONArray("workload");
                                        reportData.workLoadDatas = gson.fromJson(workloadArray.toString(),
                                                new TypeToken<List<WorkLoadData>>() {
                                                }.getType());
                                        notifyDataSetChanged();
                                    }
                                }
                            }catch (Exception e) {

                            }
                        }
                    });

        }

    }

    private void setLineChart(LineChart lineChart) {
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription("");
        lineChart.setDrawBorders(false);

        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setDrawAxisLine(true);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setTextSize(10f);
//        lineChart.getXAxis().setLabelRotationAngle(-60f);


        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(context.getResources().getColor(R.color.grey_erp));

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
        TextView report_item_callout_table_tv_time,report_item_callout_table_tv_access,report_item_callout_table_tv_deal,report_item_callout_table_tv_progress;

        public CallOutViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_callout_linechart = (LineChart) itemView.findViewById(R.id.report_item_callout_linechart);
            report_item_callout_time_day = (TextView) itemView.findViewById(R.id.report_item_callout_time_day);
            report_item_callout_time_week = (TextView) itemView.findViewById(R.id.report_item_callout_time_week);
            report_item_callout_time_month = (TextView) itemView.findViewById(R.id.report_item_callout_time_month);

            report_item_callout_table_tv_time = (TextView) itemView.findViewById(R.id.report_item_callout_table_tv_time);
            report_item_callout_table_tv_access = (TextView) itemView.findViewById(R.id.report_item_callout_table_tv_access);
            report_item_callout_table_tv_deal = (TextView) itemView.findViewById(R.id.report_item_callout_table_tv_deal);
            report_item_callout_table_tv_progress = (TextView) itemView.findViewById(R.id.report_item_callout_table_tv_progress);
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
        TableLayout report_item_queue_tablelayout;

        public QueueViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_queue_barchart = (BarChart) itemView.findViewById(R.id.report_item_queue_barchart);
            report_item_queue_tv_time_day = (TextView) itemView.findViewById(R.id.report_item_queue_tv_time_day);
            report_item_queue_tv_time_week = (TextView) itemView.findViewById(R.id.report_item_queue_tv_time_week);
            report_item_queue_tv_time_month = (TextView) itemView.findViewById(R.id.report_item_queue_tv_time_month);

            report_item_queue_tablelayout = (TableLayout) itemView.findViewById(R.id.report_item_queue_tablelayout);
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

        TableLayout report_item_im_tablelayout;

        public IMViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_im_barchart = (BarChart) itemView.findViewById(R.id.report_item_im_barchart);
            report_item_im_tv_time_day = (TextView) itemView.findViewById(R.id.report_item_im_tv_time_day);
            report_item_im_tv_time_week = (TextView) itemView.findViewById(R.id.report_item_im_tv_time_week);
            report_item_im_tv_time_month = (TextView) itemView.findViewById(R.id.report_item_im_tv_time_month);

            report_item_im_tablelayout = (TableLayout) itemView.findViewById(R.id.report_item_im_tablelayout);
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

        TextView report_item_session_table_tv_time,report_item_session_table_tv_manual,report_item_session_table_tv_robot,report_item_session_table_tv_convert;
        public SessionViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_session_linechart = (LineChart) itemView.findViewById(R.id.report_item_session_linechart);
            report_item_session_time_day = (TextView) itemView.findViewById(R.id.report_item_session_time_day);
            report_item_session_time_week = (TextView) itemView.findViewById(R.id.report_item_session_time_week);
            report_item_session_time_month = (TextView) itemView.findViewById(R.id.report_item_session_time_month);

            report_item_session_table_tv_time = (TextView) itemView.findViewById(R.id.report_item_session_table_tv_time);
            report_item_session_table_tv_manual = (TextView) itemView.findViewById(R.id.report_item_session_table_tv_manual);
            report_item_session_table_tv_robot = (TextView) itemView.findViewById(R.id.report_item_session_table_tv_robot);
            report_item_session_table_tv_convert = (TextView) itemView.findViewById(R.id.report_item_session_table_tv_convert);
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
        TableLayout report_item_cust_tablelayout;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_cust_linechart = (LineChart) itemView.findViewById(R.id.report_item_cust_linechart);
            report_item_cust_time_day = (TextView) itemView.findViewById(R.id.report_item_cust_time_day);
            report_item_cust_time_week = (TextView) itemView.findViewById(R.id.report_item_cust_time_week);
            report_item_cust_time_month = (TextView) itemView.findViewById(R.id.report_item_cust_time_month);

            report_item_cust_tablelayout = (TableLayout) itemView.findViewById(R.id.report_item_cust_tablelayout);
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

    class AgentViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{
        TextView tv;
        BarChart report_item_agent_barchart;
        TextView report_item_agent_time_day, report_item_agent_time_week, report_item_agent_time_month;
        TableLayout report_item_agent_tablelayout_callin, report_item_agent_tablelayout_callout;
        ImageView report_item_agent_iv_addagent;
        LinearLayout report_item_agent_ll_show;

        public AgentViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.report_item_tv_name);
            report_item_agent_barchart = (BarChart) itemView.findViewById(R.id.report_item_agent_barchart);
            report_item_agent_time_day = (TextView) itemView.findViewById(R.id.report_item_agent_time_day);
            report_item_agent_time_week = (TextView) itemView.findViewById(R.id.report_item_agent_time_week);
            report_item_agent_time_month = (TextView) itemView.findViewById(R.id.report_item_agent_time_month);

            report_item_agent_tablelayout_callin = (TableLayout) itemView.findViewById(R.id.report_item_agent_tablelayout_callin);
            report_item_agent_tablelayout_callout = (TableLayout) itemView.findViewById(R.id.report_item_agent_tablelayout_callout);
            report_item_agent_iv_addagent = (ImageView) itemView.findViewById(R.id.report_item_agent_iv_addagent);
            report_item_agent_ll_show = (LinearLayout) itemView.findViewById(R.id.report_item_agent_ll_show);
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
