package com.moor.im.options.mobileassistant.customer.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.dialog.dialogplus.DialogPlus;
import com.moor.im.common.utils.log.LogUtil;
import com.moor.im.common.views.roundimage.RoundImageView;
import com.moor.im.options.mobileassistant.customer.activity.CustomerDetailActivity;
import com.moor.im.options.mobileassistant.customer.dialog.Mp3PlayDialog;
import com.moor.im.options.mobileassistant.customer.model.CustomerHistory;
import com.moor.im.options.mobileassistant.customer.model.CustomerHistoryData;

import java.util.List;

/**
 * Created by longwei on 16/8/25.
 */
public class CustomerHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<CustomerHistory> datas;
    private RecyclerView mRecyclerView;
    private static final int VIEW_ITEM = 0;
    private static final int VIEW_PROG = 1;
    private boolean isLoading;
    private int totalItemCount;
    private int lastVisibleItemPosition;
    //当前滚动的position下面最小的items的临界值
    private int visibleThreshold = 4;

    public CustomerHistoryAdapter(Context context, List<CustomerHistory> datas, RecyclerView recyclerView) {
        this.context = context;
        this.datas = datas;
        this.mRecyclerView = recyclerView;

        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            //mRecyclerView添加滑动事件监听
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItemPosition + visibleThreshold)) {
                        //此时是刷新状态
                        if (mMoreDataListener != null) {
                            System.out.println("触发了加载");
                            mMoreDataListener.loadMoreData();
                        }
                        isLoading = true;
                    }
                }
            });
        }
    }
    public void setLoaded() {
        isLoading = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder holder;
        if (viewType == VIEW_ITEM) {
            holder = new CallInViewHolder(LayoutInflater.from(context).inflate(R.layout.customer_history_item_callin, null));
        } else {
            holder = new MyProgressViewHolder(LayoutInflater.from(context).inflate(R.layout.item_footer, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CallInViewHolder) {
            final CustomerHistory chd = datas.get(position);

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
            if(chd.recordFile != null && !"".equals(chd.recordFile)) {
                ((CallInViewHolder)holder).customer_history_ll_record.setVisibility(View.VISIBLE);
                ((CallInViewHolder)holder).customer_history_ll_record.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Mp3PlayDialog dialog = new Mp3PlayDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString("path", chd.recordFile);
                        dialog.setArguments(bundle);
                        dialog.show(((CustomerDetailActivity)context).getSupportFragmentManager(), "");
                    }
                });
            }else {
                ((CallInViewHolder)holder).customer_history_ll_record.setVisibility(View.GONE);
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
            }else if(chd.typeCode == CustomerHistoryData.TYPE_EMAIL) {
                ((CallInViewHolder)holder).customer_history_item_iv_type.setImageResource(R.drawable.customer_history_note);
            }
        } else if (holder instanceof MyProgressViewHolder) {
            if (((MyProgressViewHolder) holder).pb != null && mMoreDataListener != null)
                ((MyProgressViewHolder) holder).pb.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return datas.get(position) != null ? VIEW_ITEM : VIEW_PROG;

    }

    private class CallInViewHolder extends RecyclerView.ViewHolder {

        TextView customer_detail_tv_action, customer_detail_tv_time,
                customer_detail_tv_status, customer_history_item_tv_date,
                customer_detail_tv_comment;

        RoundImageView customer_history_item_iv_type;
        LinearLayout customer_history_ll_comment, customer_history_ll_record;



        public CallInViewHolder(View itemView) {
            super(itemView);
            customer_detail_tv_action = (TextView) itemView.findViewById(R.id.customer_detail_tv_action);
            customer_detail_tv_time = (TextView) itemView.findViewById(R.id.customer_detail_tv_time);
            customer_detail_tv_status = (TextView) itemView.findViewById(R.id.customer_detail_tv_status);
            customer_history_item_tv_date = (TextView) itemView.findViewById(R.id.customer_history_item_tv_date);
            customer_detail_tv_comment = (TextView) itemView.findViewById(R.id.customer_detail_tv_comment);

            customer_history_item_iv_type = (RoundImageView) itemView.findViewById(R.id.customer_history_item_iv_type);
            customer_history_ll_comment = (LinearLayout) itemView.findViewById(R.id.customer_history_ll_comment);
            customer_history_ll_record = (LinearLayout) itemView.findViewById(R.id.customer_history_ll_record);
        }
    }

    public class MyProgressViewHolder extends RecyclerView.ViewHolder {

        private final ProgressBar pb;

        public MyProgressViewHolder(View itemView) {
            super(itemView);
            pb = (ProgressBar) itemView.findViewById(R.id.pb);
        }

    }

    //设置数据的方法
    public void setData(List<CustomerHistory> data) {
        datas = data;
    }

    private LoadMoreDataListener mMoreDataListener;

    //加载更多监听方法
    public void setOnMoreDataLoadListener(LoadMoreDataListener onMoreDataLoadListener) {
        mMoreDataListener = onMoreDataLoadListener;
    }

    public interface LoadMoreDataListener {
        void loadMoreData();
    }
}
