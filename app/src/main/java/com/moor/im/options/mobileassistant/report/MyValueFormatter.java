package com.moor.im.options.mobileassistant.report;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by longwei on 16/6/23.
 */
public class MyValueFormatter implements ValueFormatter {


    public MyValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        // write your logic here
        return (int)value + "";
    }

}
