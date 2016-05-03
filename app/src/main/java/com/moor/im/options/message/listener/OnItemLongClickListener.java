package com.moor.im.options.message.listener;

import android.view.View;

/**
 * Created by longwei on 2016/4/19.
 */
public interface OnItemLongClickListener {
    /**
     * on item long click call back
     *
     * @param convertView convertView
     * @param position position
     * @return true false
     */
    boolean onItemLongClick(View convertView, int position);
}