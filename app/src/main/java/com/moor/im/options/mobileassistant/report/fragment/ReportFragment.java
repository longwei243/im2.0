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
import com.moor.im.options.mobileassistant.report.model.ReportData;

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
        final List<ReportData> items = new ArrayList<>();
        ReportData rd = new ReportData();
        rd.type = ReportData.TYPE_CALL_IN;
        rd.name = "呼入";
        items.add(rd);
        ReportData rd1 = new ReportData();
        rd1.type = ReportData.TYPE_CALL_OUT;
        rd1.name = "呼出";
        items.add(rd1);
        ReportData rd2 = new ReportData();
        rd2.type = ReportData.TYPE_QUEUE;
        rd2.name = "技能组";
        items.add(rd2);
        ReportData rd3 = new ReportData();
        rd3.type = ReportData.TYPE_IM;
        rd3.name = "客服";
        items.add(rd3);
        ReportData rd4 = new ReportData();
        rd4.type = ReportData.TYPE_SESSION;
        rd4.name = "会话";
        items.add(rd4);
        ReportData rd5 = new ReportData();
        rd5.type = ReportData.TYPE_CUSTOMER;
        rd5.name = "客户";
        items.add(rd5);


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
