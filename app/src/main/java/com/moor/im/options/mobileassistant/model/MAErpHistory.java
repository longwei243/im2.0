package com.moor.im.options.mobileassistant.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by longwei on 2016/3/4.
 */
public class MAErpHistory implements Serializable{

    public String name;
    public String time;
    public String imIcon;
    public String info;
    public List<FieldData> historyData;
}
