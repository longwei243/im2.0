package com.moor.im.options.mobileassistant.erp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.im.common.dialog.LoadingDialog;
import com.moor.im.common.http.HttpManager;
import com.moor.im.common.http.HttpParser;
import com.moor.im.common.http.ResponseListener;
import com.moor.im.common.rxbus.RxBus;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.options.mobileassistant.erp.event.HaveOrderEvent;
import com.moor.im.options.mobileassistant.erp.fragment.DLQFragment;
import com.moor.im.options.mobileassistant.model.MABusiness;

import java.util.HashMap;
import java.util.List;
/**
 * Created by longwei on 2016/2/29.
 */
public class RoalUnDealOrderAdapter extends BaseAdapter{

    private List<MABusiness> maBusinesses;
    private DLQFragment context;
    private String userId;
    LoadingDialog loadingFragmentDialog;

    public RoalUnDealOrderAdapter() {}

    public RoalUnDealOrderAdapter(DLQFragment context, List<MABusiness> maBusinesses, String userId) {
        this.context = context;
        this.maBusinesses = maBusinesses;
        this.userId = userId;
        loadingFragmentDialog = new LoadingDialog();
    }

    @Override
    public int getCount() {
        return maBusinesses.size();
    }

    @Override
    public Object getItem(int position) {
        return maBusinesses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context.getActivity()).inflate(R.layout.erp_dlq_list_item, null);
            viewHolder.tv_lq = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_lq);
            viewHolder.tv_customername = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_customername);
            viewHolder.tv_shorttime = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_shorttime);
            viewHolder.tv_flow = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_flow);
            viewHolder.tv_step = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_step);
            viewHolder.tv_createuser = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_createuser);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final MABusiness maBusiness = maBusinesses.get(position);
        viewHolder.tv_customername.setText(NullUtil.checkNull(maBusiness.name));
        viewHolder.tv_shorttime.setText(NullUtil.checkNull(maBusiness.lastUpdateTime));
        viewHolder.tv_flow.setText(NullUtil.checkNull(maBusiness.flow));
        viewHolder.tv_step.setText(NullUtil.checkNull(maBusiness.step));
        viewHolder.tv_createuser.setText(NullUtil.checkNull(maBusiness.createUser));

        viewHolder.tv_lq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingFragmentDialog.show(context.getActivity().getSupportFragmentManager(), "");
                HashMap<String, String> datas = new HashMap<>();
                datas.put("master", userId);
                datas.put("_id", maBusiness._id);
                HttpManager.getInstance().haveThisOrder(userId, datas, new HaveThisOrderResponseHandler(position));

            }
        });
        return convertView;
    }

    final static class ViewHolder{
        TextView tv_lq;
        TextView tv_customername;
        TextView tv_shorttime;
        TextView tv_flow;
        TextView tv_step;
        TextView tv_createuser;
    }

    class HaveThisOrderResponseHandler implements ResponseListener {

        private int position;

        public HaveThisOrderResponseHandler() {

        }
        public HaveThisOrderResponseHandler(int postion) {
            this.position = postion;
        }

        @Override
        public void onFailed() {
            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(String responseString) {

            String msg = HttpParser.getMessage(responseString);
            loadingFragmentDialog.dismiss();
            if(maBusinesses.size() > 0) {
                if (HttpParser.getSucceed(responseString)) {
                    maBusinesses.remove(position);
                    notifyDataSetChanged();
                    RxBus.getInstance().send(new HaveOrderEvent(1));
                    Toast.makeText(context.getActivity(), "领取成功", Toast.LENGTH_SHORT).show();
                }else if("此业务已被其他人领取。".equals(msg)){
                    maBusinesses.remove(position);
                    notifyDataSetChanged();
                    RxBus.getInstance().send(new HaveOrderEvent(0));
                    Toast.makeText(context.getActivity(), "此业务已被其他人领取", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context.getActivity(), "领取失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
