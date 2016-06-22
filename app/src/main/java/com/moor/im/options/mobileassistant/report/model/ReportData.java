package com.moor.im.options.mobileassistant.report.model;

import java.util.List;

/**
 * Created by longwei on 2016/6/20.
 */
public class ReportData {

    public static final int TYPE_CALL_IN = 0;
    public static final int TYPE_CALL_OUT = 1;
    public static final int TYPE_QUEUE = 2;
    public static final int TYPE_IM = 3;
    public static final int TYPE_SESSION = 4;
    public static final int TYPE_CUSTOMER = 5;

    public int type;
    public String name;
    public List<CallInData> callInDatas;
    public List<CallOutData> callOutDatas;
    public List<QueueData> queueDatas;
    public List<IMData> imDatas;
    public List<SessionData> sessionDatas;
    public List<CustData> custDatas;
}
