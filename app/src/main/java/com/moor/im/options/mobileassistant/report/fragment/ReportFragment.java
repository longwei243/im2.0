package com.moor.im.options.mobileassistant.report.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moor.im.R;
import com.moor.im.options.base.BaseLazyFragment;

/**
 * Created by longwei on 2016/6/13.
 */
public class ReportFragment extends BaseLazyFragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, null);

        return view;
    }
}
