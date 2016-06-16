package com.moor.im.options.mobileassistant.report.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moor.im.R;
import com.moor.im.options.base.BaseLazyFragment;
import com.moor.im.options.mobileassistant.report.adapter.DividerItemDecoration;
import com.moor.im.options.mobileassistant.report.adapter.ItemDragHelperCallback;
import com.moor.im.options.mobileassistant.report.adapter.ReportAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2016/6/13.
 */
public class ReportFragment extends BaseLazyFragment{

    private RecyclerView report_rv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, null);

        report_rv = (RecyclerView) view.findViewById(R.id.report_rv);
        init();
        return view;
    }

    private void init() {
        final List<String> items = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            items.add("Index " + i);
        }

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        report_rv.setLayoutManager(manager);
        report_rv.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));
        ItemDragHelperCallback callback = new ItemDragHelperCallback(){
            @Override
            public boolean isLongPressDragEnabled() {
                // 长按拖拽打开
                return true;
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(report_rv);

        ReportAdapter adapter = new ReportAdapter(getActivity(), items);
        report_rv.setAdapter(adapter);
    }
}
