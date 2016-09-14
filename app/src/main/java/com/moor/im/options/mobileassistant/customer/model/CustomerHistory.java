package com.moor.im.options.mobileassistant.customer.model;

import java.io.Serializable;

/**
 * Created by longwei on 16/9/9.
 */
public class CustomerHistory implements Serializable{

    public int typeCode;
    public String typeName;
    public String action;
    public String date;
    public String time;
    public String comment;
    public boolean isShowDate;
    public String status;

}
