package com.moor.im.options.mobileassistant.customer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.utils.NullUtil;
import com.moor.im.options.mobileassistant.MobileAssitantCache;
import com.moor.im.options.mobileassistant.customer.CustCacheUtil;
import com.moor.im.options.mobileassistant.model.MAAgent;
import com.moor.im.options.mobileassistant.model.MACallLogData;
import com.moor.im.options.mobileassistant.model.MACustomer;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by longwei on 16/9/6.
 */
public class MyCustomerAdapter extends BaseAdapter {

    private List<MACustomer> mMACustomers;
    private Context context;
    private String custCacheStr;

    private int[] mColors = new int[] {
            Color.rgb(249, 102, 0),
            Color.rgb(204, 0, 0),
            Color.rgb(221, 144, 255),
            Color.rgb(51, 153, 255),
            Color.rgb(102, 102, 102),
            Color.rgb(0, 0, 0),
            Color.rgb(189, 177, 0),
            Color.rgb(0, 153, 0),
            Color.rgb(0, 38, 128),
            Color.rgb(91, 0, 181)
    };

    public MyCustomerAdapter() {}

    public MyCustomerAdapter(Context context, List<MACustomer> mMACustomers, String custCacheStr) {
        this.context = context;
        this.mMACustomers = mMACustomers;
        this.custCacheStr = custCacheStr;
    }

    @Override
    public int getCount() {
        return mMACustomers.size();
    }

    @Override
    public Object getItem(int position) {
        return mMACustomers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.mycustomer_listview_item, null);
            viewHolder.mycustomer_listview_item_tv_name = (TextView) convertView.findViewById(R.id.mycustomer_listview_item_tv_name);
            viewHolder.mycustomer_listview_item_tv_owner = (TextView) convertView.findViewById(R.id.mycustomer_listview_item_tv_owner);
            viewHolder.mycustomer_listview_item_tv_title = (TextView) convertView.findViewById(R.id.mycustomer_listview_item_tv_title);
            viewHolder.mycustomer_listview_item_tv_status = (TextView) convertView.findViewById(R.id.mycustomer_listview_item_tv_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MACustomer customer = mMACustomers.get(position);

        String statusStr = NullUtil.checkNull(customer.status);
        String status = CustCacheUtil.getStatus(custCacheStr, NullUtil.checkNull(customer.dbType), statusStr);
        String name = "无归属";
        MAAgent agent = MobileAssitantCache.getInstance().getAgentById(NullUtil.checkNull(customer.owner));
        if (agent != null) {
            name = agent.displayName;
        }

        viewHolder.mycustomer_listview_item_tv_name.setText(NullUtil.checkNull(customer.name));

        viewHolder.mycustomer_listview_item_tv_owner.setText(name);

        viewHolder.mycustomer_listview_item_tv_title.setText(NullUtil.checkNull(customer.title));

        viewHolder.mycustomer_listview_item_tv_status.setText(status);
        try{
            viewHolder.mycustomer_listview_item_tv_status.setTextColor(mColors[Integer.parseInt(statusStr.substring(6))]);
        }catch (Exception e) {

        }


        return convertView;
    }

    final static class ViewHolder{
        TextView mycustomer_listview_item_tv_name;
        TextView mycustomer_listview_item_tv_owner;
        TextView mycustomer_listview_item_tv_title;
        TextView mycustomer_listview_item_tv_status;

    }
}
