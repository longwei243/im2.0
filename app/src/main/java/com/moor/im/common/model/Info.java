package com.moor.im.common.model;

import com.moor.imkf.ormlite.field.DatabaseField;
import com.moor.imkf.ormlite.table.DatabaseTable;

/**
 * Created by longwei on 2016/4/7.
 */
@DatabaseTable(tableName = "info")
public class Info {

    public Info() {}

    @DatabaseField(generatedId = true)
    public int _id;

    @DatabaseField
    public String name;

    @DatabaseField
    public String password;

    @DatabaseField
    public String connectionId;

    @DatabaseField
    public String isSucceed;
    @DatabaseField
    public String isChangePassWord;

}
