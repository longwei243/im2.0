package com.moor.im.common.model;

import com.moor.imkf.ormlite.field.DatabaseField;
import com.moor.imkf.ormlite.table.DatabaseTable;

/**
 * Created by longwei on 2016/3/16.
 */
@DatabaseTable(tableName = "userrole")
public class UserRole {

    // 主键 id 自增长
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public String role;

    @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true)
    public User user;
}
