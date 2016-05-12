package com.moor.im.options.dial.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.options.dial.model.CallLogModel;

public class CallLogAdapter extends BaseAdapter{
	
	Context context;
	List<CallLogModel> calllogs;
	
	public CallLogAdapter() {
	}
	
	public CallLogAdapter(Context context, List<CallLogModel> calllogs) {
		this.context = context;
		this.calllogs = calllogs;
	}
	
	@Override
	public int getCount() {
		return calllogs.size();
	}

	@Override
	public Object getItem(int position) {
		return calllogs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Viewholder holder;
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.fragment_dial_calllog_list_item, null);
			holder = new Viewholder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.calllog_listview_item_textview_name);
			holder.tv_number = (TextView) convertView.findViewById(R.id.calllog_listview_item_textview_number);
			holder.tv_date = (TextView) convertView.findViewById(R.id.calllog_listview_item_textview_date);
			holder.tv_duration = (TextView) convertView.findViewById(R.id.calllog_listview_item_textview_duration);
			holder.iv_type = (ImageView) convertView.findViewById(R.id.calllog_listview_item_iv_type);
			convertView.setTag(holder);
		}else{
			holder = (Viewholder) convertView.getTag();
		} 
		
		holder.tv_name.setText(calllogs.get(position).getDisplayName());
		holder.tv_number.setText(calllogs.get(position).getNumber());
		holder.tv_date.setText(TimeUtil.convertTimeToFriendly(calllogs.get(position).getDate()));
		holder.tv_duration.setText(TimeUtil.getContactsLogTime(calllogs.get(position).getDuration())+"秒");
		if("呼入".equals(calllogs.get(position).getType())) {
			holder.iv_type.setBackgroundResource(R.drawable.listitem_history_callin);
		}else if("呼出".equals(calllogs.get(position).getType())) {
			holder.iv_type.setBackgroundResource(R.drawable.listitem_history_callout);
		}else if("未接听".equals(calllogs.get(position).getType())) {
			holder.iv_type.setBackgroundResource(R.drawable.listitem_history_misscall);
		}
		return convertView;
	}
	
	static class Viewholder{
		TextView tv_name;
		TextView tv_number;
		TextView tv_date;
		TextView tv_duration;
		ImageView iv_type;
	}

}
