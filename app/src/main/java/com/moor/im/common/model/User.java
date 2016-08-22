package com.moor.im.common.model;

import java.util.List;

import com.moor.imkf.ormlite.dao.ForeignCollection;
import com.moor.imkf.ormlite.field.DatabaseField;
import com.moor.imkf.ormlite.field.ForeignCollectionField;
import com.moor.imkf.ormlite.table.DatabaseTable;

/**
 * 用户实体类
 * 
 * @author Mr.li
 * 
 */
@DatabaseTable(tableName = "user")
public class User {

	public User() {
	}

	// 主键 id 自增长
	@DatabaseField(generatedId = true)
	public int id;
	@ForeignCollectionField
	public ForeignCollection<UserRole> userRoles;

	public List<String> role;
	@DatabaseField
	public String _id;
	@DatabaseField
	public String account;
	@DatabaseField
	public String pbx;
	@DatabaseField
	public String type;
	@DatabaseField
	public String mobile;
	@DatabaseField
	public String email;
	@DatabaseField
	public String status;
	@DatabaseField
	public String product;
	@DatabaseField
	public String uversion;
	@DatabaseField
	public String lastUpdate;
	@DatabaseField
	public String callerIDNum;
	@DatabaseField
	public String exten;
	@DatabaseField
	public String password;
	@DatabaseField
	public String displayName;
	@DatabaseField
	public String loginName;
	@DatabaseField
	public String pinyin;
	@DatabaseField
	public String sipExten;
	@DatabaseField
	public String AutoBusyTime;
	@DatabaseField
	public String accountId;
	@DatabaseField
	public String platform;
	@DatabaseField
	public String connectServer;
	@DatabaseField
	public String connectionId;
	@DatabaseField
	public String ioSessionId;
	@DatabaseField
	public String pbxSipAddr;
	@DatabaseField
	public String sipExtenSecret;
	@DatabaseField
	public Boolean isAdmin;
	@DatabaseField
	public String im_icon;

}
