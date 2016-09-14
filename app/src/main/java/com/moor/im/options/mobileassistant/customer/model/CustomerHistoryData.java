package com.moor.im.options.mobileassistant.customer.model;

/**
 * Created by longwei on 16/8/25.
 */
public class CustomerHistoryData {

    public static final int TYPE_CALL_IN = 0;
    public static final int TYPE_CALL_OUT = 1;
    public static final int TYPE_CHAT = 2;
    public static final int TYPE_BUSSINESS = 3;
    public static final int TYPE_NOTE = 4;
    public static final int TYPE_EMAIL = 5;
    public static final int TYPE_APPROVAL = 6;

    public String name;
    public int typeCode;

    public String _id;
    public String type;
    public String customer;
    public String createTime;
    public String agent;
    public String status;
    public String lastUpdateTime;
    public String comments;
    public String dispose;

    public static int getTypeCode(String type) {
        int typeCode = 0;
        if("callin".equals(type)) {
            typeCode = TYPE_CALL_IN;
        }else if("callout".equals(type)) {
            typeCode = TYPE_CALL_OUT;
        }else if("chat".equals(type)) {
            typeCode = TYPE_CHAT;
        }else if("email".equals(type)) {
            typeCode = TYPE_EMAIL;
        }else if("note".equals(type)) {
            typeCode = TYPE_NOTE;
        }else if("business".equals(type)) {
            typeCode = TYPE_BUSSINESS;
        }else if("approval".equals(type)) {
            typeCode = TYPE_APPROVAL;
        }

        return typeCode;
    }

    public static String getTypeString(String type) {
        String typeStr = "";
        if("callin".equals(type)) {
            typeStr = "来电呼入";
        }else if("callout".equals(type)) {
            typeStr = "外呼去电";
        }else if("chat".equals(type)) {
            typeStr = "微信咨询";
        }else if("email".equals(type)) {
            typeStr = "邮件处理";
        }else if("note".equals(type)) {
            typeStr = "制定联系计划";
        }else if("business".equals(type)) {
            typeStr = "处理工单";
        }else if("approval".equals(type)) {
            typeStr = "审批";
        }

        return typeStr;
    }

}
