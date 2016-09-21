package com.moor.im.options.mobileassistant.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;

import java.util.List;

public class SimpleAdapter extends BaseAdapter {

  private LayoutInflater layoutInflater;
  List<String> datas;

  public SimpleAdapter(Context context, List<String> datas) {
    layoutInflater = LayoutInflater.from(context);
    this.datas = datas;
  }

  @Override
  public int getCount() {
    return datas.size();
  }

  @Override
  public Object getItem(int position) {
    return position;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;

    if (convertView == null) {
      convertView = layoutInflater.inflate(R.layout.simple_list_item, parent, false);

      viewHolder = new ViewHolder();
      viewHolder.textView = (TextView) convertView.findViewById(R.id.text_view);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }


    viewHolder.textView.setText(datas.get(position));

    return convertView;
  }

  static class ViewHolder {
    TextView textView;
  }
}
